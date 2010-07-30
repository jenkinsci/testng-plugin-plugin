package hudson.plugins.testng;

import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;

/**
 * TODO javadoc.
 *
 */
public class ProjectIndividualReport extends AbstractProjectReport<AbstractProject<?, ?>> implements ProminentProjectAction {
   public ProjectIndividualReport(AbstractProject<?, ?> project) {
      super(project);
   }

   protected Class<? extends AbstractBuildReport> getBuildActionClass() {
      return BuildIndividualReport.class;
   }
}
