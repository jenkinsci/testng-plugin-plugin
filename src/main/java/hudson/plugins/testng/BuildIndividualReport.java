package hudson.plugins.testng;

import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.AbstractBuild;
import hudson.plugins.testng.results.TestResults;

import java.io.Serializable;
import java.util.Collection;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class BuildIndividualReport implements HealthReportingAction, Serializable {

   /**
    * Unique identifier for this class.
    */
   private static final long serialVersionUID = 31415926L;

   /**
    * The owner of this Action.  Ideally I'd like this to be final and set in the constructor, but Maven does not
    * let us do that, so we need a setter.
    */
   private AbstractBuild<?, ?> build = null;

   private final TestResults results;

   //TODO: Work on exposing health
   private HealthReport healthReport;

   public BuildIndividualReport(Collection<TestResults> testngResults) {
      this.results = TestResults.total(true, testngResults);
   }

   /**
    * Write-once setter for property 'build'.
    *
    * @param build The value to set the build to.
    */
   public synchronized void setBuild(AbstractBuild<?, ?> build) {
      if (this.build == null && this.build != build) {
         this.build = build;
      }
      if (this.getBuild() != null) {
         getResults().setOwner(this.getBuild());
      }
   }

   /**
    * {@inheritDoc}
    */
   public HealthReport getBuildHealth() {
      return healthReport;
   }

   public void setBuildHealth(HealthReport healthReport) {
      this.healthReport = healthReport;
   }

   /**
    * Getter for property 'build'.
    *
    * @return Value for property 'build'.
    */
   public synchronized AbstractBuild<?, ?> getBuild() {
      return build;
   }

   /**
    * Override to control when the floating box should be displayed.
    *
    * @return <code>true</code> if the floating box should be visible.
    */
   public boolean isFloatingBoxActive() {
      return true;
   }

   /**
    * Override to control when the action displays a trend graph.
    *
    * @return <code>true</code> if the action should show a trend graph.
    */
   public boolean isGraphActive() {
      return false;
   }

   public TestResults getResults() {
      return results;
   }

   public TestResults getPreviousResults() {
      AbstractBuild<?, ?> previousBuild = getBuild().getPreviousBuild();
      while (previousBuild != null && previousBuild.getAction(getClass()) == null) {
         previousBuild = previousBuild.getPreviousBuild();
      }
      if (previousBuild == null) {
         return new TestResults("");
      } else {
         BuildIndividualReport action = previousBuild.getAction(getClass());
         return action.getResults();
      }
   }

   /**
    * The summary of this build report for display on the build index page.
    *
    * @return
    */
   public String getSummary() {
      return results.toSummary();
   }

   /**
    * {@inheritDoc}
    */
   public String getIconFileName() {
      return PluginImpl.ICON_FILE_NAME;
   }

   /**
    * {@inheritDoc}
    */
   public String getDisplayName() {
      return PluginImpl.DISPLAY_NAME;
   }

   /**
    * {@inheritDoc}
    */
   public String getUrlName() {
      return PluginImpl.URL;
   }

   public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
      return getResults().getDynamic(token, req, rsp);
   }
}
