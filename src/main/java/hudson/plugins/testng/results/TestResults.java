package hudson.plugins.testng.results;

import hudson.model.AbstractBuild;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hudson.plugins.testng.TestNGProjectAction;
import hudson.plugins.testng.util.TestResultHistoryUtil;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Represents all the results gathered for a single build (or a single suite,
 * while parsing the test results)
 *
 * @author nullin
 * @author farshidce
 */
public class TestResults extends BaseResult implements Serializable {

   private static final long serialVersionUID = -3491974223665601995L;
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

   public TestResults(String name) {
      this.name = name;
   }

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

   public List<TestResult> getTestList() {
      return testList;
   }

   /**
    * Adds only the <test>s that already aren't part of the list
    * @param classList
    */
   public void addTestList(List<TestResult> testList) {
      Set<TestResult> tmpSet = new HashSet<TestResult>(this.testList);
      tmpSet.addAll(testList);
      this.testList = new ArrayList<TestResult>(tmpSet);
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

   public static TestResults total(boolean tally, Collection<TestResults> results) {
      TestResults totalTestResults = new TestResults("");
      for (TestResults individual : results) {
         totalTestResults.add(individual);
      }
      if (tally) {
         totalTestResults.tally();
      }
      return totalTestResults;
   }

   public void add(TestResults r) {
      testList.addAll(r.getTestList());
      failedConfigurationMethods.addAll(r.getFailedConfigurationMethods());
      skippedConfigurationMethods.addAll(r.getSkippedConfigurationMethods());
      failedTests.addAll(r.getFailedTests());
      passedTests.addAll(r.getPassedTests());
      skippedTests.addAll(r.getSkippedTests());
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

   //FIXME: seems screwed up
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      TestResults testResults = (TestResults) o;
      return name.equals(testResults.name) && !(owner != null ? !owner.equals(testResults.owner)
            : testResults.owner != null);
   }

   public int hashCode() {
      int result;
      result = (owner != null ? owner.hashCode() : 0);
      result = 31 * result + name.hashCode();
      return result;
   }

   public String toString() {
      return String.format("TestResults {name='%s', totalTests=%d, " +
          "failedTests=%d, skippedTests=%d, failedConfigs=%d, " +
          "skippedConfigs=%d}", name, totalTestCount, failedTestCount,
          skippedTestCount, failedConfigurationMethodsCount,
          skippedConfigurationMethodsCount);
   }

   public String toSummary() {
      int prevFailedTestCount = 0;
      int prevSkippedTestCount = 0;
      int prevFailedConfigurationCount = 0;
      int prevSkippedConfigurationCount = 0;
      int prevTotalTestCount = 0;
      List<TestResults> prevTestResults =
            TestResultHistoryUtil.getPreviousBuildTestResults(getOwner());

      if (prevTestResults != null && prevTestResults.size() > 0) {
         TestResults previousResult = prevTestResults.get(0);
         prevFailedTestCount = previousResult.getFailedTestCount();
         prevSkippedTestCount = previousResult.getSkippedTestCount();
         prevFailedConfigurationCount = previousResult.getFailedConfigurationMethodsCount();
         prevSkippedConfigurationCount = previousResult.getSkippedConfigurationMethodsCount();
         prevTotalTestCount = previousResult.getTotalTestCount();
      }

      return "<ul>" + diff(prevTotalTestCount, totalTestCount, "Total Tests")
            + diff(prevFailedTestCount, failedTestCount, "Failed Tests")
            + printTestsUrls(getFailedTests())
            + diff(prevSkippedTestCount, skippedTestCount, "Skipped Tests")
            + printTestsUrls(getSkippedTests())
            + diff(prevFailedConfigurationCount, failedConfigurationMethodsCount, "Failed Configurations")
            + printTestsUrls(getFailedConfigurationMethods())
            + diff(prevSkippedConfigurationCount, skippedConfigurationMethodsCount, "Skipped Configurations")
            + printTestsUrls(getSkippedConfigurationMethods())
            + "</ul>";
   }

   private String diff(long prev, long curr, String name) {
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
      StringBuffer htmlStr = new StringBuffer();
      htmlStr.append("<OL>");
      if (methodResults != null && methodResults.size() > 0) {
         for (MethodResult methodResult : methodResults) {
            htmlStr.append("<LI>");
            if (methodResult.getParent() instanceof ClassResult) {
               // /${it.project.url}${_buildNumber}/${it.urlName}
               htmlStr.append("<a href=\"").append(getOwner().getUpUrl());
               htmlStr.append(getOwner().getNumber());
               htmlStr.append("/").append(getOwner().getProject().getAction(TestNGProjectAction.class).getUrlName());
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
               List<ClassResult> classResults = packageMap.get(pkg).getClassList();
               boolean classAlreadyAddedToPackage = false;
               for (ClassResult classResult : classResults) {
                  if (classResult.getName().equals(_class.getName())) {
                     //let's merge the testMethods
                     //loop through and dont add them if the name ,  startTime , endTime and
                     //other fields are identical
                     List<MethodResult> methods = classResult.getTestMethods();
                     List<MethodResult> _methods = _class.getTestMethodList();
                     for (MethodResult _method : _methods) {
                        boolean _methodAlreadyAdded = false;
                        for (MethodResult method : methods) {
                           if(_method.getName().equals(method.getName()) &&
                                _method.getDuration() == method.getDuration() &&
                                   _method.getStartedAt().equals(method.getStartedAt())) {
                              _methodAlreadyAdded = true;
                              break;
                           }
                        }
                        if(!_methodAlreadyAdded) {
                           classResult.addTestMethod(_method);
                        }
                     }
                     classAlreadyAddedToPackage = true;
                     break;
                  }
               }
               if (!classAlreadyAddedToPackage) {
                  packageMap.get(pkg).getClassList().add(_class);
               }
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
