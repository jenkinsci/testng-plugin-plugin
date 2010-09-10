package hudson.plugins.testng;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.helpers.AbstractBuildAction;
import hudson.plugins.helpers.AbstractProjectAction;

/**
 * TODO javadoc.
 *
 */
public class ProjectIndividualReport extends AbstractProjectAction<AbstractProject<?, ?>> {
   public ProjectIndividualReport(AbstractProject<?, ?> project) {
      super(project);
   }

   protected Class<? extends AbstractBuildAction<AbstractBuild<?,?>>> getBuildActionClass() {
      return BuildIndividualReport.class;
   }
}
