package hudson.plugins.testng.results;

import hudson.model.AbstractBuild;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hudson.plugins.helpers.AbstractBuildAction;
import hudson.plugins.helpers.AbstractProjectAction;
import hudson.plugins.testng.util.TestResultHistoryUtil;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 25-Feb-2008 21:33:40
 */
public class TestResults extends BaseResult implements Serializable {
   // ------------------------------ FIELDS ------------------------------
   private List<TestResult> testList = new ArrayList<TestResult>();
   private List<MethodResult> passedTests = new ArrayList<MethodResult>();
   private List<MethodResult> failedTests = new ArrayList<MethodResult>();
   private List<MethodResult> skippedTests = new ArrayList<MethodResult>();
   private List<MethodResult> failedConfigurationMethods = new ArrayList<MethodResult>();
   private List<MethodResult> skippedConfigurationMethods = new ArrayList<MethodResult>();
   private int totalTestCount;
   private int passedTestCount;
   private int failedTestCount;
   private int skippedTestCount;
   private int failedConfigurationMethodsCount;
   private int skippedConfigurationMethodsCount;
   private Map<String, PackageResult> packageMap = new HashMap<String, PackageResult>();

   public long getAge() {
      List<TestResults> previousTestResults =
            TestResultHistoryUtil.getPreviousBuildTestResults(getOwner());
      if (previousTestResults == null) {
         return 1;
      } else {
         return 1 + previousTestResults.size();
      }
   }

   public List<MethodResult> getFailedTests() {
      return failedTests;
   }

   public List<MethodResult> getPassedTests() {
      return passedTests;
   }

   public void setPassedTests(List<MethodResult> passedTests) {
      this.passedTests = passedTests;
   }

   public List<MethodResult> getSkippedTests() {
      return skippedTests;
   }

   public List<MethodResult> getFailedConfigurationMethods() {
      return failedConfigurationMethods;
   }

   public List<MethodResult> getSkippedConfigurationMethods() {
      return skippedConfigurationMethods;
   }

   // -------------------------- STATIC METHODS --------------------------

   public List<TestResult> getTestList() {
      return testList;
   }

   public void setTestList(List<TestResult> testList) {
      this.testList = testList;
   }

   public int getTotalTestCount() {
      return totalTestCount;
   }

   public void setTotalTestCount(int totalTestCount) {
      this.totalTestCount = totalTestCount;
   }

   public int getPassedTestCount() {
      return passedTestCount;
   }

   public void setPassedTestCount(int passedTestCount) {
      this.passedTestCount = passedTestCount;
   }

   public int getFailedTestCount() {
      return failedTestCount;
   }

   public void setFailedTestCount(int failedTestCount) {
      this.failedTestCount = failedTestCount;
   }

   public int getSkippedTestCount() {
      return skippedTestCount;
   }

   public void setSkippedTestCount(int skippedTestCount) {
      this.skippedTestCount = skippedTestCount;
   }

   public int getFailedConfigurationMethodsCount() {
      return failedConfigurationMethodsCount;
   }

   public void setFailedConfigurationMethodsCount(int failedConfigurationMethodsCount) {
      this.failedConfigurationMethodsCount = failedConfigurationMethodsCount;
   }

   public int getSkippedConfigurationMethodsCount() {
      return skippedConfigurationMethodsCount;
   }

   public void setSkippedConfigurationMethodsCount(int skippedConfigurationMethodsCount) {
      this.skippedConfigurationMethodsCount = skippedConfigurationMethodsCount;
   }

   public Map<String, PackageResult> getPackageMap() {
      return packageMap;
   }

   public Set<String> getPackageNames() {
      return packageMap.keySet();
   }

   public void setPackageMap(Map<String, PackageResult> packageMap) {
      this.packageMap = packageMap;
   }

   public void setFailedTests(List<MethodResult> failedTests) {
      this.failedTests = failedTests;
   }

   public void setSkippedTests(List<MethodResult> skippedTests) {
      this.skippedTests = skippedTests;
   }

