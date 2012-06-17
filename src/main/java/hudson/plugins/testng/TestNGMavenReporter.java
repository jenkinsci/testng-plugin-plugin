package hudson.plugins.testng;

import hudson.Extension;
import hudson.FilePath;
import hudson.maven.MavenBuildProxy;
import hudson.maven.MavenBuildProxy.BuildCallable;
import hudson.maven.MavenReporter;
import hudson.maven.MavenReporterDescriptor;
import hudson.maven.MojoInfo;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.reporters.Messages;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.plugins.testng.results.TestResults;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;

public class TestNGMavenReporter extends MavenReporter {
    
    // TODO: better use this one than the one in TestNGAggregatedBuildAction?
//    @Override
//    public Action getAggregatedProjectAction(MavenModuleSet project) {
//        return new TestNGProjectAction(project, false, false); // TOD escapeTestDescp, escapeExceptionMsg));
//    }
    
    public Collection<? extends Action> getProjectActions(MavenModule module) {
        return Collections.singletonList(
                new TestNGProjectAction(module, true, true));
    }
    
    @Override
    public boolean postExecute(MavenBuildProxy build, MavenProject pom,
            MojoInfo mojo, final BuildListener listener, Throwable error)
            throws InterruptedException, IOException {
        if (!isATestRunMojo(mojo)) return true;
        
        build.registerAsProjectAction(this);

        PrintStream logger = listener.getLogger();

        File reportsDir;
        if (mojo.is("org.apache.maven.plugins", "maven-surefire-plugin", "test") ||
            mojo.is("org.apache.maven.plugins", "maven-failsafe-plugin", "integration-test")) {
            try {
                reportsDir = mojo.getConfigurationValue("reportsDirectory", File.class);
            } catch (ComponentConfigurationException e) {
                e.printStackTrace(listener.fatalError(Messages.SurefireArchiver_NoReportsDir()));
                build.setResult(Result.FAILURE);
                return true;
            }
        }
        else {
            reportsDir = new File(pom.getBasedir(), "target/surefire-reports");
        }

        if(reportsDir.isDirectory()) {

            synchronized (build) {
                
                FilePath reportsPath = new FilePath(reportsDir);
                
                FilePath[] paths = Publisher.locateReports(reportsPath, "testng-results.xml");
                
                
                if (paths.length == 0) {
                    logger.println("Did not find any matching files.");
                    //build can still continue
                    return true;
                 }
                
                /*
                 * filter out the reports based on timestamps. See JENKINS-12187
                 */
                paths = Publisher.checkReports(build.getTimestamp(), paths, logger);
                
                
                boolean filesSaved = Publisher.saveReports(getTestNGReport(build.getRootDir()), paths, logger);
                if (!filesSaved) {
                   logger.println("Failed to save TestNG XML reports");
                   return true;
                }
                
                build.execute(new BuildCallable<Boolean, IOException>() {
                        private static final long serialVersionUID = 1L;
                        public Boolean call(MavenBuild build) throws IOException, InterruptedException {
                            Action a = createTestNGBuildAction(build, listener);
                            if (a != null) {
                                build.addAction(a);
                            }
                            return Boolean.TRUE;
                        }
                });

                logger.println("TestNG Reports Processing: FINISH");
                return true;
           }
        }

        return true;
    }
    
    public static TestNGBuildAction createTestNGBuildAction(AbstractBuild<?, ?> build, BuildListener listener) {
        PrintStream logger = listener.getLogger();
        TestResults results = new TestResults("");
        try {
           results = TestNGBuildAction.loadResults(build, logger);
        } catch (Throwable t) {
           /*
            * don't fail build if TestNG parser barfs.
            * only print out the exception to console.
            */
           t.printStackTrace(logger);
        }

        if (results.getTestList().size() > 0) {
           //create an individual report for all of the results and add it to the build
           TestNGBuildAction action = new TestNGBuildAction(build, results);
           if (results.getFailedConfigCount() > 0 || results.getFailedTestCount() > 0) {
              build.setResult(Result.UNSTABLE);
           }
           return action;
        } else {
           logger.println("Found matching files but did not find any TestNG results.");
        }
        return null;
    }
        
    /**
     * Gets the directory to store report files
     */
    static FilePath getTestNGReport(FilePath rootDir) {
        return new FilePath(rootDir, "testng");
    }
    
    private static boolean isATestRunMojo(MojoInfo mojo) {
        if ((!mojo.is("com.sun.maven", "maven-junit-plugin", "test"))
            && (!mojo.is("org.sonatype.flexmojos", "flexmojos-maven-plugin", "test-run"))
            && (!mojo.is("org.eclipse.tycho", "tycho-surefire-plugin", "test"))
            && (!mojo.is("org.sonatype.tycho", "maven-osgi-test-plugin", "test"))
            && (!mojo.is("org.codehaus.mojo", "gwt-maven-plugin", "test"))
            && (!mojo.is("org.apache.maven.plugins", "maven-surefire-plugin", "test"))
            && (!mojo.is("org.apache.maven.plugins", "maven-failsafe-plugin", "integration-test")))
            return false;

        try {
            if (mojo.is("org.apache.maven.plugins", "maven-surefire-plugin", "test")) {
                Boolean skip = mojo.getConfigurationValue("skip", Boolean.class);
                if (((skip != null) && (skip))) {
                    return false;
                }
                
                if (mojo.pluginName.version.compareTo("2.3") >= 0) {
                    Boolean skipExec = mojo.getConfigurationValue("skipExec", Boolean.class);
                    
                    if (((skipExec != null) && (skipExec))) {
                        return false;
                    }
                }
                
                if (mojo.pluginName.version.compareTo("2.4") >= 0) {
                    Boolean skipTests = mojo.getConfigurationValue("skipTests", Boolean.class);
                    
                    if (((skipTests != null) && (skipTests))) {
                        return false;
                    }
                }
            }
            else if (mojo.is("com.sun.maven", "maven-junit-plugin", "test")) {
                Boolean skipTests = mojo.getConfigurationValue("skipTests", Boolean.class);
                
                if (((skipTests != null) && (skipTests))) {
                    return false;
                }
            }
            else if (mojo.is("org.sonatype.flexmojos", "flexmojos-maven-plugin", "test-run")) {
                Boolean skipTests = mojo.getConfigurationValue("skipTest", Boolean.class);
                if (((skipTests != null) && (skipTests))) {
                    return false;
                }
            } else if (mojo.is("org.sonatype.tycho", "maven-osgi-test-plugin", "test")) {
                Boolean skipTests = mojo.getConfigurationValue("skipTest", Boolean.class);
                if (((skipTests != null) && (skipTests))) {
                    return false;
                }
            } else if (mojo.is("org.eclipse.tycho", "tycho-surefire-plugin", "test")) {
                Boolean skipTests = mojo.getConfigurationValue("skipTest", Boolean.class);
                if (((skipTests != null) && (skipTests))) {
                    return false;
                }
            }

        } catch (ComponentConfigurationException e) {
            return false;
        }

        return true;
    }

    @Extension
    public static final class DescriptorImpl extends MavenReporterDescriptor {
        public String getDisplayName() {
            return "Publish " + PluginImpl.DISPLAY_NAME;
        }

        public TestNGMavenReporter newAutoInstance(MavenModule module) {
            return new TestNGMavenReporter();
        }
    }

    private static final long serialVersionUID = 1L;
}
