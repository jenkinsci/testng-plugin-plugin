package hudson.plugins.testng;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.testng.parser.ResultsParser;
import hudson.plugins.testng.results.TestResults;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * This class defines a @Publisher and @Extension
 *
 */
public class Publisher extends Recorder {

   public final String reportFilenamePattern;
   @Deprecated //not used anymore. Here to ensure installed versions of plugin are not affected
   private boolean isRelativePath;
   public final boolean escapeTestDescp;
   public final boolean escapeExceptionMsg;

   @Extension
   public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

   @DataBoundConstructor
   public Publisher(String reportFilenamePattern,
         boolean escapeTestDescp, boolean escapeExceptionMsg) {
      reportFilenamePattern.getClass();
      this.reportFilenamePattern = reportFilenamePattern;
      this.escapeTestDescp = escapeTestDescp;
      this.escapeExceptionMsg = escapeExceptionMsg;
   }

   public BuildStepMonitor getRequiredMonitorService() {
      return BuildStepMonitor.STEP;
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
      actions.add(new TestNGProjectAction(project, escapeTestDescp, escapeExceptionMsg));
      return actions;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
         throws InterruptedException, IOException {

      PrintStream logger = listener.getLogger();
      logger.println("Looking for TestNG results report in workspace using pattern: " + reportFilenamePattern);
      FilePath[] paths = locateReports(build.getWorkspace(), reportFilenamePattern);

      Collection<TestResults> results = new ArrayList<TestResults>();
      Set<String> parsedFiles = new HashSet<String>();

      if (paths.length == 0) {
         logger.println("Did not find any matching files.");
         build.setResult(Result.FAILURE);
         //build can still continue
         return true;
      }

      //loop through all the files and get the results
      ResultsParser parser = new ResultsParser(logger);
      for (FilePath path : paths) {
         final String pathStr = path.getRemote();
         if (!parsedFiles.contains(pathStr)) {
            TestResults result = parser.parse(new File(pathStr));
            if (result.getTestList().size() > 0) {
              logger.println("Found results for: " + pathStr);
              results.add(result);
              parsedFiles.add(pathStr);
            }
         }
      }

      //TODO: Save the reports

      if (results.size() > 0) {
         //create an individual report for all of the results and add it to the build
         TestNGBuildAction action = new TestNGBuildAction(build, results);
         build.getActions().add(action);
         TestResults r = TestResults.total(true, results);
         if (r.getFailedConfigurationMethodsCount() > 0 || r.getSkippedConfigurationMethodsCount() > 0 ||
               r.getFailedTestCount() > 0 || r.getSkippedTestCount() > 0) {
            build.setResult(Result.UNSTABLE);
         }
      } else {
         logger.println("Found matching files but did not find any TestNG results.");
         build.setResult(Result.FAILURE);
         //build can still continue
         return true;
      }

      return true;
   }

   /**
    * look for testng reports based in the configured parameter includes.
    * 'filenamePattern' is
    *   - an Ant-style pattern
    *   - a list of files and folders separated by the characters ;:,
    *
    * NOTE: based on how things work for emma plugin for jenkins
    */
   private FilePath[] locateReports(FilePath workspace,
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
               files.addAll(Arrays.asList(src.list("**/testng-results.xml")));
            } else {
               files.add(src);
            }
         }
      }
      return files.toArray(new FilePath[files.size()]);
   }

  /**
    * {@inheritDoc}
    */
   @Override
   public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
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