   public void setFailedConfigurationMethods(List<MethodResult> failedConfigurationMethods) {
      this.failedConfigurationMethods = failedConfigurationMethods;
   }

   public void setSkippedConfigurationMethods(List<MethodResult> skippedConfigurationMethods) {
      this.skippedConfigurationMethods = skippedConfigurationMethods;
   }

   public static TestResults total(Collection<TestResults>... results) {
      Collection<TestResults> merged = merge(results);
      TestResults total = new TestResults("");
      for (TestResults individual : merged) {
         total.add(individual, false);
      }
      total.tally();
      return total;
   }

   private void add(TestResults r, boolean tally) {
      testList.addAll(r.getTestList());
      failedConfigurationMethods.addAll(r.getFailedConfigurationMethods());
      skippedConfigurationMethods.addAll(r.getSkippedConfigurationMethods());
      failedTests.addAll(r.getFailedTests());
      passedTests.addAll(r.getPassedTests());
      skippedTests.addAll(r.getSkippedTests());
      if (tally) {
         // save cycles while getting total results
         tally();
      }
   }

   public void add(TestResults r) {
      add(r, true);
   }

   private static Collection<TestResults> merge(Collection<TestResults>... results) {
      Collection<TestResults> newResults = new ArrayList<TestResults>();
      if (results.length == 0) {
         return Collections.emptySet();
      } else {
         if (results.length == 1) {
            return results[0];
         } else {
            List<String> indivNames = new ArrayList<String>();
            for (Collection<TestResults> result : results) {
               for (TestResults individual : result) {
                  if (!indivNames.contains(individual.name)) {
                     indivNames.add(individual.name);
                  }
               }
            }
            for (String indivName : indivNames) {
               TestResults indivStat = new TestResults(indivName);
               for (Collection<TestResults> result : results) {

                  for (TestResults individual : result) {
                     if (indivName.equals(individual.name)) {
                        indivStat.add(individual);
                     }
                  }
               }
               newResults.add(indivStat);
            }
            return newResults;
         }
      }
   }

   // --------------------------- CONSTRUCTORS ---------------------------

   public TestResults(String name) {
      this.name = name;
   }

   public void setOwner(AbstractBuild<?, ?> owner) {
      this.owner = owner;
      for (TestResult _test : testList) {
         _test.setOwner(owner);
      }
      for (String pkg : packageMap.keySet()) {
         packageMap.get(pkg).setOwner(owner);
      }
   }

