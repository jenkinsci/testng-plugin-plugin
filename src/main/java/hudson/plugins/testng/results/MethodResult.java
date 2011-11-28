package hudson.plugins.testng.results;

import hudson.model.AbstractBuild;
import hudson.plugins.testng.TestNGBuildAction;
import hudson.plugins.testng.TestNGProjectAction;
import hudson.plugins.testng.util.FormatUtil;
import hudson.plugins.testng.util.GraphHelper;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
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
            Date startedAt,
            String isConfig,
            String testRunId,
            String parentTestName,
            String parentSuiteName,
            String testInstanceName)
   {
      super(name);
      this.status = status;
      this.description = description;
      // this uuid is used later to group the tests and config-methods together
      this.testRunId = testRunId;
      this.testInstanceName = testInstanceName;
      this.parentTestName = parentTestName;
      this.parentSuiteName = parentSuiteName;
      this.startedAt = startedAt;

      try {
         this.duration = Long.parseLong(duration);
      } catch (NumberFormatException e) {
         System.err.println("Unable to parse duration value: " + duration);
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
      // package + classname + method url
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
   }

   /**
    * Used on jelly page to display the proper (un)escaped version of description
    * @return
    */
   public String getDisplayDescription() {
     TestNGProjectAction projAction
        = super.getOwner().getProject().getAction(TestNGProjectAction.class);
     if (projAction.getEscapeTestDescp()) {
         return FormatUtil.escapeString(description);
      }
      return description;
   }

   /**
    * Used on jelly page to display the proper (un)escaped version of excp msg
    * @return
    */
   public String getDisplayExceptionMessage() {
     TestNGProjectAction projAction
        = super.getOwner().getProject().getAction(TestNGProjectAction.class);
     if (projAction.getEscapeExceptionMsg()) {
        return FormatUtil.escapeString(this.exception.getMessage());
     }
     return exception.getMessage();
   }

   /**
    * Used on jelly page to display duration in human readable form
    * @return
    */
   public String getDisplayDuration() {
      return FormatUtil.formatTimeInMilliSeconds(duration);
   }

   /**
    * Used on jelly page to display comma separate list of groups
    * @return
    */
   public String getDisplayGroups() {
      if (groups != null && !groups.isEmpty()) {
         return StringUtils.join(groups, ", ");
      }
      return "";
   }

   public boolean isConfig() {
      return isConfig;
   }

   /**
    * Creates test method execution history graph
    * @param req
    * @param rsp
    * @throws IOException
    */
   public void doGraph(final StaplerRequest req, StaplerResponse rsp) throws IOException {
      Graph g = getGraph(req, rsp);
      if(g != null) {
         g.doPng(req, rsp);
      }
   }

   /**
    * Creates map to make the graph clickable
    * @param req
    * @param rsp
    * @throws IOException
    */
   public void doGraphMap(final StaplerRequest req, StaplerResponse rsp) throws IOException {
      Graph g = getGraph(req, rsp);
      if(g != null) {
         g.doMap(req,rsp);
      }
   }

   /**
    * Returns graph instance if needed
    * @param req
    * @param rsp
    * @return
    */
   private hudson.util.Graph getGraph(final StaplerRequest req, StaplerResponse rsp) {
      Calendar t = getOwner().getProject().getLastCompletedBuild().getTimestamp();
      if (req.checkIfModified(t, rsp)) {
         /*
          * checkIfModified() is after '&&' because we want it evaluated only
          * if number of builds is different
          */
         return null;
      }

      final DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
         new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
      final Map<ChartUtil.NumberOnlyBuildLabel, String> statusMap =
         new HashMap<ChartUtil.NumberOnlyBuildLabel, String>();

      populateDataSetBuilder(dataSetBuilder, statusMap);
      return new Graph(-1, 800, 150) {
         protected JFreeChart createGraph() {
            return GraphHelper.createMethodChart(req, dataSetBuilder.build(), statusMap,
                     getFullUrl());
         }
      };
   }

   /**
    * Populates the data set build with results from any successive and at max 9
    * previous builds.
    *
    * @param dataSetBuilder the data set
    * @param statusMap key as build and value as the execution status (result) of
    *                   test method execution
    */
   private void populateDataSetBuilder(
            DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder,
            Map<ChartUtil.NumberOnlyBuildLabel, String> statusMap)
   {
      int count = 0;
      for (AbstractBuild<?, ?> build = getOwner(); build != null; build = build.getNextBuild()) {
         addData(dataSetBuilder, statusMap, build);
      }
      for (AbstractBuild<?, ?> build = getOwner();
            build != null && count++ < 10; build = build.getPreviousBuild()) {
         addData(dataSetBuilder, statusMap, build);
      }
   }

   private void addData(DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder,
            Map<ChartUtil.NumberOnlyBuildLabel, String> statusMap,
            AbstractBuild<?, ?> build)
   {
      ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(build);
      TestNGBuildAction action = build.getAction(TestNGBuildAction.class);
      TestResults results = null;
      MethodResult methodResult = null;
      if (action != null && (results = action.getResults()) != null) {
         methodResult = getMethodResult(results);
      }

      if (methodResult == null) {
         dataSetBuilder.add(0, "resultRow", label);
         statusMap.put(label, "UNKNOWN");
      } else {
         //status is PASS, FAIL or SKIP
         //duration in seconds
         dataSetBuilder.add(methodResult.getDuration() / 1000, "resultRow", label);
         statusMap.put(label, methodResult.getStatus());
      }
   }

   /**
    * Gets the method result, if any, from the given set of test results. Searches
    * for method result that matches the url of this method
    * @param results
    * @return
    */
   private MethodResult getMethodResult(TestResults results) {
      Map<String, PackageResult> packageMap = results.getPackageMap();
      //get package name!
      String methodPackageName = getParent().getParent().getName();
      String methodClassName = getParent().getName();

      if (packageMap.containsKey(methodPackageName) &&
            packageMap.get(methodPackageName).getClassList() != null) {
         List<ClassResult> classResults =
               packageMap.get(methodPackageName).getClassList();
         for (ClassResult classResult : classResults) {
            if (classResult.getName().equals(methodClassName)) {
               List<MethodResult> methodResults = null;
               if (this.isConfig) {
                  methodResults = classResult.getConfigurationMethods();
               } else {
                  methodResults = classResult.getTestMethods();
               }

               if (methodResults != null) {
                  for (MethodResult methodResult : methodResults) {
                     if (methodResult.getUrl().equals(this.getUrl())) {
                        return methodResult;
                     }
                  }
               }
            }
         }
      }
      return null;
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
