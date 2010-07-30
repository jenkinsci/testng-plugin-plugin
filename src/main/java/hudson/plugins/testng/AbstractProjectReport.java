package hudson.plugins.testng;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;
import hudson.plugins.helpers.AbstractProjectAction;
import hudson.plugins.helpers.GraphHelper;
import hudson.plugins.testng.results.TestResults;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;

import java.io.IOException;
import java.util.Calendar;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class AbstractProjectReport<T extends AbstractProject<?, ?>> extends AbstractProjectAction<T>
      implements ProminentProjectAction {

   public AbstractProjectReport(T project) {
      super(project);
   }

   /**
    * {@inheritDoc}
    */
   public String getIconFileName() {
      for (AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild()) {
         final AbstractBuildReport action = build.getAction(getBuildActionClass());
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
      for (AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild()) {
         final AbstractBuildReport action = build.getAction(getBuildActionClass());
         if (action != null) {
            //let's get the latest build # of pass/fails
            String displayName = PluginImpl.DISPLAY_NAME + " ";
            if (getProject().getLastBuild() != null) {
               AbstractBuild<?, ?> lastBuild = getProject().getLastBuild();
               AbstractBuildReport lastActions = lastBuild.getAction(getBuildActionClass());
               if (lastActions != null) {
                  TestResults testResults = action.getResults();
                  if (testResults != null) {
                     if (getResults().getPassedTestCount() > 0) {
                        displayName += "<br/>" + getResults().getPassedTestCount()
                              + " Tests Passed ";
                     }
                     if (getResults().getFailedTestCount() > 0) {
                        displayName += "<br/>" + getResults().getFailedTestCount() + " Tests Failed ";
                     }
                     if (getResults().getSkippedTestCount() > 0) {
                        displayName += "<br/>" + getResults().getSkippedTestCount() + " Tests Skipped ";
                     }
                  }
               }
            }
            return displayName;
         }
      }
      return null;
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
      AbstractBuild lastCompletedBuild = getProject().getLastCompletedBuild();
      if (lastCompletedBuild != null) {
         return "lastCompletedBuild" + "/" + PluginImpl.URL;
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public String getSearchUrl() {
      return PluginImpl.URL;
   }

   /**
    * Generates the graph that shows the coverage trend up to this report.
    */
   public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
      if (GraphHelper.isGraphUnsupported()) {
         GraphHelper.redirectWhenGraphUnsupported(rsp, req);
         return;
      }

      Calendar t = getProject().getLastBuild().getTimestamp();

      if (req.checkIfModified(t, rsp)) {
         return; // up to date
      }

      DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
            new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

      populateDataSetBuilder(dataSetBuilder);

      ChartUtil.generateGraph(req, rsp, GraphHelper.buildChart(dataSetBuilder.build()), getGraphWidth(),
            getGraphHeight());
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
    * Returns the latest totals.
    *
    * @return Value for property 'graphAvailable'.
    */
   public TestResults getResults() {
      for (AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild()) {
         final AbstractBuildReport action = build.getAction(getBuildActionClass());
         if (action != null) {
            return action.getResults();
         }
      }
      return null;
   }

   protected abstract Class<? extends AbstractBuildReport> getBuildActionClass();

   protected void populateDataSetBuilder(DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataset) {

      for (AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild()) {
         ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(build);
         AbstractBuildReport action = build.getAction(getBuildActionClass());
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
