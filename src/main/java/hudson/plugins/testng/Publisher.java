package hudson.plugins.testng;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.testng.results.TestNGResult;
import hudson.plugins.testng.results.MethodResult;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * This class defines a @Publisher and @Extension
 *
 */
public class Publisher extends Recorder {

   //ant style regex pattern to find report files
   public final String reportFilenamePattern;
   //should test description be HTML escaped or not
   public final boolean escapeTestDescp;
   //should exception messages be HTML escaped or not
   public final boolean escapeExceptionMsg;
   //should failed builds be included in graphs or not
   public final boolean showFailedBuilds;
   //skipped tests mark build as unstable
   public final boolean unstableOnSkippedTests;
   //failed config mark build as failure
   public final boolean failureOnFailedTestConfig;
   //ignore test failure if one passed
   public final boolean ignoreTestFailureIfTestPassesOnce;

   @Extension
   public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

   @DataBoundConstructor
   public Publisher(String reportFilenamePattern, boolean escapeTestDescp, boolean escapeExceptionMsg,
                    boolean showFailedBuilds, boolean unstableOnSkippedTests, boolean failureOnFailedTestConfig, 
                    boolean ignoreTestFailureIfTestPassesOnce) {
      this.reportFilenamePattern = reportFilenamePattern;
      this.escapeTestDescp = escapeTestDescp;
      this.escapeExceptionMsg = escapeExceptionMsg;
      this.showFailedBuilds = showFailedBuilds;
      this.unstableOnSkippedTests = unstableOnSkippedTests;
      this.failureOnFailedTestConfig = failureOnFailedTestConfig;
      this.ignoreTestFailureIfTestPassesOnce = ignoreTestFailureIfTestPassesOnce;
   }

