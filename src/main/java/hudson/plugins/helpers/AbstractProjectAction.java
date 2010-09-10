package hudson.plugins.helpers;

import hudson.model.ProminentProjectAction;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.testng.PluginImpl;
import hudson.plugins.testng.results.TestResults;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;

import java.io.IOException;
import java.util.Calendar;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

abstract public class AbstractProjectAction<PROJECT extends AbstractProject<?, ?>> implements ProminentProjectAction {
   /**
    * The owner of this action.
    */
   private final PROJECT project;

   protected AbstractProjectAction(PROJECT project) {
      this.project = project;
   }

   /**
    * Getter for property 'project'.
    *
    * @return Value for property 'project'.
    */
   public PROJECT getProject() {
      return project;
   }

   /**
    * {@inheritDoc}
    */
   public String getIconFileName() {
      for (AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild()) {
         final AbstractBuildAction action = build.getAction(getBuildActionClass());
         if (action != null) {
            return PluginImpl.ICON_FILE_NAME;
         }
      }
      return null;
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
   public void doGraph(StaplerRequest req,
                       StaplerResponse rsp) throws IOException {
      if (GraphHelper.isGraphUnsupported()) {
         GraphHelper.redirectWhenGraphUnsupported(rsp, req);
         return;
      }

      Calendar t = getProject().getLastCompletedBuild().getTimestamp();

      if (req.checkIfModified(t, rsp)) {
         return; // up to date
      }

      DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
            new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

      populateDataSetBuilder(dataSetBuilder);
      ChartUtil.generateGraph(req, rsp, GraphHelper.createChart(req, dataSetBuilder.build()),
               getGraphWidth(), getGraphHeight());
   }

   public void doGraphMap(StaplerRequest req,
            StaplerResponse rsp) throws IOException {
      Calendar t = getProject().getLastCompletedBuild().getTimestamp();
      if (req.checkIfModified(t, rsp)) {
         return; // up to date
      }

      DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
       new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

      //TODO: optimize by using cache
      populateDataSetBuilder(dataSetBuilder);
      ChartUtil.generateClickableMap(req, rsp, GraphHelper.createChart(req, dataSetBuilder.build()),
               getGraphWidth(), getGraphHeight());
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
         final AbstractBuildAction action = build.getAction(getBuildActionClass());
         if (action != null) {
            return action.getResults();
         }
      }
      return null;
   }

   public AbstractBuildAction getLastCompletedBuildAction() {
      for (AbstractBuild<?, ?> build = getProject().getLastCompletedBuild(); build != null; build = build.getPreviousBuild()) {
         final AbstractBuildAction action = build.getAction(getBuildActionClass());
         if (action != null) {
            return action;
         }
      }
      return null;
   }

   protected abstract Class<? extends AbstractBuildAction> getBuildActionClass();

   protected void populateDataSetBuilder(DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataset) {

      for (AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild()) {
         ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(build);
         AbstractBuildAction action = build.getAction(getBuildActionClass());
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
