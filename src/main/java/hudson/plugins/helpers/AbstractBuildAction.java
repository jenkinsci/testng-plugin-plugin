package hudson.plugins.helpers;

import hudson.model.HealthReportingAction;
import hudson.model.AbstractBuild;
import hudson.plugins.testng.PluginImpl;
import hudson.plugins.testng.results.TestResults;

import java.io.Serializable;
import java.util.*;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class AbstractBuildAction<BUILD extends AbstractBuild<?, ?>>
      implements HealthReportingAction, Serializable {

   /**
    * Unique identifier for this class.
    */
   private static final long serialVersionUID = 31415926L;

   /**
    * The owner of this Action.  Ideally I'd like this to be final and set in the constructor, but Maven does not
    * let us do that, so we need a setter.
    */
   private BUILD build = null;

   private final TestResults results;

   /**
    * Constructs a new AbstractBuildReport.
    * @param results - testng test results
    */
   protected AbstractBuildAction(Collection<TestResults> results) {
      this.results = TestResults.total(results);
   }

   /**
    * Getter for property 'build'.
    *
    * @return Value for property 'build'.
    */
   public synchronized BUILD getBuild() {
      return build;
   }

   /**
    * Write once setter for property 'build'.
    *
    * @param build Value to set for property 'build'.
    */
   public synchronized void setBuild(BUILD build) {
      // Ideally I'd prefer to use and AtomicReference... but I'm unsure how it would work with the serialization fun
      if (this.build == null && this.build != build) {
         this.build = build;
      }
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
         AbstractBuildAction action = previousBuild.getAction(getClass());
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
