package hudson.plugins.helpers;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.Recorder;

import java.io.IOException;

/**
 * An abstract Publisher that is designed to work with a Ghostwriter.
 */
public abstract class AbstractPublisherImpl extends Recorder {

   /**
    * Creates the configured Ghostwriter.
    *
    * @return returns the configured Ghostwriter.
    */
   protected abstract Ghostwriter newGhostwriter();

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
         throws InterruptedException, IOException {
      return BuildProxy.doPerform(newGhostwriter(), build, listener);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
      return true;
   }
}
