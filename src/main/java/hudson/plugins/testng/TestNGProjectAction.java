package hudson.plugins.testng;

import hudson.Functions;
import hudson.model.AbstractBuild;
import java.io.IOException;
import java.util.Calendar;

import hudson.model.Job;
import hudson.model.ProminentProjectAction;
import hudson.model.Result;
import hudson.model.Run;
import hudson.plugins.testng.util.GraphHelper;
import hudson.tasks.test.TestResultProjectAction;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import java.util.SortedMap;
import jenkins.model.lazy.LazyBuildMixIn;
import jenkins.model.lazy.LazyBuildMixIn.LazyLoadingJob;
import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Action to associate the TestNG reports with the project
 *
 * @author nullin
 */
public class TestNGProjectAction extends TestResultProjectAction implements ProminentProjectAction {

   private transient boolean escapeTestDescp;
   private transient boolean escapeExceptionMsg;
   private transient boolean showFailedBuilds;

   public TestNGProjectAction(Job<?, ?> project,
         boolean escapeTestDescp, boolean escapeExceptionMsg, boolean showFailedBuilds) {
      super(project);
      this.escapeExceptionMsg = escapeExceptionMsg;
      this.escapeTestDescp = escapeTestDescp;
      this.showFailedBuilds = showFailedBuilds;
   }

   protected Class<TestNGTestResultBuildAction> getBuildActionClass() {
      return TestNGTestResultBuildAction.class;
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
    * Getter for property 'project'.
    *
    * @return Value for property 'project'.
    */
   public Job<?, ?> getProject() {
      return super.job;
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
   // @org.kohsuke.stapler.verb.POST // POST blocks graph rendering in groovy web page
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

    /** Generalizes {@link AbstractBuild#getUpUrl} to {@link Run}. */
    public String getUpUrl() {
        return Functions.getNearestAncestorUrl(Stapler.getCurrentRequest(), job) + '/';
    }

    /**
    * If the last build is the same,
    * no need to regenerate the graph. Browser should reuse it's cached image
    *
    * @param req request
    * @param rsp response
    * @return true, if new image does NOT need to be generated, false otherwise
    */
   private boolean newGraphNotNeeded(final StaplerRequest req,
         StaplerResponse rsp) {
      Calendar t = getProject().getLastCompletedBuild().getTimestamp();
      return req.checkIfModified(t, rsp);
   }

    // @org.kohsuke.stapler.verb.POST // POST blocks rendering in groovy defined web page
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
      Run<?, ?> build = getProject().getLastBuild();
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

   public TestNGTestResultBuildAction getLastCompletedBuildAction() {
      for (Run<?, ?> build = getProject().getLastCompletedBuild();
               build != null; build = build.getPreviousCompletedBuild()) {
         final TestNGTestResultBuildAction action = build.getAction(getBuildActionClass());
         if (action != null) {
            return action;
         }
      }
      return null;
   }

   protected void populateDataSetBuilder(DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataset) {
      if (!(job instanceof LazyBuildMixIn.LazyLoadingJob)) {
         return;
      }

      // cf. AbstractTestResultAction.getPreviousResult(Class, false)
      SortedMap<Integer, Run<?, ?>> loadedBuilds = (SortedMap<Integer, Run<?, ?>>) ((LazyLoadingJob<?,?>) job).getLazyBuildMixIn()._getRuns().getLoadedBuilds();
      for (Run<?, ?> build : loadedBuilds.values()) {
         ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(build);
         TestNGTestResultBuildAction action = build.getAction(getBuildActionClass());

         Result result = build.getResult();
         if (result == null || result.isWorseThan(Result.FAILURE)) {
            //We don't want to add aborted or builds with no results into the graph
            continue;
         }

         if (!showFailedBuilds && result.equals(Result.FAILURE)) {
            //failed build and configuration states that we should skip this build
            continue;
         }

         if (action != null) {
            dataset.add(action.getTotalCount() - action.getFailCount() - action.getSkipCount(), "Passed", label);
            dataset.add(action.getFailCount(), "Failed", label);
            dataset.add(action.getSkipCount(), "Skipped", label);
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
