package hudson.plugins.testng.results;

import hudson.model.AbstractBuild;
import hudson.plugins.testng.util.TestResultHistoryUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@SuppressWarnings("serial")
public class ClassResult extends BaseResult {

   private List<MethodResult> testMethodList = new ArrayList<MethodResult>();
   private Map<String, GroupedTestRun> testRunMap;

   private long duration;
   private int fail;
   private int skip;
   private int total;

   public ClassResult(String name) {
     this.name = name;
   }

   public Map<String, GroupedTestRun> getTestRunMap() {
      //group all the test methods based on their run
      this.testRunMap = new HashMap<String, GroupedTestRun>();
      for (MethodResult methodResult : this.testMethodList) {
         if (this.testRunMap.containsKey(methodResult.getTestRunId())) {
            GroupedTestRun group = this.testRunMap.get(methodResult.getTestRunId());
            if (methodResult.isConfig()) {
               group.addConfigurationMethod(methodResult);
            } else {
               group.addTestMethod(methodResult);
            }
         } else {
            GroupedTestRun group = new GroupedTestRun();
            group.setTestRunId(methodResult.getTestRunId());
            if (methodResult.isConfig()) {
               group.addConfigurationMethod(methodResult);
            } else {
               group.addTestMethod(methodResult);
            }
            this.testRunMap.put(methodResult.getTestRunId(), group);
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

   public long getDuration() {
      return this.duration;
   }

   public int getFail() {
      return this.fail;
   }

   public int getSkip() {
      return skip;
   }

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
      if (previousTestResults != null) {
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
      }
      return null;
   }
}
