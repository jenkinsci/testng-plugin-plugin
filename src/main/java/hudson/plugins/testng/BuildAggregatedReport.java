package hudson.plugins.testng;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.plugins.testng.results.TestResults;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuildAggregatedReport extends AbstractBuildReport<MavenModuleSetBuild> implements MavenAggregatedReport {
   private HealthReport buildHealth = null;

   public BuildAggregatedReport(MavenModuleSetBuild build, Map<MavenModule, List<MavenBuild>> moduleBuilds) {
      super(new ArrayList<TestResults>());
      setBuild(build);
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void update(Map<MavenModule, List<MavenBuild>> moduleBuilds, MavenBuild newBuild) {
      BuildIndividualReport report = newBuild.getAction(BuildIndividualReport.class);
      if (report != null) {
         getResults().add(report.getResults());
         buildHealth = HealthReport.min(buildHealth, report.getBuildHealth());
      }
   }

   /**
    * {@inheritDoc}
    */
   public Class<? extends AggregatableAction> getIndividualActionType() {
      return BuildIndividualReport.class;
   }

   /**
    * {@inheritDoc}
    */
   public Action getProjectAction(MavenModuleSet moduleSet) {
      for (MavenModuleSetBuild build : moduleSet.getBuilds()) {
         if (build.getAction(BuildAggregatedReport.class) != null) {
            return new ProjectAggregatedReport(moduleSet);
         }
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public HealthReport getBuildHealth() {
      return buildHealth;
   }

}