   // ------------------------ CANONICAL METHODS ------------------------

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      TestResults statistic = (TestResults) o;
      return name.equals(statistic.name) && !(owner != null ? !owner.equals(statistic.owner)
            : statistic.owner != null);
   }

   public int hashCode() {
      int result;
      result = (owner != null ? owner.hashCode() : 0);
      result = 31 * result + name.hashCode();
      return result;
   }

   public String toString() {
      return "TestResults{" + "name='" + name + '\'' + ", totalTests="
            + totalTestCount + ", failedTests=" + failedTestCount
            + ", skippedTests=" + skippedTestCount + ", failedConfigurationMethods="
            + failedConfigurationMethodsCount + ", skippedConfigurationMethods=" + skippedConfigurationMethodsCount + '}';
   }

   public String toSummary() {
      //lets get the previous failed count
      int previouseFailedTestCount = 0;
      int previousSkippedTestCount = 0;
      int previousFailedConfigurationCount = 0;
      int previousSkippedConfigurationCount = 0;
      int previousTotalTestCount = 0;
      List<TestResults> previousTestResults =
            TestResultHistoryUtil.getPreviousBuildTestResults(getOwner());
      if (previousTestResults != null && previousTestResults.size() > 0) {
         TestResults previousResult = previousTestResults.get(0);
         previouseFailedTestCount = previousResult.getFailedTestCount();
         previousSkippedTestCount = previousResult.getSkippedTestCount();
         previousFailedConfigurationCount = previousResult.getFailedConfigurationMethodsCount();
         previousSkippedConfigurationCount = previousResult.getSkippedConfigurationMethodsCount();
         previousTotalTestCount = previousResult.getTotalTestCount();
      }
      return "<ul>" + diff(previousTotalTestCount, totalTestCount, "Total Tests")
            + diff(previouseFailedTestCount, failedTestCount, "Failed Tests")
            + printTestsUrls(getFailedTests())
            + diff(previousSkippedTestCount, skippedTestCount, "Skipped Tests")
            + printTestsUrls(getSkippedTests())
            + diff(previousFailedConfigurationCount, failedConfigurationMethodsCount, "Failed Configurations")
            + printTestsUrls(getFailedConfigurationMethods())
            + diff(previousSkippedConfigurationCount, skippedConfigurationMethodsCount, "Skipped Configurations")
            + printTestsUrls(getSkippedConfigurationMethods())
            + "</ul>";
   }

   private static String diff(long prev, long curr, String name) {
      if (prev <= curr) {
         return "<li>" + name + ": " + curr + " (+" + (curr - prev) + ")</li>";
      } else { // if (a < b)
         return "<li>" + name + ": " + curr + " (-" + (prev - curr) + ")</li>";
      }
   }


   /*<OL start="10">
           <LI><a href="url">test_full_name</a> </LI>
        </OL>
   **/

   public String printTestsUrls(List<MethodResult> methodResults) {
      StringBuffer htmlString = new StringBuffer();
      htmlString.append("<OL>");
      if (methodResults != null && methodResults.size() > 0) {
         for (MethodResult methodResult : methodResults) {
            htmlString.append("<LI>");
            if (methodResult.getParent() instanceof ClassResult) {
               // /${it.project.url}${_buildNumber}/${it.urlName}
               htmlString.append("<a href=\"");
               htmlString.append("/").append(getOwner().getProject().getUrl());
               htmlString.append("/").append(getOwner().getNumber());
               htmlString.append("/").append(getOwner().getProject().getAction(AbstractProjectAction.class).getUrlName());
               htmlString.append("/").append(methodResult.getFullUrl());
               htmlString.append("\">");
               htmlString.append(methodResult.getFullName()).append("</a>");
            } else {
               htmlString.append(methodResult.getFullName());
            }
            htmlString.append("</LI>");
         }

      }
      htmlString.append("</OL>");
      return htmlString.substring(0);
   }

   public void set(TestResults that) {
      this.failedConfigurationMethods = that.getFailedConfigurationMethods();
      this.skippedConfigurationMethods = that.getSkippedConfigurationMethods();
      this.failedTests = that.getFailedTests();
      this.skippedTests = that.getSkippedTests();
      this.passedTests = that.getPassedTests();
      this.testList = that.getTestList();
   }

   /**
    * Updates the calculated fields
    */
   public void tally() {
      failedConfigurationMethodsCount = failedConfigurationMethods.size();
      skippedConfigurationMethodsCount = skippedConfigurationMethods.size();
      failedTestCount = failedTests.size();
      passedTestCount = passedTests.size();
      skippedTestCount = skippedTests.size();
      totalTestCount = passedTestCount + failedTestCount + skippedTestCount;
      packageMap.clear();
      for (TestResult _test : testList) {
         for (ClassResult _class : _test.getClassList()) {
            String pkg = _class.getName();
            int lastDot = pkg.lastIndexOf('.');
            if (lastDot == -1) {
               pkg = "No Package";
            } else {
               pkg = pkg.substring(0, lastDot);
            }
            if (packageMap.containsKey(pkg)) {
               packageMap.get(pkg).getClassList().add(_class);
            } else {
               PackageResult tpkg = new PackageResult();
               tpkg.setName(pkg);
               tpkg.getClassList().add(_class);
               tpkg.setParent(this);
               packageMap.put(pkg, tpkg);
            }
         }
      }
      for (String pkg : packageMap.keySet()) {
         packageMap.get(pkg).tally();
      }
   }

   public Object getDynamic(String token,
                            StaplerRequest req,
                            StaplerResponse rsp) {
      return packageMap.get(token);
   }
}
