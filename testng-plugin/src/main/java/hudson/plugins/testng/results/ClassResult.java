package hudson.plugins.testng.results;

import hudson.model.ModelObject;
import hudson.model.AbstractBuild;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hudson.plugins.helpers.AbstractBuildAction;
import hudson.plugins.testng.util.TestResultHistoryUtil;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class ClassResult extends BaseResult implements ModelObject {
   private List<MethodResult> testMethodList = new ArrayList<MethodResult>();
   private long duration;
   private int fail;
   private int skip;
   private int total;

   public String getUrl() {
      return getName();
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

   public void setDuration(long duration) {
      this.duration = duration;
   }

   public int getFail() {
      return this.fail;
   }

   public void setFail(int fail) {
      this.fail = fail;
   }

   public int getSkip() {
      return skip;
   }

   public void setSkip(int skip) {
      this.skip = skip;
   }

   public int getTotal() {
      return total;
   }

   public void setTotal(int total) {
      this.total = total;
   }

   public void setTestMethodList(List<MethodResult> testMethodList) {
      this.testMethodList = testMethodList;
   }

   public long
   getFailedTestsDiffCount() {
      long diff = 0;
      List<ClassResult> previousClassResults = getPreviousClassResults();
      if (previousClassResults != null && previousClassResults.size() > 0) {
         diff = getFail() - previousClassResults.get(0).getFail();
      }
      return diff;
   }

   public long
   getSkippedTestsDiffCount() {
      long diff = 0;
      List<ClassResult> previousClassResults = getPreviousClassResults();
      if (previousClassResults != null && previousClassResults.size() > 0) {
         diff = getSkip() - previousClassResults.get(0).getSkip();
      }
      return diff;
   }

   public long
   getTotalTestsDiffCount() {
      long diff = 0;
      List<ClassResult> previousClassResults = getPreviousClassResults();
      if (previousClassResults != null && previousClassResults.size() > 0) {
         diff = getTotal() - previousClassResults.get(0).getTotal();
      }
      return diff;
   }

   public long getAge() {
      List<ClassResult> previousClassResults = getPreviousClassResults();
      if (previousClassResults == null) {
         return 1;
      } else {
         return 1 + previousClassResults.size();
      }
   }


   public void tally() {
      this.duration = 0;
      this.fail = 0;
      this.skip = 0;
      this.total = 0;
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
      }
   }

   public String getDisplayName() {
      return getName();
   }


   public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
      if (token.equals("/" + getName())) {
         return this;
      }
      if (this.testMethodList != null) {
         for (MethodResult methodResult : this.testMethodList) {
            if (token.equals(methodResult.getName())) {
               return methodResult;
            }
         }
      }
      return null;
   }

   public List<MethodResult>
   getTestMethods() {
      List<MethodResult> list = new ArrayList<MethodResult>();
      for (MethodResult methodResult : this.testMethodList) {
         if (!methodResult.isConfig()) {
            list.add(methodResult);
         }
      }
      return list;
   }

   public List<MethodResult>
   getConfigurationMethods() {
      List<MethodResult> list = new ArrayList<MethodResult>();
      for (MethodResult methodResult : this.testMethodList) {
         if (methodResult.isConfig()) {
            list.add(methodResult);
         }
      }
      return list;
   }

   /**
    * Create a list that contains previous builds results for this class
    *
    * (foreach package in previousbuilds tests results packages)
    *  if package.name matches this method's package name then :
    *    (foreach class in package.classlist)
    *       if class.name matches this method's class name then add this class
    *         to the return list
    *
    * @return list of previous builds results for this class
    */
   public List<ClassResult>
   getPreviousClassResults() {
      List<ClassResult> classResults = new ArrayList<ClassResult>();
      List<TestResults> previousTestResults =
            TestResultHistoryUtil.getPreviousBuildTestResults(getOwner());
      if (previousTestResults != null) {
         for (TestResults previousTestResult : previousTestResults) {
            Map<String, PackageResult> previousPackageMap = previousTestResult.getPackageMap();
            //get package name!
            String classPackageName = getParent().getName();
            if (previousPackageMap.containsKey(classPackageName) &&
                  previousPackageMap.get(classPackageName).getClassList() != null) {
               List<ClassResult> previousClassResults =
                     previousPackageMap.get(getParent().getName()).getClassList();
               for (ClassResult previousClassResult : previousClassResults) {
                  if (previousClassResult.getName().equals(this.getName())) {
                     classResults.add(previousClassResult);
                     break;
                  }
               }
            }
         }
      }
      return classResults;

   }

}
