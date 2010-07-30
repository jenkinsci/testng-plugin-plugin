package hudson.plugins.helpers;

import hudson.model.AbstractBuild;
import hudson.model.HealthReportingAction;

import java.io.Serializable;


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

   /**
    * Constructs a new AbstractBuildAction.
    */
   protected AbstractBuildAction() {
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

   /**
    * Override to define the graph name.
    *
    * @return The graph name.
    */
   public String getGraphName() {
      return getDisplayName();
   }

   /**
    * Override to control the build summary detail.
    *
    * @return the summary string for the main build page.
    */
   public String getSummary() {
      return "";
   }
}
