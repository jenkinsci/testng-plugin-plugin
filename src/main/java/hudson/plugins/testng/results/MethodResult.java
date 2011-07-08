package hudson.plugins.testng.results;

import hudson.plugins.testng.TestNGProjectAction;
import hudson.plugins.testng.parser.ResultsParser;
import hudson.plugins.testng.util.FormatUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.export.Exported;

@SuppressWarnings("serial")
public class MethodResult extends BaseResult {

   private String status;
   private String description;
   private boolean isConfig;
   private long duration;
   private MethodResultException exception;
   private Date startedAt;
   private String testInstanceName;
   private String parentTestName;
   private String parentSuiteName;
   private List<String> groups;
   private List<String> parameters;

   /**
    * unique id for this tests's run (helps associate the test method with
    * related configuration methods)
    */
   private String testRunId;
   /**
    * unique id for this test method
    */
   private String testUuid;

   public MethodResult(String name,
            String status,
            String description,
            String duration,
            String startedAt,
            String isConfig,
            String testRunId,
            String parentTestName,
            String parentSuiteName,
            String testInstanceName)
   {
      this.name = name;
      this.status = status;
      this.description = description;
      // this uuid is used later to group the tests and config-methods together
      this.testRunId = testRunId;
      this.testInstanceName = testInstanceName;
      this.parentTestName = parentTestName;
      this.parentSuiteName = parentSuiteName;

      try {
         this.duration = Long.parseLong(duration);
      } catch (NumberFormatException e) {
         System.err.println("Unable to parse duration value: " + duration);
      }

      try {
         this.startedAt = new SimpleDateFormat(ResultsParser.DATE_FORMAT).parse(startedAt);
      } catch (ParseException e) {
         System.err.println("Unable to parse started-at value: " + startedAt);
      }

      if (isConfig != null) {
         /*
          * If is-config attribute is present on test-method,
          * it's always set to true
          */
         this.isConfig = true;
      }
   }

   public String getTestUuid() {
      return testUuid;
   }

   public void setTestUuid(String testUuid) {
      this.testUuid = testUuid;
   }

   public String getTestInstanceName() {
      return testInstanceName;
   }

   public String getParentTestName() {
      return parentTestName;
   }

   public String getParentSuiteName() {
      return parentSuiteName;
   }

   public String getTestRunId() {
      return testRunId;
   }

   @Exported
   public Date getStartedAt() {
      return startedAt;
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
      String url = getName();
      if (this.testUuid != null) {
         url += "_" + this.testUuid;
      }
      return url;
   }

   @Exported
   public long getDuration() {
      return duration;
   }

   @Exported(visibility = 9)
   public String getStatus() {
      return status;
   }

   @Exported
   public String getDescription() {
      return description;
   }

   @Exported
   public List<String> getGroups() {
      return groups;
   }

   @Exported
   public List<String> getParameters() {
      return parameters;
   }

   /**
    * Added only to expose possible exception via  .../api/xxx
    *
    * @return String representation of the exception
    */
   @Exported(name = "exception")
   public String getExceptionString() {
      if (exception != null) {
         return exception.toString();
      }
      return null;
   }

   /**
    * Added only to expose class name as part of method result via  .../api/xxx
    *
    * @return String representation of the exception
    */
   @Exported(name = "className")
   public String getClassName() {
      return getParent().getName();
   }

   public void setGroups(List<String> groups) {
      this.groups = groups;
   }

   public void setParameters(List<String> parameters) {
      this.parameters = parameters;
      if (!parameters.isEmpty()) {
         this.name = this.name + "(" + StringUtils.join(parameters, ", ") + ")";
      }
   }

   public String getDisplayDescription() {
     TestNGProjectAction projAction
        = super.getOwner().getProject().getAction(TestNGProjectAction.class);
     if (projAction.getEscapeTestDescp()) {
         return FormatUtil.escapeString(description);
      }
      return description;
   }

   public String getDisplayExceptionMessage() {
     TestNGProjectAction projAction
        = super.getOwner().getProject().getAction(TestNGProjectAction.class);
     if (projAction.getEscapeExceptionMsg()) {
        return FormatUtil.escapeString(this.exception.getMessage());
     }
     return exception.getMessage();
   }

   public String getDisplayDuration() {
      return FormatUtil.formatTimeInMilliSeconds(duration);
   }

   public String getDisplayGroups() {
      StringBuffer sb = new StringBuffer();

      if (groups != null) {
         int len = groups.size();
         int count = 1;
         for (String grp : groups) {
            sb.append(grp);
            if (count++ < len) {
               sb.append(", ");
            }
         }
      }

      return sb.toString();
   }

   public boolean isConfig() {
      return isConfig;
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
//   public List<MethodResult> getPreviousMethodResults() {
//      List<MethodResult> methodResults = new ArrayList<MethodResult>();
//      List<TestResults> previousTestResults =
//            TestResultHistoryUtil.getAllPreviousBuildTestResults(getOwner());
//      if (previousTestResults != null) {
//         for (TestResults previousTestResult : previousTestResults) {
//            Map<String, PackageResult> previousPackageMap = previousTestResult.getPackageMap();
//            //get package name!
//            String methodPackageName = getParent().getParent().getName();
//            String methodClassName = getParent().getName();
//
//            if (previousPackageMap.containsKey(methodPackageName) &&
//                  previousPackageMap.get(methodPackageName).getClassList() != null) {
//               List<ClassResult> previousClassResults =
//                     previousPackageMap.get(getParent().getName()).getClassList();
//               boolean foundMatch = false;
//               for (ClassResult previousClassResult : previousClassResults) {
//                  if (previousClassResult.getName().equals(methodClassName)) {
//                     if (this.isConfig) {
//                        if (previousClassResult.getConfigurationMethods() != null) {
//                           List<MethodResult> previousMethodResults =
//                                 previousClassResult.getConfigurationMethods();
//                           for (MethodResult previousMethodResult : previousMethodResults) {
//                              if (previousMethodResult.getName().equals(this.getName())) {
//                                 //found a match
//                                 methodResults.add(previousMethodResult);
//                                 foundMatch = true;
//                                 break;
//                              }
//                           }
//                        }
//                     } else {
//                        if (previousClassResult.getTestMethods() != null) {
//                           List<MethodResult> previousMethodResults =
//                                 previousClassResult.getTestMethods();
//                           for (MethodResult previousMethodResult : previousMethodResults) {
//                              if (previousMethodResult.getName().equals(this.getName())) {
//                                 //found a match
//                                 methodResults.add(previousMethodResult);
//                                 foundMatch = true;
//                                 break;
//                              }
//                           }
//                        }
//                     }
//                  }
//                  if (foundMatch) {
//                     break;
//                  }
//               }
//            }
//         }
//      }
//      return methodResults;
//   }

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