   public BuildStepMonitor getRequiredMonitorService() {
      return BuildStepMonitor.NONE;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public BuildStepDescriptor<hudson.tasks.Publisher> getDescriptor() {
      return DESCRIPTOR;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
      Collection<Action> actions = new ArrayList<Action>();
      actions.add(new TestNGProjectAction(project, escapeTestDescp, escapeExceptionMsg, showFailedBuilds));
      return actions;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
         throws InterruptedException, IOException {

      PrintStream logger = listener.getLogger();

      if (build.getResult().equals(Result.ABORTED)) {
         logger.println("Build Aborted. Not looking for any TestNG results.");
         return true;
      }

      // replace any variables in the user specified pattern
      EnvVars env = build.getEnvironment(listener);
      env.overrideAll(build.getBuildVariables());
      String pathsPattern = env.expand(reportFilenamePattern);

      logger.println("TestNG Reports Processing: START");
      logger.println("Looking for TestNG results report in workspace using pattern: " + pathsPattern);
      FilePath[] paths = locateReports(build.getWorkspace(), pathsPattern);

      if (paths.length == 0) {
         logger.println("Did not find any matching files.");
         //build can still continue
         return true;
      }

      /*
       * filter out the reports based on timestamps. See JENKINS-12187
       */
      paths = checkReports(build, paths, logger);

      boolean filesSaved = saveReports(getTestNGReport(build), paths, logger);
      if (!filesSaved) {
         logger.println("Failed to save TestNG XML reports");
         return true;
      }

      TestNGResult results = new TestNGResult();
      try {
         results = TestNGTestResultBuildAction.loadResults(build, logger);
      } catch (Throwable t) {
         /*
          * don't fail build if TestNG parser barfs.
          * only print out the exception to console.
          */
         t.printStackTrace(logger);
      }

      if (results.getTestList().size() > 0) {
         //create an individual report for all of the results and add it to the build
         build.addAction(new TestNGTestResultBuildAction(results));
         if (failureOnFailedTestConfig && results.getFailedConfigCount() > 0) {
             logger.println("Failed configuration methods found. Marking build as FAILURE.");
             build.setResult(Result.FAILURE);
         } else if (unstableOnSkippedTests && (results.getSkippedConfigCount() > 0 || results.getSkipCount() > 0)) {
            logger.println("Skipped Tests/Configs found. Marking build as UNSTABLE.");
            build.setResult(Result.UNSTABLE);
         } else if (results.getFailedConfigCount() > 0) {
            logger.println("Failed Configs found. Marking build as UNSTABLE.");
            build.setResult(Result.UNSTABLE);
         } else if(results.getFailCount() > 0) {
            if(ignoreTestFailureIfTestPassesOnce ) {
                if(countFailIgnoreTestPassesOnce(results) > 0) {
                    logger.println("Failed Tests found. Marking build as UNSTABLE.");
                    build.setResult(Result.UNSTABLE);
                } else {
                    logger.println("Failed Tests found, but were ignored because the test passed once.");
                }
            } else {
                logger.println("Failed Tests found. Marking build as UNSTABLE.");
                build.setResult(Result.UNSTABLE);
             }
         } 
      } else {
         logger.println("Found matching files but did not find any TestNG results.");
         return true;
      }
      logger.println("TestNG Reports Processing: FINISH");
      return true;
   }

    int countFailIgnoreTestPassesOnce(TestNGResult results) {
        int count = 0;
        Set<String> passed = new HashSet<String>();
        for (MethodResult test : results.getPassedTests()) {
            passed.add(test.getName());
        }

        for (MethodResult test : results.getFailedTests()) {
            if(!passed.contains(test.getName())) {
                ++count;
            }
        }

        return count;
    }

   /**
    * look for testng reports based in the configured parameter includes.
    * 'filenamePattern' is
    *   - an Ant-style pattern
    *   - a list of files and folders separated by the characters ;:,
    *
    * NOTE: based on how things work for emma plugin for jenkins
    */
   static FilePath[] locateReports(FilePath workspace,
        String filenamePattern) throws IOException, InterruptedException
   {

      // First use ant-style pattern
      try {
         FilePath[] ret = workspace.list(filenamePattern);
         if (ret.length > 0) {
            return ret;
         }
      } catch (Exception e) {}

      // If it fails, do a legacy search
      List<FilePath> files = new ArrayList<FilePath>();
      String parts[] = filenamePattern.split("\\s*[;:,]+\\s*");
      for (String path : parts) {
         FilePath src = workspace.child(path);
         if (src.exists()) {
            if (src.isDirectory()) {
               files.addAll(Arrays.asList(src.list("**/testng*.xml")));
            } else {
               files.add(src);
            }
         }
      }
      return files.toArray(new FilePath[files.size()]);
   }

   /**
    * Gets the directory to store report files
    */
   static FilePath getTestNGReport(AbstractBuild<?,?> build) {
       return new FilePath(new File(build.getRootDir(), "testng"));
   }

   static FilePath[] checkReports(AbstractBuild<?,?> build, FilePath[] paths,
            PrintStream logger)
   {
      List<FilePath> filePathList = new ArrayList<FilePath>(paths.length);

      for (FilePath report : paths) {
         /*
          * Check that the file was created as part of this build and is not
          * something left over from before.
          *
          * Checks that the last modified time of file is greater than the
          * start time of the build
          *
          */
         try {
            /*
             * dividing by 1000 and comparing because we want to compare secs
             * and not milliseconds
             */
            if (build.getTimestamp().getTimeInMillis() / 1000 <= report.lastModified() / 1000) {
               filePathList.add(report);
            } else {
               logger.println(report.getName() + " was last modified before "
                        + "this build started. Ignoring it.");
            }
         } catch (IOException e) {
            // just log the exception
            e.printStackTrace(logger);
         } catch (InterruptedException e) {
            // just log the exception
            e.printStackTrace(logger);
         }
      }
      return filePathList.toArray(new FilePath[]{});
   }

   static boolean saveReports(FilePath testngDir, FilePath[] paths, PrintStream logger)
   {
      logger.println("Saving reports...");
      try {
         testngDir.mkdirs();
         int i = 0;
         for (FilePath report : paths) {
            String name = "testng-results" + (i > 0 ? "-" + i : "") + ".xml";
            i++;
            FilePath dst = testngDir.child(name);
            report.copyTo(dst);
         }
      } catch (Exception e) {
         e.printStackTrace(logger);
         return false;
      }
      return true;
   }

   public static final class DescriptorImpl extends BuildStepDescriptor<hudson.tasks.Publisher> {

      /**
       * Do not instantiate DescriptorImpl.
       */
      private DescriptorImpl() {
         super(Publisher.class);
      }

      /**
       * {@inheritDoc}
       */
      public String getDisplayName() {
         return "Publish " + PluginImpl.DISPLAY_NAME;
      }

      @Override
      public hudson.tasks.Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
         return req.bindJSON(Publisher.class, formData);
      }

      public boolean isApplicable(Class<? extends AbstractProject> aClass) {
         return true;
      }
   }

}
