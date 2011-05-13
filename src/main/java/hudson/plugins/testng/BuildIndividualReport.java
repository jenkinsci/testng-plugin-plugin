package hudson.plugins.testng;

import hudson.model.HealthReport;
import hudson.model.AbstractBuild;
import hudson.plugins.helpers.AbstractBuildAction;
import hudson.plugins.testng.results.TestResults;

import java.util.Collection;

public class BuildIndividualReport extends AbstractBuildAction<AbstractBuild<?, ?>> {

   //TODO: Work on exposing health
   private HealthReport healthReport;

   public BuildIndividualReport(Collection<TestResults> testngResults) {
      super(testngResults);
   }

   /**
    * Write-once setter for property 'build'.
    *
    * @param build The value to set the build to.
    */
   @Override
   public synchronized void setBuild(AbstractBuild<?, ?> build) {
      super.setBuild(build);
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
}
