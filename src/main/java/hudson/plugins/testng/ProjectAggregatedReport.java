package hudson.plugins.testng;

import hudson.maven.MavenModuleSet;
import hudson.plugins.helpers.AbstractBuildAction;
import hudson.plugins.helpers.AbstractProjectAction;

/**
 *
 *
 */
public class ProjectAggregatedReport extends AbstractProjectAction<MavenModuleSet> {
   public ProjectAggregatedReport(MavenModuleSet project) {
      super(project);
   }

   protected Class<? extends AbstractBuildAction> getBuildActionClass() {
      return BuildAggregatedReport.class;
   }
}
