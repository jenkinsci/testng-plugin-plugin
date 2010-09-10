package hudson.plugins.testng;

import hudson.Extension;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.model.Action;
import hudson.model.AbstractProject;
import hudson.plugins.helpers.AbstractPublisherImpl;
import hudson.plugins.helpers.Ghostwriter;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * This class defines a @Publisher and @Extension
 *
 */
public class Publisher extends AbstractPublisherImpl {

   private String reportFilenamePattern;

   @DataBoundConstructor
   public Publisher(String reportFilenamePattern) {
      reportFilenamePattern.getClass();
      this.reportFilenamePattern = reportFilenamePattern;
   }


   public String getReportFilenamePattern() {
      return reportFilenamePattern;
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
      return new ProjectIndividualReport(project);
   }

   protected Ghostwriter newGhostwriter() {
      //return new GhostWriter(reportFilenamePattern, targets);
      return new GhostWriter(reportFilenamePattern);
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
         return !MavenModuleSet.class.isAssignableFrom(aClass)
               && !MavenModule.class.isAssignableFrom(aClass);
      }
   }

}
