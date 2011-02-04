package hudson.plugins.testng.util;

import hudson.model.AbstractBuild;
import hudson.plugins.helpers.AbstractBuildAction;
import hudson.plugins.testng.results.TestResults;

import java.util.ArrayList;
import java.util.List;


public class TestResultHistoryUtil {

   /**
    * Loops through all the previous builds and add their test results to the
    * previousResults array.
    * We'd rather make this list when needed otherwise if we cache these values in the memory
    * we will run out of memory
    *
    * @return list of previous build test results
    */
   public static List<TestResults>
   getPreviousBuildTestResults(AbstractBuild<?, ?> owner) {
      List<TestResults> previousResults = new ArrayList<TestResults>();
      AbstractBuild<?, ?> previousBuild = owner.getPreviousBuild();
      while (previousBuild != null) {
         if (previousBuild.getAction(AbstractBuildAction.class) != null) {
            if (previousBuild.getAction(AbstractBuildAction.class).getResults() != null) {
               previousResults.add(previousBuild.getAction(AbstractBuildAction.class).getResults());
            }
         }
         previousBuild = previousBuild.getPreviousBuild();
      }
      return previousResults;
   }

   //TODO: move getPreviousXXXResults from MethodResult,ClassResult and PackageResult to this class
}
