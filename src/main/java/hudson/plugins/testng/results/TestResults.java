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

   public static Collection<TestResults> merge(Collection<TestResults>... results) {
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
      if (!name.equals(statistic.name)) {
         return false;
      }
      if (owner != null ? !owner.equals(statistic.owner)
            : statistic.owner != null) {
         return false;
      }
      return true;
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
      return "<ul>" + diff(0, totalTestCount, "Total Tests")
            + diff(0, failedTestCount, "Failed Tests")
            + diff(0, skippedTestCount, "Skipped Tests")
            + diff(0, failedConfigurationMethodsCount, "Failed Configurations")
            + diff(0, skippedConfigurationMethodsCount, "Skipped Configurations") + "</ul>";
   }

   private static String diff(long prev, long curr, String name) {
      if (prev <= curr) {
         return "<li>" + name + ": " + curr + " (+" + (curr - prev) + ")</li>";
      } else { // if (a < b)
         return "<li>" + name + ": " + curr + " (-" + (prev - curr) + ")</li>";
      }
   }

   public String toSummary(TestResults totals) {
      return "<ul>"
            + diff(totals.getTotalTestCount(), totalTestCount, "Total Tests")
            + diff(totals.getFailedTestCount(), failedTestCount,
            "Failed Tests")
            + diff(totals.getSkippedTestCount(), skippedTestCount,
            "Skipped Tests")
            + diff(totals.getFailedConfigurationMethodsCount(), failedConfigurationMethodsCount,
            "Failed Configurations")
            + diff(totals.getSkippedConfigurationMethodsCount(), skippedConfigurationMethodsCount,
            "Skipped Configurations") + "</ul>";
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
