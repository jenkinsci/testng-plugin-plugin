package hudson.plugins.helpers;

import hudson.FilePath;
import hudson.maven.MavenBuildProxy;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.util.IOException2;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.maven.project.MavenProject;

/**
 * Proxy for the key build information.
 */
public final class BuildProxy implements Serializable {
// ------------------------------ FIELDS ------------------------------

   private final FilePath artifactsDir;
   private final FilePath projectRootDir;
   private final FilePath buildRootDir;
   private final FilePath executionRootDir;
   private final Calendar timestamp;
   private final List<AbstractBuildAction<AbstractBuild<?, ?>>> actions =
         new ArrayList<AbstractBuildAction<AbstractBuild<?, ?>>>();
   private Result result = null;
   private boolean continueBuild = true;
   private AbstractBuild<?,?> build;

   public void setBuild(AbstractBuild<?, ?> build) {
      this.build = build;
   }

   public AbstractBuild<?, ?> getBuild() {
      return build;
   }
   // -------------------------- STATIC METHODS --------------------------

   /**
    * (Call from master) Invokes the ghostwriter on the master and slave nodes for this build.
    *
    * @param ghostwriter The ghostwriter that will be doing the work for the publisher.
    * @param build       The build.
    * @param listener    The build listener.
    * @return <code>true</code> if the build can continue.
    * @throws IOException          on IOException.
    * @throws InterruptedException on InterruptedException.
    */
   public static boolean doPerform(Ghostwriter ghostwriter,
                                   AbstractBuild<?, ?> build,
                                   BuildListener listener)
         throws IOException, InterruptedException {

      // first, do we need to do anything on the slave

      if (ghostwriter instanceof Ghostwriter.SlaveGhostwriter) {

         // construct the BuildProxy instance that we will use

         BuildProxy buildProxy = new BuildProxy(
               new FilePath(build.getArtifactsDir()),
               new FilePath(build.getProject().getRootDir()),
               new FilePath(build.getRootDir()),
               build.getModuleRoot(),
               build.getTimestamp());
         buildProxy.setBuild(build);

         BuildProxyCallableHelper callableHelper = new BuildProxyCallableHelper(buildProxy, ghostwriter, listener);

         try {
            buildProxy = buildProxy.getExecutionRootDir().act(callableHelper);

            buildProxy.updateBuild(build);

            // terminate the build if necessary
            if (!buildProxy.isContinueBuild()) {
               return false;
            }
         } catch (Exception e) {
            throw unwrapException(e, listener);
         }
      }

      // finally, on to the master

      final Ghostwriter.MasterGhostwriter masterGhostwriter = Ghostwriter.MasterGhostwriter.class.cast(ghostwriter);

      return masterGhostwriter == null
            || masterGhostwriter.performFromMaster(build, build.getModuleRoot(), listener);
   }

   /**
    * Takes a remote exception that has been wrapped up in the remoting layer, and rethrows it as IOException,
    * InterruptedException or if all else fails, a RuntimeException.
    *
    * @param e        The wrapped exception.
    * @param listener The listener for the build.
    * @return never.
    * @throws IOException          if the wrapped exception is an IOException.
    * @throws InterruptedException if the wrapped exception is an InterruptedException.
    * @throws RuntimeException     if the wrapped exception is neither an IOException nor an InterruptedException.
    */
   private static RuntimeException unwrapException(Exception e,
                                                   BuildListener listener)
         throws IOException, InterruptedException {
      if (e.getCause() instanceof IOException) {
         throw new IOException2(e.getCause().getMessage(), e);
      }
      if (e.getCause() instanceof InterruptedException) {
         e.getCause().printStackTrace(listener.getLogger());
         throw new InterruptedException(e.getCause().getMessage());
      }
      if (e.getCause() instanceof RuntimeException) {
         throw new RuntimeException(e.getCause());
      }
      // How on earth do we get this far down the branch
      e.printStackTrace(listener.getLogger());
      throw new RuntimeException("Unexpected exception", e);
   }

   /**
    * (Designed for execution from the master) Updates the build with the results that were reported to this proxy.
    *
    * @param build The build to update.
    */
   public void updateBuild(AbstractBuild<?, ?> build) {
      // update the actions
      for (AbstractBuildAction<AbstractBuild<?, ?>> action : actions) {
         if (!build.getActions().contains(action)) {
            action.setBuild(build);
            build.getActions().add(action);
         }
      }

      // update the result
      if (result != null && result.isWorseThan(build.getResult())) {
         build.setResult(result);
      }
   }

