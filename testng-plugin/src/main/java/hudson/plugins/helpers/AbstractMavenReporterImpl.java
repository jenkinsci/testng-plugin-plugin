package hudson.plugins.helpers;

import hudson.maven.MavenBuildProxy;
import hudson.maven.MavenReporter;
import hudson.maven.MojoInfo;
import hudson.model.BuildListener;
import hudson.model.Result;

import java.io.IOException;

import org.apache.maven.project.MavenProject;


public abstract class AbstractMavenReporterImpl extends MavenReporter {

   /**
    * Returns the execution mode that this reporter will follow.
    *
    * @return the execution mode that this reporter will follow.
    */
   protected MojoExecutionReportingMode getExecutionMode() {
      return MojoExecutionReportingMode.ONLY_REPORT_ON_SUCCESS;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean postExecute(MavenBuildProxy build,
                              MavenProject pom,
                              MojoInfo mojo,
                              BuildListener listener,
                              Throwable error)
         throws InterruptedException, IOException {
      if (!isExecutingMojo(mojo)) {
         // not a mojo who's result we are interested in
         return true;
      }

      final Boolean okToContinue = getExecutionMode().isOkToContinue(this, build, listener, error);
      if (okToContinue != null) {
         return okToContinue;
      }

      build.registerAsProjectAction(this);

      return BuildProxy.doPerform(newGhostwriter(pom, mojo), build, pom, listener);
   }

   /**
    * Returns <code>true</code> if this is the mojo that corresponds with execution of the task we want to report on.
    *
    * @param mojo The mojo execution.
    * @return <code>true</code> if this is the mojo that corresponds with execution of the task we want to report on.
    */
   protected abstract boolean isExecutingMojo(MojoInfo mojo);

   /**
    * Creates the configured Ghostwriter based on the information from the pom and mojo. Will only be called when
    * isExecutingMojo returns true.
    *
    * @param pom  The pom.
    * @param mojo The mojo for which isExecutingMojo returned true.
    * @return The configured Ghostwriter instance.
    */
   protected abstract Ghostwriter newGhostwriter(MavenProject pom, MojoInfo mojo);

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean preExecute(MavenBuildProxy build,
                             MavenProject pom,
                             MojoInfo mojo,
                             BuildListener listener)
         throws InterruptedException, IOException {
      return !isAutoconfMojo(mojo) || autoconfPom(build, pom, mojo, listener);
   }

   /**
    * Called during the preExecute phase when isAutoconfMojo returns true.
    * if we want to tweak the mojo.configuration to ensure that the mojo is configured with the options we need
    * here is the place to do that for example:
    * <p/>
    * XmlPlexusConfiguration xmlOutputDir = (XmlPlexusConfiguration) mojo.configuration.getChild("xmlOutputDir");
    * if (xmlOutputDir == null) {
    * xmlOutputDir = new XmlPlexusConfiguration("xmlOutputDir");
    * xmlOutputDir.setValue("${basedir}/target/xmlReport");
    * mojo.configuration.addChild(xmlOutputDir);
    * }
    *
    * @param build    The build.
    * @param pom      The pom which will be manipulated by this method to autoconfigure.
    * @param mojo     The mojo.
    * @param listener The build listener.
    * @return <code>true</code> if autoconf was successful.
    */
   protected boolean autoconfPom(MavenBuildProxy build,
                                 MavenProject pom,
                                 MojoInfo mojo,
                                 BuildListener listener) {
      return true;
   }

   /**
    * Returns <code>true</code> if this mojo execution is an execution that we want to modify, i.e. where we want to
    * "fix" some of the configuration details, e.g. enable XML reports, etc.
    *
    * @param mojo The mojo.
    * @return <code>true</code> if this mojo execution is an execution that we want to modify.
    */
   protected boolean isAutoconfMojo(MojoInfo mojo) {
      return false;
   }

// -------------------------- ENUMERATIONS --------------------------

   /**
    * The reporting mode.
    */
   protected enum MojoExecutionReportingMode {
      /**
       * Only runs the Ghostwriter when the mojo executed successfully.
       */
      ONLY_REPORT_ON_SUCCESS {
         Boolean isOkToContinue(MavenReporter reporter,
                                MavenBuildProxy build,
                                BuildListener listener,
                                Throwable error) {
            return error == null ? null : Boolean.TRUE;
         }
      },
      /**
       * Always runs the Ghostwriter, even if the mojo executed with errors.
       */
      ALWAYS_REPORT_STABLE {
         Boolean isOkToContinue(MavenReporter reporter,
                                MavenBuildProxy build,
                                BuildListener listener,
                                Throwable error) {
            return null;
         }},
      /**
       * Always runs the Ghostwriter, if the mojo executed with errors set the build to UNSTABLE.
       */
      REPORT_UNSTABLE_ON_ERROR {
         Boolean isOkToContinue(MavenReporter reporter,
                                MavenBuildProxy build,
                                BuildListener listener,
                                Throwable error) {
            if (error != null) {
               listener.getLogger().println("[HUDSON] "
                     + reporter.getDescriptor().getDisplayName()
                     + " setting build to UNSTABLE");
               build.setResult(Result.UNSTABLE);
            }
            return null;
         }
      };

// -------------------------- OTHER METHODS --------------------------

      /**
       * Decides whether to continue and call the Ghostwriter in the event of a mojo execution error.
       *
       * @param reporter The MavenReporter.
       * @param build    The maven build proxy.
       * @param listener The build listener.
       * @param error    The mojo execution exception.
       * @return <code>null</code> to continue with execution of the Ghostwriter. Otherwise <code>Boolean.TRUE</code>
       *         to continue the build or <code>Boolean.FALSE</code> to halt the build.
       */
      abstract Boolean isOkToContinue(MavenReporter reporter,
                                      MavenBuildProxy build,
                                      BuildListener listener,
                                      Throwable error);
   }
}
