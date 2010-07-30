package hudson.plugins.testng;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.plugins.testng.results.TestResults;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BuildIndividualReport extends AbstractBuildReport<AbstractBuild<?, ?>>
      implements AggregatableAction {

   private HealthReport healthReport;

   public BuildIndividualReport(Collection<TestResults> testNGResultsCollection) {
      super(testNGResultsCollection);
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
   public MavenAggregatedReport createAggregatedAction(MavenModuleSetBuild build,
                                                       Map<MavenModule, List<MavenBuild>> moduleBuilds) {
      return new BuildAggregatedReport(build, moduleBuilds);
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