   /**
    * (Call from slave) Invokes the ghostwriter on the master and slave nodes for this build.
    *
    * @param ghostwriter     The ghostwriter that will be doing the work for the publisher.
    * @param mavenBuildProxy The build (proxy).
    * @param pom             The maven pom.
    * @param listener        The build listener.
    * @return <code>true</code> if the build can continue.
    * @throws IOException          on IOException.
    * @throws InterruptedException on InterruptedException.
    */
   public static boolean doPerform(Ghostwriter ghostwriter,
                                   MavenBuildProxy mavenBuildProxy,
                                   MavenProject pom,
                                   final BuildListener listener)
         throws InterruptedException, IOException {

      // first, construct the BuildProxy instance that we will use

      BuildProxy buildProxy = new BuildProxy(
            mavenBuildProxy.getArtifactsDir(),
            mavenBuildProxy.getProjectRootDir(),
            mavenBuildProxy.getRootDir(),
            new FilePath(pom.getBasedir()),
            mavenBuildProxy.getTimestamp());

      // do we need to do anything on the slave

      if (ghostwriter instanceof Ghostwriter.SlaveGhostwriter) {
         final Ghostwriter.SlaveGhostwriter slaveGhostwriter = (Ghostwriter.SlaveGhostwriter) ghostwriter;

         // terminate the build if necessary
         if (!slaveGhostwriter.performFromSlave(buildProxy, listener)) {
            return false;
         }
      }

      // finally, on to the master

      try {
         return mavenBuildProxy.execute(new BuildProxyCallableHelper(buildProxy, ghostwriter, listener));
      } catch (Exception e) {
         throw unwrapException(e, listener);
      }
   }

// --------------------------- CONSTRUCTORS ---------------------------

   /**
    * Constructs a new build proxy that encapsulates all the information that a build step should need from the
    * slave.
    *
    * @param artifactsDir     The artifacts directory on the master.
    * @param projectRootDir   The project directory on the master (i.e. the .../hudson/jobs/ProjectName/). Note for
    *                         multi-module projects it will be .../hudson/jobs/ProjectName/modules/ModuleName/.
    * @param buildRootDir     The build results directory on the master.
    * @param executionRootDir The build base directory on the slave.
    * @param timestamp        The time when the build started executing.
    */
   private BuildProxy(FilePath artifactsDir,
                      FilePath projectRootDir,
                      FilePath buildRootDir,
                      FilePath executionRootDir,
                      Calendar timestamp) {
      this.artifactsDir = artifactsDir;
      this.projectRootDir = projectRootDir;
      this.buildRootDir = buildRootDir;
      this.executionRootDir = executionRootDir;
      this.timestamp = timestamp;
   }

// --------------------- GETTER / SETTER METHODS ---------------------

   /**
    * Getter for property 'actions'.
    *
    * @return Value for property 'actions'.
    */
   public List<AbstractBuildAction<AbstractBuild<?, ?>>> getActions() {
      return actions;
   }

   /**
    * Gets the directory (on the master) where the artifacts are archived.
    *
    * @return the directory (on the master) where the artifacts are archived.
    */
   public FilePath getArtifactsDir() {
      return artifactsDir;
   }

   /**
    * Root directory of the {@link hudson.model.AbstractBuild} on the master.
    * <p/>
    * Files related to the {@link hudson.model.AbstractBuild} should be stored below this directory.
    *
    * @return Root directory of the {@link hudson.model.AbstractBuild} on the master.
    */
   public FilePath getBuildRootDir() {
      return buildRootDir;
   }

   /**
    * Returns the root directory of the checked-out module on the machine where the build executes.
    * <p/>
    * This is usually where <tt>pom.xml</tt>, <tt>build.xml</tt>
    * and so on exists.
    *
    * @return Returns the root directory of the checked-out module on the machine where the build executes.
    */
   public FilePath getExecutionRootDir() {
      return executionRootDir;
   }

   /**
    * Root directory of the {@link hudson.model.AbstractProject} on the master.
    * <p/>
    * Files related to the {@link hudson.model.AbstractProject} should be stored below this directory.
    *
    * @return Root directory of the {@link hudson.model.AbstractProject} on the master.
    */
   public FilePath getProjectRootDir() {
      return projectRootDir;
   }

   /**
    * Getter for property 'result'.
    *
    * @return Value for property 'result'.
    */
   public Result getResult() {
      return result;
   }

   /**
    * Setter for property 'result'.
    *
    * @param result Value to set for property 'result'.
    */
   public void setResult(Result result) {
      this.result = result;

   }

   /**
    * When the build is scheduled.
    *
    * @return The time when the build started executing.
    */
   public Calendar getTimestamp() {
      return timestamp;
   }

   /**
    * Getter for property 'continueBuild'.
    *
    * @return Value for property 'continueBuild'.
    */
   public boolean isContinueBuild() {
      return continueBuild;
   }

   /**
    * Setter for property 'continueBuild'.
    *
    * @param continueBuild Value to set for property 'continueBuild'.
    */
   public void setContinueBuild(boolean continueBuild) {
      this.continueBuild = continueBuild;
   }
}
