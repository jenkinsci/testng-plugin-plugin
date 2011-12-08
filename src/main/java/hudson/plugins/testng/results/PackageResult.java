package hudson.plugins.testng.results;

import hudson.model.AbstractBuild;
import hudson.plugins.testng.util.FormatUtil;
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
import org.kohsuke.stapler.bind.JavaScriptMethod;

@SuppressWarnings("serial")
public class PackageResult extends BaseResult {

   private List<ClassResult> classList = new ArrayList<ClassResult>();
   private List<MethodResult> sortedTestMethodsByStartTime = null;

   private transient long duration;
   private transient int fail;
   private transient int skip;
   private transient int total;

   public final int MAX_EXEC_MTHD_LIST_SIZE = 25;

   public PackageResult(String name) {
      super(name);
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

   @Exported
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

   /**
    * Gets the age of a result
    *
    * @return the number of consecutive builds for which we have a result for
    *  this package
    */
   public long getAge() {
      PackageResult packageResult = getPreviousPackageResult();
      if (packageResult == null) {
         return 1;
      } else {
         return 1 + packageResult.getAge();
      }
   }

   /**
    * Gets all the method results related to this package sorted by the time
    * the methods were executed
    *
    * @return
    */
   public List<MethodResult> getSortedTestMethodsByStartTime() {
      if (sortedTestMethodsByStartTime == null) {
         sortTestMethods();
      }
      return sortedTestMethodsByStartTime;
   }

   /**
    * Gets table row representation for all the method results associated with
    * this package (sorted based on start time)
    * @return
    */
   @JavaScriptMethod
   public String getAllSortedTestMethodsByStartTime() {
      return getMethodExecutionTableContent(getSortedTestMethodsByStartTime());
   }

   /**
    * Gets table row representation for the first {@link #MAX_EXEC_MTHD_LIST_SIZE}
    * method results associated with this package (sorted based on start time)
    *
    * @return
    */
   @JavaScriptMethod
   public String getFirstXSortedTestMethodsByStartTime() {
      //returning the first MAX results only
      List<MethodResult> list = getSortedTestMethodsByStartTime();
      list = list.subList(0, list.size() > MAX_EXEC_MTHD_LIST_SIZE
               ? MAX_EXEC_MTHD_LIST_SIZE : list.size());
      return getMethodExecutionTableContent(list);
   }

   /**
    * Gets the table row representation for the specified method results
    *
    * @param mrList list of method result objects
    * @return table row representation
    */
   private String getMethodExecutionTableContent(List<MethodResult> mrList) {
      StringBuffer sb = new StringBuffer(mrList.size() * 100);

      for (MethodResult mr : mrList) {
         sb.append("<tr><td align=\"left\">");
         sb.append("<a href=\"../").append(mr.getFullUrl()).append("\">");
         sb.append(mr.getParent().getName()).append(".").append(mr.getName());
         sb.append("</a>");
         sb.append("</td><td align=\"center\">");
         sb.append(FormatUtil.formatTimeInMilliSeconds(mr.getDuration()));
         sb.append("</td><td align=\"center\">");
         sb.append(mr.getStartedAt());
         sb.append("</td><td align=\"center\"><span class=\"").append(mr.getCssClass()).append("\">");
         sb.append(mr.getStatus());
         sb.append("</span></td></tr>");
      }
      return sb.toString();
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

   /**
    * Sorts the test method results associated with this package based on the
    * start time for method execution
    */
   public void sortTestMethods() {
      this.sortedTestMethodsByStartTime = new ArrayList<MethodResult>();
      //for each class
      Map<Date, List<MethodResult>> map = new HashMap<Date, List<MethodResult>>();
      for (ClassResult aClass : classList) {
         if (aClass.getTestMethods() != null) {
            for (MethodResult aMethod : aClass.getTestMethods()) {
               Date startDate = aMethod.getStartedAt();
               if (!aMethod.getStatus().equalsIgnoreCase("skip")
                        && startDate != null) {
                  if (map.containsKey(startDate)) {
                     map.get(startDate).add(aMethod);
                  } else {
                     List<MethodResult> list = new ArrayList<MethodResult>();
                     list.add(aMethod);
                     map.put(startDate, list);
                  }
               }
            }
         }
      }
      List<Date> keys = new ArrayList<Date>(map.keySet());
      Collections.sort(keys);
      //now create the list with the order
      for (Date key : keys) {
         if (map.containsKey(key)) {
            this.sortedTestMethodsByStartTime.addAll(map.get(key));
         }
      }
   }

   private PackageResult getPreviousPackageResult() {
      TestResults previousTestResult =
            TestResultHistoryUtil.getPreviousBuildTestResults(getOwner());

      Map<String, PackageResult> previousPackageMap = previousTestResult.getPackageMap();
      for (Map.Entry<String, PackageResult> entry : previousPackageMap.entrySet()) {
         if (entry.getKey().equals(this.getName())) {
            return entry.getValue();
         }
      }
      return null;
   }

   public long getFailedTestsDiffCount() {
      PackageResult prevPackageResult = getPreviousPackageResult();
      if (prevPackageResult != null) {
         return fail - prevPackageResult.getFail();
      }
      return 0;
   }

   public long getTotalTestsDiffCount() {
      PackageResult prevPackageResult = getPreviousPackageResult();
      if (prevPackageResult != null) {
         return total - prevPackageResult.getTotal();
      }
      return 0;
   }

   public long getSkippedTestsDiffCount() {
      PackageResult prevPackageResult = getPreviousPackageResult();
      if (prevPackageResult != null) {
         return skip - prevPackageResult.getSkip();
      }
      return 0;
   }

}
