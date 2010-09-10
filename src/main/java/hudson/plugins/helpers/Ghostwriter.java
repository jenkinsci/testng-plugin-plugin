package hudson.plugins.helpers;

import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.io.Serializable;

/**
 * A Ghostwriter implements the real work of a Publisher for both Maven projects and normal projects. An implementation
 * must implement at least one of SlaveGhostwriter and/or MasterGhostwriter. If both interfaces are
 * implemented, the slave execution will take place first. If neither interface is implemented, nothing will happen.
 *
 */
public interface Ghostwriter extends Serializable {
// -------------------------- INNER CLASSES --------------------------

   /**
    * If a Ghostwriter implements this interface, the performFromSlave method will be executed on the slave.
    */
   public static interface SlaveGhostwriter extends Ghostwriter {
// -------------------------- OTHER METHODS --------------------------

      /**
       * Runs (on the slave) the step over the given build and reports the progress to the listener.
       *
       * @param build    The information about the build.
       * @param listener The buildListener.
       * @return true if the build can continue, false if there was an error
       *         and the build needs to be aborted.
       * @throws InterruptedException If the build is interrupted by the user (in an attempt to abort the build.)
       *                              Normally the {@link hudson.tasks.BuildStep} implementations may simply forward
       *                              the exception it got from its lower-level functions.
       * @throws java.io.IOException  If the implementation wants to abort the processing when an {@link java.io.IOException}
       *                              happens, it can simply propagate the exception to the caller. This will cause
       *                              the build to fail, with the default error message.
       *                              Implementations are encouraged to catch {@link java.io.IOException} on its own to
       *                              provide a better error message, if it can do so, so that users have better
       *                              understanding on why it failed.
       */
      boolean performFromSlave(BuildProxy build, BuildListener listener) throws InterruptedException, IOException;
   }

   public static interface MasterGhostwriter extends Ghostwriter {
// -------------------------- OTHER METHODS --------------------------

      /**
       * Runs (on the master) the step over the given build and reports the progress to the listener.
       *
       * @param build         The the build.
       * @param executionRoot The module root on which the build executed.
       * @param listener      The buildListener.
       * @return true if the build can continue, false if there was an error
       *         and the build needs to be aborted.
       * @throws InterruptedException If the build is interrupted by the user (in an attempt to abort the build.)
       *                              Normally the {@link hudson.tasks.BuildStep} implementations may simply forward
       *                              the exception it got from its lower-level functions.
       * @throws java.io.IOException  If the implementation wants to abort the processing when an {@link java.io.IOException}
       *                              happens, it can simply propagate the exception to the caller. This will cause
       *                              the build to fail, with the default error message.
       *                              Implementations are encouraged to catch {@link java.io.IOException} on its own to
       *                              provide a better error message, if it can do so, so that users have better
       *                              understanding on why it failed.
       */
      boolean performFromMaster(AbstractBuild<?, ?> build, FilePath executionRoot, BuildListener listener) throws InterruptedException, IOException;
   }

}

