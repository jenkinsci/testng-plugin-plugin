package hudson.plugins.testng.results;

import hudson.model.AbstractBuild;
import hudson.plugins.testng.util.TestResultHistoryUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;

@SuppressWarnings("serial")
public class PackageResult extends BaseResult {

   private List<ClassResult> classList = new ArrayList<ClassResult>();
   private final List<MethodResult> sortedTestMethodsByStartTime = new ArrayList<MethodResult>();

   private transient long duration;
   private transient int fail;
   private transient int skip;
   private transient int total;

   public PackageResult(String name)
   {
      this.name = name;
   }

   public void setOwner(AbstractBuild<?, ?> owner) {
      super.setOwner(owner);
      for (ClassResult _class : classList) {
         _class.setOwner(owner);
      }
   }

   @Exported(name = "classs") // because stapler notices suffix 's' and remove it
   public List<ClassResult> getClassList() {
      return classList;
   }

   public long getDuration() {
      return duration;
   }

   @Exported(visibility = 9)
   public int getFail() {
      return fail;
   }

   @Exported(visibility = 9)
   public int getSkip() {
      return skip;
   }

   @Exported(visibility = 9)
   public int getTotal() {
      return total;
   }

   public long getAge() {
      PackageResult packageResult = getPreviousPackageResult();
      if (packageResult == null) {
         return 1;
      } else {
         return 1 + packageResult.getAge();
      }
   }

   public List<MethodResult> getSortedTestMethodsByStartTime() {
      sortTestMethods();
      return sortedTestMethodsByStartTime;
   }

   public void tally() {
      duration = 0;
      fail = 0;
      skip = 0;
      total = 0;
      for (ClassResult _c : classList) {
          _c.setParent(this);
          _c.tally();
         duration += _c.getDuration();
         fail += _c.getFail();
         skip += _c.getSkip();
         total += _c.getTotal();
      }
   }

   public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
      if (token.equals("/" + getName())) {
         return this;
      }

      ClassResult result = null;
      for (ClassResult _c : classList) {
         if (_c.getName().equals(token)) {
            result = _c;
            break;
         }
      }
      return result;
   }

   public void sortTestMethods() {
      //for each class
      Map<Date, List<MethodResult>> map = new HashMap<Date, List<MethodResult>>();
      for (ClassResult aClass : classList) {
         if (aClass.getTestMethods() != null) {
            for (MethodResult aMethod : aClass.getTestMethods()) {
               if (!aMethod.getStatus().equalsIgnoreCase("skip")) {
                  if (aMethod.getStartedAt() != null) {
                     if (map.containsKey(aMethod.getStartedAt())) {
                        map.get(aMethod.getStartedAt()).add(aMethod);
                     } else {
                        List<MethodResult> list = new ArrayList<MethodResult>();
                        list.add(aMethod);
                        map.put(aMethod.getStartedAt(), list);
                     }
                  }
               }
            }
         }
      }
      List<Date> keys = new ArrayList<Date>(map.keySet());
      Collections.sort(keys);
      //now create the list with the order
      this.sortedTestMethodsByStartTime.clear();
      for (Date key : keys) {
         if (map.containsKey(key)) {
            this.sortedTestMethodsByStartTime.addAll(map.get(key));
         }
      }
   }

   public long getFailedTestsCount() {
      int failedTests = 0;
      for (ClassResult aClass : classList) {
         if (aClass.getTestMethods() != null) {
            for (MethodResult aMethod : aClass.getTestMethods()) {
               if (!aMethod.isConfig() && aMethod.getStatus().equalsIgnoreCase("fail")) {
                  failedTests++;
               }
            }
         }
      }
      return failedTests;
   }

   public long getSkippedTestsCount() {
      int skippedTests = 0;
      for (ClassResult aClass : classList) {
         if (aClass.getTestMethods() != null) {
            for (MethodResult aMethod : aClass.getTestMethods()) {
               if (!aMethod.isConfig() && aMethod.getStatus().equalsIgnoreCase("skip")) {
                  skippedTests++;
               }
            }
         }
      }
      return skippedTests;
   }

   public long getPassedTestsCount() {
      int passTests = 0;
      for (ClassResult aClass : classList) {
         if (aClass.getTestMethods() != null) {
            for (MethodResult aMethod : aClass.getTestMethods()) {
               if (!aMethod.isConfig() && aMethod.getStatus().equalsIgnoreCase("pass")) {
                  passTests++;
               }
            }
         }
      }
      return passTests;
   }

   private PackageResult getPreviousPackageResult() {
      TestResults previousTestResult =
            TestResultHistoryUtil.getPreviousBuildTestResults(getOwner());
      if (previousTestResult != null) {
         Map<String, PackageResult> previousPackageMap = previousTestResult.getPackageMap();
         for (Map.Entry<String, PackageResult> entry : previousPackageMap.entrySet()) {
            if (entry.getKey().equals(this.getName())) {
               return entry.getValue();
            }
         }
      }
      return null;
   }

   public long getFailedTestsDiffCount() {
      PackageResult prevPackageResult = getPreviousPackageResult();
      if (prevPackageResult != null) {
         return getFailedTestsCount() - prevPackageResult.getFailedTestsCount();
      }
      return 0;
   }

   public long getTotalTestsDiffCount() {
      PackageResult prevPackageResult = getPreviousPackageResult();
      if (prevPackageResult != null) {
         return getTotalTestsCount() - prevPackageResult.getTotalTestsCount();
      }
      return 0;
   }

   public long getSkippedTestsDiffCount() {
      PackageResult prevPackageResult = getPreviousPackageResult();
      if (prevPackageResult != null) {
         return getSkippedTestsCount() - prevPackageResult.getSkippedTestsCount();
      }
      return 0;
   }

   public long getTotalTestsCount() {
      int totalTests = 0;
      for (ClassResult aClass : classList) {
         if (aClass.getTestMethods() != null) {
            for (MethodResult aMethod : aClass.getTestMethods()) {
               if (!aMethod.isConfig()) {
                  totalTests++;
               }
            }
         }
      }
      return totalTests;
   }
}
