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

   private boolean escapeTestDescp;
   private boolean escapeExceptionMsg;

   public ProjectIndividualReport(AbstractProject<?, ?> project, boolean escapeTestDescp, boolean escapeExceptionMsg) {
      super(project);
      this.escapeExceptionMsg = escapeExceptionMsg;
      this.escapeTestDescp = escapeTestDescp;
   }

   protected Class<? extends AbstractBuildAction<AbstractBuild<?,?>>> getBuildActionClass() {
      return BuildIndividualReport.class;
   }

  public boolean getEscapeTestDescp()
  {
    return escapeTestDescp;
  }

  public boolean getEscapeExceptionMsg()
  {
    return escapeExceptionMsg;
  }
}
