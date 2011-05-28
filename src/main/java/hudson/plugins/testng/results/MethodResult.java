package hudson.plugins.testng.results;

import hudson.model.ModelObject;
import hudson.plugins.testng.ProjectIndividualReport;
import hudson.plugins.testng.util.FormatUtil;
import hudson.plugins.testng.util.TestResultHistoryUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@SuppressWarnings("serial")
public class MethodResult extends BaseResult implements ModelObject {

   private String status;
   private String description;
   private boolean isConfig;
   private long duration;
   private MethodResultException exception;
   private Date startedAt;
   private String testName;
   private List<String> parameters;
   //unique id for this tests's run (helps associate the test method with
   // related configuration methods)
   private String testRunId;
   //unique id for this test method
   private String testUuid;

   public String getTestUuid() {
      return testUuid;
   }

   public void setTestUuid(String testUuid) {
      this.testUuid = testUuid;
   }

   public String getTestRunId() {
      return testRunId;
   }

   public void setTestRunId(String testRunId) {
      this.testRunId = testRunId;
   }

   public Date getStartedAt() {
      return startedAt;
   }

   public void setStartedAt(Date startedAt) {
      this.startedAt = startedAt;
   }

   public String getFullUrl() {
       //let's add the test uuid to this url
      return super.getParent().getParent().getName()
            + "/" + super.getParent().getName() + "/" + getUrl();
   }

   public MethodResultException getException() {
      return exception;
   }

   public void setException(MethodResultException exception) {
      this.exception = exception;
   }

   public String getUrl() {
      return getName() + "--" + this.testUuid;
   }

   public long getDuration() {
      return duration;
   }

   public void setDuration(long duration) {
      this.duration = duration;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public String getDescription() {
      return description;
   }

   public String getDescriptionForDisplay() {
     ProjectIndividualReport projAction
        = super.getOwner().getProject().getAction(ProjectIndividualReport.class);
     if (projAction.getEscapeTestDescp()) {
         return FormatUtil.escapeString(description);
      }
      return description;
   }

   public String getExceptionMessageForDisplay() {
     ProjectIndividualReport projAction
        = super.getOwner().getProject().getAction(ProjectIndividualReport.class);
     if (projAction.getEscapeExceptionMsg()) {
        return FormatUtil.escapeString(this.exception.getMessage());
     }
     return exception.getMessage();
  }

   public void setDescription(String description) {
      this.description = description;
   }

   public boolean isConfig() {
      return isConfig;
   }

   public void setConfig(boolean isConfig) {
      this.isConfig = isConfig;
   }

   public String getDisplayName() {
      return getName();
   }

   public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
      if (token.equals("/" + getName() + "--" + this.testUuid)) {
         return this;
      }
      return null;
   }

   /**
    * Create a list that contains previous builds results for this method
    * <p/>
    * (foreach package in previousbuilds tests results packages)
    *    if package.name matches this method's package name then :
    *       (foreach class in package.classlist)
    *          if class.name matches this method's class name then :
    *             (foreach method in class.methodlist)
    *                if method.name matches this method's name then
    *                   add this method to the return list.
    *
    * @return list of previous builds results for this method
    */
   public List<MethodResult> getPreviousMethodResults() {
      List<MethodResult> methodResults = new ArrayList<MethodResult>();
      List<TestResults> previousTestResults =
            TestResultHistoryUtil.getPreviousBuildTestResults(getOwner());
      if (previousTestResults != null) {
         for (TestResults previousTestResult : previousTestResults) {
            Map<String, PackageResult> previousPackageMap = previousTestResult.getPackageMap();
            //get package name!
            String methodPackageName = getParent().getParent().getName();
            String methodClassName = getParent().getName();

            if (previousPackageMap.containsKey(methodPackageName) &&
                  previousPackageMap.get(methodPackageName).getClassList() != null) {
               List<ClassResult> previousClassResults =
                     previousPackageMap.get(getParent().getName()).getClassList();
               boolean foundMatch = false;
               for (ClassResult previousClassResult : previousClassResults) {
                  if (previousClassResult.getName().equals(methodClassName)) {
                     if (this.isConfig) {
                        if (previousClassResult.getConfigurationMethods() != null) {
                           List<MethodResult> previousMethodResults =
                                 previousClassResult.getConfigurationMethods();
                           for (MethodResult previousMethodResult : previousMethodResults) {
                              if (previousMethodResult.getName().equals(this.getName())) {
                                 //found a match
                                 methodResults.add(previousMethodResult);
                                 foundMatch = true;
                                 break;
                              }
                           }
                        }
                     } else {
                        if (previousClassResult.getTestMethods() != null) {
                           List<MethodResult> previousMethodResults =
                                 previousClassResult.getTestMethods();
                           for (MethodResult previousMethodResult : previousMethodResults) {
                              if (previousMethodResult.getName().equals(this.getName())) {
                                 //found a match
                                 methodResults.add(previousMethodResult);
                                 foundMatch = true;
                                 break;
                              }
                           }
                        }
                     }
                  }
                  if (foundMatch) {
                     break;
                  }
               }
            }
         }
      }
      return methodResults;

   }

   public Object getCssClass() {
      if (this.status != null) {
         if (this.status.equalsIgnoreCase("pass")) {
            return "result-passed";
         } else {
            if (this.status.equalsIgnoreCase("skip")) {
               return "result-skipped";
            } else {
               if (this.status.equalsIgnoreCase("fail")) {
                  return "result-failed";
               }
            }
         }

      }
      return "result-passed";
   }
}
