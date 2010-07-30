package hudson.plugins.testng;

import hudson.maven.MavenModuleSet;
import hudson.model.ProminentProjectAction;

/**
 * 
 *
 */
public class ProjectAggregatedReport extends AbstractProjectReport<MavenModuleSet> implements ProminentProjectAction {
   public ProjectAggregatedReport(MavenModuleSet project) {
      super(project);
   }

   protected Class<? extends AbstractBuildReport> getBuildActionClass() {
      return BuildAggregatedReport.class;
   }
}
