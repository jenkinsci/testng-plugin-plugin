package hudson.plugins.testng.util;

import hudson.model.AbstractBuild;
import hudson.plugins.testng.TestNGBuildAction;
import hudson.plugins.testng.TestNGProjectAction;
import hudson.plugins.testng.results.ClassResult;
import hudson.plugins.testng.results.MethodResult;
import hudson.plugins.testng.results.TestResults;

import java.util.List;

public class TestResultHistoryUtil {

   /**
    * Gets the latest build before this one and return's it's test result.
    *
    * We'd rather make this list when needed otherwise if we cache these values
    * in the memory we will run out of memory
    *
    * @return previous build test results if the build exists and has a
    *       {@link TestNGBuildAction}, otherwise returns an empty {@link TestResults}
    *       object. <b>Never returns {@code null}.</b>
    */
   public static TestResults getPreviousBuildTestResults(AbstractBuild<?, ?> owner) {
      AbstractBuild<?, ?> previousBuild = owner.getPreviousBuild();
      if (previousBuild != null
               && previousBuild.getAction(TestNGBuildAction.class) != null) {
         return previousBuild.getAction(TestNGBuildAction.class).getResults();
      } else {
         return new TestResults("");
      }
   }

   /**
    * Summarizes the delta in tests and also displays a list of failed/skipped
    * tests and configuration methods.
    *
    * The list is returned as an HTML unordered list.
    *
    * @param action
    * @return
    */
   public static String toSummary(TestNGBuildAction action) {
      int prevFailedTestCount = 0;
      int prevSkippedTestCount = 0;
      int prevFailedConfigurationCount = 0;
      int prevSkippedConfigurationCount = 0;
      int prevTotalTestCount = 0;

      AbstractBuild<?,?> owner = action.getBuild();
      TestResults previousResult =
            TestResultHistoryUtil.getPreviousBuildTestResults(owner);

      prevFailedTestCount = previousResult.getFailedTestCount();
      prevSkippedTestCount = previousResult.getSkippedTestCount();
      prevFailedConfigurationCount = previousResult.getFailedConfigCount();
      prevSkippedConfigurationCount = previousResult.getSkippedConfigCount();
      prevTotalTestCount = previousResult.getTotalTestCount();

      TestResults tr = action.getResults();

      return "<ul>" + diff(prevTotalTestCount, tr.getTotalTestCount(), "Total Tests")
            + diff(prevFailedTestCount, tr.getFailedTestCount(), "Failed Tests")
            + printTestsUrls(owner, tr.getFailedTests())
            + diff(prevSkippedTestCount, tr.getSkippedTestCount(), "Skipped Tests")
            + printTestsUrls(owner, tr.getSkippedTests())
            + diff(prevFailedConfigurationCount, tr.getFailedConfigCount(), "Failed Configurations")
            + printTestsUrls(owner, tr.getFailedConfigs())
            + diff(prevSkippedConfigurationCount, tr.getSkippedConfigCount(), "Skipped Configurations")
            + printTestsUrls(owner, tr.getSkippedConfigs())
            + "</ul>";
   }

   private static String diff(long prev, long curr, String name) {
      if (prev <= curr) {
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
   private static String printTestsUrls(AbstractBuild<?,?> owner, List<MethodResult> methodResults) {
      StringBuffer htmlStr = new StringBuffer();
      htmlStr.append("<OL>");
      if (methodResults != null && methodResults.size() > 0) {
         for (MethodResult methodResult : methodResults) {
            htmlStr.append("<LI>");
            if (methodResult.getParent() instanceof ClassResult) {
               // /${it.project.url}${_buildNumber}/${it.urlName}
               htmlStr.append("<a href=\"").append(owner.getUpUrl());
               htmlStr.append(owner.getNumber());
               htmlStr.append("/").append(owner.getProject().getAction(TestNGProjectAction.class).getUrlName());
               htmlStr.append("/").append(methodResult.getFullUrl());
               htmlStr.append("\">");
               htmlStr.append(((ClassResult)methodResult.getParent()).getName());
               htmlStr.append(".").append(methodResult.getName()).append("</a>");
            } else {
               htmlStr.append(methodResult.getName());
            }
            htmlStr.append("</LI>");
         }

      }
      htmlStr.append("</OL>");
      return htmlStr.substring(0);
   }

   //TODO: move getPreviousXXXResults from MethodResult,ClassResult and PackageResult to this class
}
