package hudson.plugins.testng;

import hudson.model.AbstractBuild;
import hudson.model.HealthReportingAction;
import hudson.plugins.helpers.AbstractBuildAction;
import hudson.plugins.helpers.GraphHelper;
import hudson.plugins.testng.results.TestResults;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class AbstractBuildReport<T extends AbstractBuild<?, ?>> extends AbstractBuildAction<T> implements HealthReportingAction {

   private final TestResults results;

   /**
    * Constructs a new AbstractBuildReport.
    * @param results - testng test results
    */
   public AbstractBuildReport(Collection<TestResults> results) {
      TestNGResultAction resultAction =
            new TestNGResultAction(this.getBuild());
      this.results = TestResults.total(results);
      resultAction.setTestNGResults(this.results);
   }

   public TestResults getResults() {
      return results;
   }

   public TestResults getPreviousResults() {
      AbstractBuild<?, ?> prevBuild = getBuild().getPreviousBuild();
      while (prevBuild != null && prevBuild.getAction(getClass()) == null) {
         prevBuild = prevBuild.getPreviousBuild();
      }
      if (prevBuild == null) {
         return new TestResults("");
      } else {
         AbstractBuildReport action = prevBuild.getAction(getClass());
         return action.getResults();
      }
   }

   /**
    * The summary of this build report for display on the build index page.
    *
    * @return
    */
   public String getSummary() {
      AbstractBuild<?, ?> prevBuild = getBuild().getPreviousBuild();
      while (prevBuild != null && prevBuild.getAction(getClass()) == null) {
         prevBuild = prevBuild.getPreviousBuild();
      }
      if (prevBuild == null) {
         return results.toSummary();
      } else {
         AbstractBuildReport action = prevBuild.getAction(getClass());
         return results.toSummary(action.getResults());
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getIconFileName() {
      return PluginImpl.ICON_FILE_NAME;
   }

   /**
    * {@inheritDoc}
    */
   public String getDisplayName() {
      return PluginImpl.DISPLAY_NAME;
   }

   /**
    * Getter for property 'graphName'.
    *
    * @return Value for property 'graphName'.
    */
   public String getGraphName() {
      return PluginImpl.GRAPH_NAME;
   }

   /**
    * {@inheritDoc}
    */
   public String getUrlName() {
      return PluginImpl.URL;
   }

   /**
    *
    */
   /**
    * Generates the graph that shows test pass/fail ratio
    * @param req -
    * @param rsp -
    * @throws IOException -
    */
   public void doGraph(StaplerRequest req,
                       StaplerResponse rsp) throws IOException {
      if (GraphHelper.isGraphUnsupported()) {
         GraphHelper.redirectWhenGraphUnsupported(rsp, req);
         return;
      }

      Calendar t = getBuild().getTimestamp();

      if (req.checkIfModified(t, rsp)) {
         return; // up to date
      }

      DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
            new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

      populateDataSetBuilder(dataSetBuilder);
      ChartUtil.generateGraph(req, rsp, GraphHelper.buildChart(dataSetBuilder.build()), getGraphWidth(), getGraphHeight());
   }


   /**
    * Returns <code>true</code> if there is a graph to plot.
    *
    * @return Value for property 'graphAvailable'.
    */
   public boolean isGraphActive() {
      AbstractBuild<?, ?> build = getBuild();
      // in order to have a graph, we must have at least two points.
      int numPoints = 0;
      while (numPoints < 2) {
         if (build == null) {
            return false;
         }
         if (build.getAction(getClass()) != null) {
            numPoints++;
         }
         build = build.getPreviousBuild();
      }
      return true;
   }

   protected void populateDataSetBuilder(DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataset) {
      for (AbstractBuild<?, ?> build = getBuild(); build != null; build = build.getPreviousBuild()) {
         ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(build);
         AbstractBuildReport action = build.getAction(getClass());
         if (action != null) {
            dataset.add(action.getResults().getPassedTestCount(), "Passed", label);
            dataset.add(action.getResults().getFailedTestCount(), "Failed", label);
            dataset.add(action.getResults().getSkippedTestCount(), "Skipped", label);
         }
      }
   }

   /**
    * Getter for property 'graphWidth'.
    *
    * @return Value for property 'graphWidth'.
    */
   public int getGraphWidth() {
      return 500;
   }

   /**
    * Getter for property 'graphHeight'.
    *
    * @return Value for property 'graphHeight'.
    */
   public int getGraphHeight() {
      return 200;
   }

   public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
      return getResults().getDynamic(token, req, rsp);
   }
}
