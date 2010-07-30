package hudson.plugins.testng;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.helpers.BuildProxy;
import hudson.plugins.helpers.Ghostwriter;
import hudson.plugins.testng.parser.ResultsParser;
import hudson.plugins.testng.results.TestResults;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class GhostWriter
      implements Ghostwriter,
      Ghostwriter.MasterGhostwriter,
      Ghostwriter.SlaveGhostwriter {

   private final String reportFilenamePattern;


   public GhostWriter(String reportFilenamePattern) {
      this.reportFilenamePattern = reportFilenamePattern;
   }

   /**
    *
    * @param build         The the build.
    * @param executionRoot The module root on which the build executed.
    * @param listener      The buildListener.
    * @return true TODO: why ??
    * @throws InterruptedException
    * @throws IOException
    */
   public boolean performFromMaster(AbstractBuild<?, ?> build,
                                    FilePath executionRoot,
                                    BuildListener listener)
         throws InterruptedException, IOException {
      return true;
   }

   /**
    *
    * @param build    The information about the build.
    * @param listener The buildListener.
    * @return true , always even if we did not find any test result
    * @throws InterruptedException
    * @throws IOException
    */
   public boolean performFromSlave(BuildProxy build,
                                   BuildListener listener)
         throws InterruptedException, IOException {
      FilePath[] paths = build.getExecutionRootDir().list(reportFilenamePattern);
      Collection<TestResults> results = null;
      Set<String> parsedFiles = new HashSet<String>();
      //loop through all the files and get the results
      for (FilePath path : paths) {
         final String pathStr = path.getRemote();
         if (!parsedFiles.contains(pathStr)) {
            parsedFiles.add(pathStr);
            Collection<TestResults> result = ResultsParser.parse(new File(pathStr));
            if (results == null) {
               results = result;
            } else {
               results.addAll(result);
            }
         }
      }
      if (results != null) {
         //create an individual report for all of the results and add it to the build
         BuildIndividualReport action = new BuildIndividualReport(results);
         build.getActions().add(action);
         TestResults r = TestResults.total(results);
         if (r.getFailedConfigurationMethodsCount() > 0 || r.getSkippedConfigurationMethodsCount() > 0 ||
               r.getFailedTestCount() > 0 || r.getSkippedTestCount() > 0) {
            build.setResult(Result.UNSTABLE);
         }
      }
      return true;
   }
}
