package hudson.plugins.testng;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;
import hudson.model.Result;
import hudson.tasks.test.TestResultProjectAction;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Action to associate the TestNG reports with the project
 *
 * @author nullin
 */
public class TestNGProjectAction extends TestResultProjectAction implements ProminentProjectAction {

   private transient boolean escapeTestDescp;
   private transient boolean escapeExceptionMsg;
   private transient boolean showFailedBuilds;

   public TestNGProjectAction(AbstractProject<?, ?> project,
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
   public AbstractProject<?, ?> getProject() {
      return super.project;
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

   public TestNGTestResultBuildAction getLastCompletedBuildAction() {
      for (AbstractBuild<?, ?> build = getProject().getLastCompletedBuild();
               build != null; build = build.getPreviousCompletedBuild()) {
         final TestNGTestResultBuildAction action = build.getAction(getBuildActionClass());
         if (action != null) {
            return action;
         }
      }
      return null;
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
    * Returns json for charting
    *
    * @return a json for a chart
    */
   public String getChartJson() {
      JSONObject jsonObject = new JSONObject();
      JSONArray passes = new JSONArray();
      JSONArray fails = new JSONArray();
      JSONArray skips = new JSONArray();
      JSONArray buildNum = new JSONArray();
      JSONArray durations = new JSONArray();
      JSONArray buildStatus = new JSONArray();

      int count = 0;

      List<? extends AbstractBuild<?, ?>> loadedBuilds = new ArrayList<AbstractBuild<?, ?>>(getProject()._getRuns().getLoadedBuilds().values());
      AbstractBuild<?, ?> build;
      for (int i = 0; i < loadedBuilds.size() && count++ < 25; i++) {
         build = loadedBuilds.get(i);
         TestNGTestResultBuildAction action = build.getAction(getBuildActionClass());

         if (build.getResult() == null || build.getResult().isWorseThan(Result.FAILURE)) {
            //We don't want to add aborted or builds with no results into the graph
            continue;
         }

         if (action != null) {
            passes.add(action.getTotalCount() - action.getFailCount() - action.getSkipCount());
            fails.add(action.getFailCount());
            skips.add(action.getSkipCount());
            buildNum.add(Integer.toString(build.getNumber()));
            durations.add(build.getDuration());
            buildStatus.add(build.getResult().color);
         }
      }
      jsonObject.put("pass", passes);
      jsonObject.put("fail", fails);
      jsonObject.put("skip", skips);
      jsonObject.put("buildNum", buildNum);
      jsonObject.put("duration", durations);
      jsonObject.put("buildStatus", buildStatus);
      return jsonObject.toString();
   }
}
