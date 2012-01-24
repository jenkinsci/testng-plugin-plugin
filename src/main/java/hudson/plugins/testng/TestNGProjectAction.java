package hudson.plugins.testng;

import hudson.model.ProminentProjectAction;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.testng.util.GraphHelper;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Action to associate the TestNG reports with the project
 *
 * @author nullin
 */
public class TestNGProjectAction implements ProminentProjectAction {

   private boolean escapeTestDescp;
   private boolean escapeExceptionMsg;

   /**
    * Used to figure out if we need to regenerate the graphs or not.
    * Only used in newGraphNotNeeded() method. Key is the request URI and value
    * is the number of builds for the project.
    */
   private transient Map<String, Integer> requestMap = new HashMap<String, Integer>();

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
      if (newGraphNotNeeded(req, rsp)) {
        return;
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

   /**
    * If number of builds hasn't changed and if checkIfModified() returns true,
    * no need to regenerate the graph. Browser should reuse it's cached image
    *
    * @param req
    * @param rsp
    * @return true, if new image does NOT need to be generated, false otherwise
    */
   private boolean newGraphNotNeeded(final StaplerRequest req,
         StaplerResponse rsp) {
      Calendar t = getProject().getLastCompletedBuild().getTimestamp();
      Integer prevNumBuilds = requestMap.get(req.getRequestURI());
      int numBuilds = getProject().getBuilds().size();

      //change null to 0
      prevNumBuilds = prevNumBuilds == null ? 0 : prevNumBuilds;
      if (prevNumBuilds != numBuilds) {
        requestMap.put(req.getRequestURI(), numBuilds);
      }

      if (requestMap.size() > 10) {
        //keep map size in check
        requestMap.clear();
      }

      if (prevNumBuilds == numBuilds && req.checkIfModified(t, rsp)) {
         /*
          * checkIfModified() is after '&&' because we want it evaluated only
          * if number of builds is different
          */
         return true;
      }

      return false;
   }

  public void doGraphMap(final StaplerRequest req,
           StaplerResponse rsp) throws IOException {
      if (newGraphNotNeeded(req, rsp)) {
         return;
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

   public TestNGBuildAction getLastCompletedBuildAction() {
      for (AbstractBuild<?, ?> build = getProject().getLastCompletedBuild();
               build != null; build = build.getPreviousBuild()) {
         final TestNGBuildAction action = build.getAction(getBuildActionClass());
         if (action != null) {
            return action;
         }
      }
      return null;
   }

   protected void populateDataSetBuilder(DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataset) {

      for (AbstractBuild<?, ?> build = getProject().getLastBuild();
               build != null; build = build.getPreviousBuild()) {
         ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(build);
         TestNGBuildAction action = build.getAction(getBuildActionClass());
         if (action != null) {
            dataset.add(action.getPassedTestCount(), "Passed", label);
            dataset.add(action.getFailedTestCount(), "Failed", label);
            dataset.add(action.getSkippedTestCount(), "Skipped", label);
         } else {
            //even if testng plugin wasn't run with this build,
            //we should add this build to the graph
            dataset.add(0, "Passed", label);
            dataset.add(0, "Failed", label);
            dataset.add(0, "Skipped", label);
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
