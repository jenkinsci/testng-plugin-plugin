package hudson.plugins.testng;

import hudson.model.ProminentProjectAction;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.testng.results.TestResults;
import hudson.plugins.testng.util.GraphHelper;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;

import java.io.IOException;
import java.util.Calendar;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * TODO javadoc.
 *
 */
public class TestNGProjectAction implements ProminentProjectAction {

   private boolean escapeTestDescp;
   private boolean escapeExceptionMsg;

   public TestNGProjectAction(AbstractProject<?, ?> project,
         boolean escapeTestDescp, boolean escapeExceptionMsg) {
      this.project = project;
      this.escapeExceptionMsg = escapeExceptionMsg;
      this.escapeTestDescp = escapeTestDescp;
   }

   protected Class<TestNGBuildAction> getBuildActionClass() {
      return TestNGBuildAction.class;
   }

   public boolean getEscapeTestDescp()
   {
      return escapeTestDescp;
   }

   public boolean getEscapeExceptionMsg()
   {
      return escapeExceptionMsg;
   }

   /**
    * The owner of this action.
    */
   private final AbstractProject<?, ?> project;

   /**
    * Getter for property 'project'.
    *
    * @return Value for property 'project'.
    */
   public AbstractProject<?, ?> getProject() {
      return project;
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
    * {@inheritDoc}
    */
   public String getSearchUrl() {
      return PluginImpl.URL;
   }

   /**
    * Generates the graph that shows test pass/fail ratio
    * @param req -
    * @param rsp -
    * @throws IOException -
    */
   public void doGraph(final StaplerRequest req,
                      StaplerResponse rsp) throws IOException {
      if (GraphHelper.isGraphUnsupported()) {
         GraphHelper.redirectWhenGraphUnsupported(rsp, req);
         return;
      }

      Calendar t = getProject().getLastCompletedBuild().getTimestamp();

      if (req.checkIfModified(t, rsp)) {
         return; // up to date
      }

      final DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
            new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

      populateDataSetBuilder(dataSetBuilder);
      new hudson.util.Graph(-1, getGraphWidth(), getGraphHeight()) {
         protected JFreeChart createGraph() {
            return GraphHelper.createChart(req, dataSetBuilder.build());
         }
      }.doPng(req,rsp);
   }

   public void doGraphMap(final StaplerRequest req,
           StaplerResponse rsp) throws IOException {
      Calendar t = getProject().getLastCompletedBuild().getTimestamp();
      if (req.checkIfModified(t, rsp)) {
         return; // up to date
      }

      final DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
      new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

      //TODO: optimize by using cache
      populateDataSetBuilder(dataSetBuilder);
      new hudson.util.Graph(-1, getGraphWidth(), getGraphHeight()) {
         protected JFreeChart createGraph() {
           return GraphHelper.createChart(req, dataSetBuilder.build());
         }
      }.doMap(req, rsp);
   }

   /**
    * Returns <code>true</code> if there is a graph to plot.
    *
    * @return Value for property 'graphAvailable'.
    */
   public boolean isGraphActive() {
      AbstractBuild<?, ?> build = getProject().getLastBuild();
      // in order to have a graph, we must have at least two points.
      int numPoints = 0;
      while (numPoints < 2) {
         if (build == null) {
            return false;
         }
         if (build.getAction(getBuildActionClass()) != null) {
            numPoints++;
         }
         build = build.getPreviousBuild();
      }
      return true;
   }

   /**
    * Returns the latest test results.
    *
    * @return Value for property 'graphAvailable'.
    */
   public TestResults getResults() {
      for (AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild()) {
         final TestNGBuildAction action = build.getAction(getBuildActionClass());
         if (action != null) {
            return action.getResults();
         }
      }
      return null;
   }

   public TestNGBuildAction getLastCompletedBuildAction() {
      for (AbstractBuild<?, ?> build = getProject().getLastCompletedBuild(); build != null; build = build.getPreviousBuild()) {
         final TestNGBuildAction action = build.getAction(getBuildActionClass());
         if (action != null) {
            return action;
         }
      }
      return null;
   }

   protected void populateDataSetBuilder(DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataset) {

      for (AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild()) {
         ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(build);
         TestNGBuildAction action = build.getAction(getBuildActionClass());
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
}
