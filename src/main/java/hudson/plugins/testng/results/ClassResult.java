package hudson.plugins.testng.results;

import hudson.model.ModelObject;
import hudson.model.AbstractBuild;

import java.util.ArrayList;
import java.util.List;

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
      for (MethodResult _m : testMethodList) {
         _m.setOwner(owner);
      }
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

   public void setTestMethodList(List<MethodResult> testMethodList) {
      this.testMethodList = testMethodList;
   }

   public void tally() {
      duration = 0;
      fail = 0;
      skip = 0;
      total = testMethodList.size();
      for (MethodResult _m : testMethodList) {
         duration += _m.getDuration();
         if ("FAIL".equals(_m.getStatus())) {
            fail++;
         } else {
            if ("SKIP".equals(_m.getStatus())) {
               skip++;
            }
         }
         _m.setParent(this);
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
         for (MethodResult testNGMethod : testMethodList) {
            if (token.equals(testNGMethod.getName())) {
               return testNGMethod;
            }
         }
      }
      return null;
   }

   public List<MethodResult>
   getTestMethods() {
      List<MethodResult> list = new ArrayList<MethodResult>();
      for (MethodResult methodResult : testMethodList) {
         if (!methodResult.isConfig()) {
            list.add(methodResult);
         }
      }
      return list;
   }

   public List<MethodResult>
   getConfigurationMethods() {
      List<MethodResult> list = new ArrayList<MethodResult>();
      for (MethodResult methodResult : testMethodList) {
         if (methodResult.isConfig()) {
            list.add(methodResult);
         }
      }
      return list;
   }
}
