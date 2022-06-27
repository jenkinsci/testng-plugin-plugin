package hudson.plugins.testng.util;

import java.util.List;

import hudson.model.Run;
import hudson.plugins.testng.TestNGTestResultBuildAction;
import hudson.plugins.testng.results.ClassResult;
import hudson.plugins.testng.results.MethodResult;
import hudson.plugins.testng.results.TestNGResult;

/**
 * Utility methods around displaying results (esp history of results)
 *
 * @author nullin
 */
public class TestResultHistoryUtil {
    
    private TestResultHistoryUtil() {}

   /**
    * Gets the latest build before this one and returns it's test result.
    *
    * We'd rather make this list when needed otherwise if we cache these values
    * in the memory we will run out of memory
    *
    * @return previous build test results if the build exists and has a
    *       {@link hudson.plugins.testng.TestNGTestResultBuildAction},
    *       otherwise returns an empty {@link hudson.plugins.testng.results.TestNGResult}
    *       object. <b>Never returns {@code null}.</b>
    */
   public static TestNGResult getPreviousBuildTestResults(Run<?, ?> owner) {
      // Doesn't make sense to return a build that is still running.
      // We can compare results with a previous build that completed
      Run<?, ?> previousBuild = owner.getPreviousCompletedBuild();
      if (previousBuild != null
               && previousBuild.getAction(TestNGTestResultBuildAction.class) != null) {
         return previousBuild.getAction(TestNGTestResultBuildAction.class).getResult();
      } else {
         return new TestNGResult();
      }
   }

   /**
    * Summarizes the delta in tests and also displays a list of failed/skipped
    * tests and configuration methods.
    *
    * The list is returned as an HTML unordered list.
    *
    * @param action TestNG build action
    * @return summarized
    */
   //TODO: move into Groovy/Jelly
   public static String toSummary(TestNGTestResultBuildAction action) {
      int prevFailedTestCount;
      int prevSkippedTestCount;
      int prevFailedConfigurationCount;
      int prevSkippedConfigurationCount;
      int prevTotalTestCount;

      Run<?,?> run = action.run;
      TestNGResult previousResult =
            TestResultHistoryUtil.getPreviousBuildTestResults(run);

      prevFailedTestCount = previousResult.getFailCount();
      prevSkippedTestCount = previousResult.getSkipCount();
      prevFailedConfigurationCount = previousResult.getFailedConfigCount();
      prevSkippedConfigurationCount = previousResult.getSkippedConfigCount();
      prevTotalTestCount = previousResult.getTotalCount();

      TestNGResult tr = action.getResult();

      return "<ul>" + diff(prevTotalTestCount, tr.getTotalCount(), "Total Tests")
            + diff(prevFailedConfigurationCount, tr.getFailedConfigCount(), "Failed Configurations")
            + printTestsUrls(tr.getFailedConfigs())
            + diff(prevFailedTestCount, tr.getFailCount(), "Failed Tests")
            + printTestsUrls(tr.getFailedTests())
            + diff(prevSkippedTestCount, tr.getSkipCount(), "Skipped Tests")
            + printTestsUrls(tr.getSkippedTests())
            + diff(prevSkippedConfigurationCount, tr.getSkippedConfigCount(), "Skipped Configurations")
            + printTestsUrls(tr.getSkippedConfigs())
            + "</ul>";
   }

   private static String diff(long prev, long curr, String name) {
      if (prev == curr) {
         return "<li>" + name + ": " + curr + " (&plusmn;0)</li>";
      } else if (prev < curr) {
         return "<li>" + name + ": " + curr + " (+" + (curr - prev) + ")</li>";
      } else { // if (a < b)
         return "<li>" + name + ": " + curr + " (-" + (prev - curr) + ")</li>";
      }
   }

   /*
      <OL start="10">
         <LI><a href="url">test_full_name</a></LI>
      </OL>
   */
   private static String printTestsUrls(List<MethodResult> methodResults) {
      StringBuilder htmlStr = new StringBuilder();
      htmlStr.append("<UL>");
      boolean firstGroup = true;
      String testName = "";
      String suiteName = "";
      int testIndex = 1;
      if (methodResults != null && methodResults.size() > 0) {
         for (MethodResult methodResult : methodResults) {
            if (!testName.equals(methodResult.getParentTestName())
                    || !suiteName.equals(methodResult.getParentSuiteName())) {
               if (!firstGroup) {
                  htmlStr.append("</OL></LI>");
               }
               firstGroup = false;
               testName = methodResult.getParentTestName();
               suiteName = methodResult.getParentSuiteName();
               htmlStr.append("<LI style=\"list-style-type:none\"><b>").append(suiteName).append(" / ").append(testName).append("</b>");
               htmlStr.append("<OL start=\"").append(testIndex).append("\">");
            }
            htmlStr.append("<LI>");
            if (methodResult.getParent() instanceof ClassResult) {
               htmlStr.append("<a href=\"").append(methodResult.getUpUrl());
               htmlStr.append("\">");
               htmlStr.append(((ClassResult)methodResult.getParent()).getCanonicalName());
               htmlStr.append(".").append(methodResult.getName()).append("</a>");
            } else {
               htmlStr.append(methodResult.getName());
            }
            htmlStr.append("</LI>");
            testIndex++;
         }

      }
      if (!firstGroup) {
         htmlStr.append("</OL></LI>");
      }
      htmlStr.append("</UL>");
      return htmlStr.substring(0);
   }

}
