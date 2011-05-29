package hudson.plugins.testng;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.testng.parser.ResultsParser;
import hudson.plugins.testng.results.TestResults;
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

   private final String reportFilenamePattern;
   private final boolean isRelativePath;
   public final boolean escapeTestDescp;
   public final boolean escapeExceptionMsg;

   @DataBoundConstructor
   public Publisher(String reportFilenamePattern, boolean isRelativePath,
         boolean escapeTestDescp, boolean escapeExceptionMsg) {
      reportFilenamePattern.getClass();
      this.reportFilenamePattern = reportFilenamePattern;
      this.isRelativePath = isRelativePath;
      this.escapeTestDescp = escapeTestDescp;
      this.escapeExceptionMsg = escapeExceptionMsg;
   }

   public String getReportFilenamePattern() {
      return reportFilenamePattern;
   }

   public boolean getIsRelativePath() {
       return isRelativePath;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean needsToRunAfterFinalized() {
      return false;
   }

   public BuildStepMonitor getRequiredMonitorService() {
      return BuildStepMonitor.BUILD;
   }

   @Extension
   public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

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
   public Action getProjectAction(AbstractProject<?, ?> project) {
      return new ProjectIndividualReport(project, escapeTestDescp, escapeExceptionMsg);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
         throws InterruptedException, IOException {
      Collection<TestResults> results = null;
      Set<String> parsedFiles = new HashSet<String>();
      if (!isRelativePath) {
         //TODO : fix this code to handle relative and absolute path together
         //instead of branching here
         FilePath[] paths = build.getWorkspace().list(reportFilenamePattern);

         //loop through all the files and get the results
         for (FilePath path : paths) {
            final String pathStr = path.getRemote();
            if (!parsedFiles.contains(pathStr)) {
               parsedFiles.add(pathStr);
               ResultsParser parser = new ResultsParser(listener.getLogger());
               Collection<TestResults> result = parser.parse(new File(pathStr));
               if (results == null) {
                  results = result;
               } else {
                  results.addAll(result);
               }
            }
         }
      } else {
         String executionRootDirRemotePath = build.getWorkspace().getRemote();
         String testngResultXmlRelativePath = reportFilenamePattern;
         String testngResultXmlRemotePath = executionRootDirRemotePath + "/" + testngResultXmlRelativePath;
         ResultsParser parser = new ResultsParser(listener.getLogger());
         results = parser.parse(new File(testngResultXmlRemotePath));
      }

      if (results != null) {
         //create an individual report for all of the results and add it to the build
         BuildIndividualReport action = new BuildIndividualReport(results);
         action.setBuild(build);
         build.getActions().add(action);
         TestResults r = TestResults.total(true, results);
         if (r.getFailedConfigurationMethodsCount() > 0 || r.getSkippedConfigurationMethodsCount() > 0 ||
               r.getFailedTestCount() > 0 || r.getSkippedTestCount() > 0) {
            build.setResult(Result.UNSTABLE);
         }
      }
      return true;
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