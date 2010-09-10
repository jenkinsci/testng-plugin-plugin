package hudson.plugins.testng.results;

import hudson.model.ModelObject;
import hudson.model.AbstractBuild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class PackageResult extends BaseResult implements ModelObject {
   private List<ClassResult> classList = new ArrayList<ClassResult>();
   private long duration;
   private int fail;
   private int skip;
   private int total;
   private List<MethodResult> sortedTestMethodsByStartTime = new ArrayList<MethodResult>();

   public String getUrl() {
      return getName();
   }

   public void setOwner(AbstractBuild<?, ?> owner) {
      super.setOwner(owner);
      for (ClassResult _class : classList) {
         _class.setOwner(owner);
      }
   }

   public List<ClassResult> getClassList() {
      return classList;
   }

   public void setClassList(List<ClassResult> classList) {
      this.classList = classList;
   }

   public long getDuration() {
      return duration;
   }

   public void setDuration(long duration) {
      this.duration = duration;
   }

   public int getFail() {
      return fail;
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
         duration += _c.getDuration();
         fail += _c.getFail();
         skip += _c.getSkip();
         total += _c.getTotal();
         _c.setParent(this);
         _c.tally();
      }
   }

   public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
//      if (token.equals(getId())) {
//          return this;
//      }
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

   public String getDisplayName() {
      return getName();
   }


   public void sortTestMethods() {
      //for each class
      Map<Date, List<MethodResult>> map = new HashMap<Date, List<MethodResult>>();
      for (ClassResult aClass : classList) {
         if (aClass.getTestMethods() != null) {
            for (MethodResult aMethod : aClass.getTestMethods()) {
               if (!aMethod.getStatus().equals("SKIP")) {
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

   public long
   getFailedTestsCount() {
      int failedTests = 0;
      for (ClassResult aClass : classList) {
         if (aClass.getTestMethods() != null) {
            for (MethodResult aMethod : aClass.getTestMethods()) {
               if (aMethod.getStatus().equals("FAIL")) {
                  failedTests++;
               }
            }
         }
      }
      return failedTests;
   }

   public long
   getSkippedTestsCount() {
      int skippedTests = 0;
      for (ClassResult aClass : classList) {
         if (aClass.getTestMethods() != null) {
            for (MethodResult aMethod : aClass.getTestMethods()) {
               if (!aMethod.isConfig() && aMethod.getStatus().equals("SKIP")) {
                  skippedTests++;
               }
            }
         }
      }
      return skippedTests;
   }

   public long
   getPassedTestsCount() {
      int passTests = 0;
      for (ClassResult aClass : classList) {
         if (aClass.getTestMethods() != null) {
            for (MethodResult aMethod : aClass.getTestMethods()) {
               if (aMethod.getStatus().equals("PASS")) {
                  passTests++;
               }
            }
         }
      }
      return passTests;
   }

   public long
   getTotalTestsCount() {
      int totalTests = 0;
      for (ClassResult aClass : classList) {
         if (aClass.getTestMethods() != null) {
            for (MethodResult aMethod : aClass.getTestMethods()) {
               totalTests++;
            }
         }
      }
      return totalTests;
   }
}
