package hudson.plugins.testng.results;

import hudson.model.AbstractBuild;
import hudson.plugins.testng.util.TestResultHistoryUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;

@SuppressWarnings("serial")
public class ClassResult extends BaseResult {

   private List<MethodResult> testMethodList = new ArrayList<MethodResult>();
   private Map<String, GroupedTestRun> testRunMap = null;

   private long duration;
   private int fail;
   private int skip;
   private int total;

   public ClassResult(String name) {
      super(name);
   }

   /**
    * Called only from jelly file
    * @return
    */
   public Map<String, GroupedTestRun> getTestRunMap() {
      if (testRunMap != null) {
         return testRunMap;
      }
      //group all the test methods based on their run
      testRunMap = new HashMap<String, GroupedTestRun>();
      for (MethodResult methodResult : this.testMethodList) {
         String methodTestRunId = methodResult.getTestRunId();
         GroupedTestRun group = null;
         if (this.testRunMap.containsKey(methodTestRunId)) {
            group = this.testRunMap.get(methodTestRunId);
         } else {
            group = new GroupedTestRun(methodTestRunId,
                     methodResult.getParentTestName(),
                     methodResult.getParentSuiteName());
            this.testRunMap.put(methodTestRunId, group);
         }

         if (methodResult.isConfig()) {
            group.addConfigurationMethod(methodResult);
         } else {
            group.addTestMethod(methodResult);
         }
      }
      return testRunMap;
   }

   public void setOwner(AbstractBuild<?, ?> owner) {
      super.setOwner(owner);
      for (MethodResult _m : this.testMethodList) {
         _m.setOwner(owner);
      }
   }

   @Exported
   public long getDuration() {
      return this.duration;
   }

   @Exported(visibility = 9)
   public int getFail() {
      return this.fail;
   }

   @Exported(visibility = 9)
   public int getSkip() {
      return skip;
   }

   @Exported(visibility = 9)
   public int getTotal() {
      return total;
   }

   public List<MethodResult> getTestMethodList() {
      return this.testMethodList;
   }

   public void addTestMethods(List<MethodResult> list) {
      this.testMethodList.addAll(list);
   }

   public void addTestMethod(MethodResult testMethod) {
      this.testMethodList.add(testMethod);
   }

   public long getFailedTestsDiffCount() {
      ClassResult prevClassResult = getPreviousClassResult();
      if (prevClassResult != null) {
         return getFail() - prevClassResult.getFail();
      }
      return 0;
   }

   public long getSkippedTestsDiffCount() {
      ClassResult prevClassResult = getPreviousClassResult();
      if (prevClassResult != null) {
         return getSkip() - prevClassResult.getSkip();
      }
      return 0;
   }

   public long getTotalTestsDiffCount() {
      ClassResult prevClassResult = getPreviousClassResult();
      if (prevClassResult != null) {
         return getTotal() - prevClassResult.getTotal();
      }
      return 0;
   }

   public void tally() {
      this.duration = 0;
      this.fail = 0;
      this.skip = 0;
      this.total = 0;
      Map<String, Integer> methodInstanceMap = new HashMap<String, Integer>();
      for (MethodResult methodResult : this.testMethodList) {
         if (!methodResult.isConfig()) {
            this.duration += methodResult.getDuration();
            this.total++;
            if ("FAIL".equals(methodResult.getStatus())) {
               this.fail++;
            } else {
               if ("SKIP".equals(methodResult.getStatus())) {
                  this.skip++;
               }
            }
         }
         methodResult.setParent(this);
         /*
          * Setup testUuids to ensure that methods with same names can be
          * reached using unique urls
          */
         String methodName = methodResult.getName();
         if (methodInstanceMap.containsKey(methodName)) {
            int currIdx = methodInstanceMap.get(methodName);
            methodResult.setTestUuid(String.valueOf(++currIdx));
            methodInstanceMap.put(methodName, currIdx);
         } else {
            methodInstanceMap.put(methodName, 0);
         }
      }
   }

   public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
      if (token.equals("/" + getName())) {
         return this;
      }
      if (this.testMethodList != null) {
         for (MethodResult methodResult : this.testMethodList) {
            //append the uuid as well
            if (token.equals(methodResult.getUrl())) {
               return methodResult;
            }
         }
      }
      return null;
   }

   @Exported(name = "test-method")
   public List<MethodResult> getTestMethods() {
      List<MethodResult> list = new ArrayList<MethodResult>();
      for (MethodResult methodResult : this.testMethodList) {
         if (!methodResult.isConfig()) {
            list.add(methodResult);
         }
      }
      return list;
   }

   public List<MethodResult> getConfigurationMethods() {
      List<MethodResult> list = new ArrayList<MethodResult>();
      for (MethodResult methodResult : this.testMethodList) {
         if (methodResult.isConfig()) {
            list.add(methodResult);
         }
      }
      return list;
   }

   public long getAge() {
      ClassResult prevClassResult = getPreviousClassResult();
      if (prevClassResult == null) {
         return 1;
      } else {
         return 1 + prevClassResult.getAge();
      }
   }

   private ClassResult getPreviousClassResult() {
      TestResults previousTestResults =
            TestResultHistoryUtil.getPreviousBuildTestResults(getOwner());
      Map<String, PackageResult> previousPackageMap = previousTestResults.getPackageMap();
      //get package name!
      String classPackageName = getParent().getName();
      PackageResult packageResult = previousPackageMap.get(classPackageName);
      if (packageResult != null) {
         List<ClassResult> prevClassList = packageResult.getClassList();
         for (ClassResult prevClassResult : prevClassList) {
            if (prevClassResult.getName().equals(this.getName())) {
               return prevClassResult;
            }
         }
      }
      return null;
   }
}
