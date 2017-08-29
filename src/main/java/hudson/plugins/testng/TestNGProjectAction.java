package hudson.plugins.testng;

import hudson.model.*;
import hudson.tasks.test.TestResultProjectAction;

import jenkins.model.lazy.LazyBuildMixIn;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import hudson.Functions;
import hudson.model.AbstractBuild;
import java.util.Calendar;
import java.util.SortedMap;

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

   public String getChartJson() {
      JSONObject jsonObject = new JSONObject();
      JSONArray passes = new JSONArray();
      JSONArray fails = new JSONArray();
      JSONArray skips = new JSONArray();
      JSONArray buildNum = new JSONArray();
      JSONArray durations = new JSONArray();
      JSONArray buildStatus = new JSONArray();

      int count = 0;

      SortedMap<Integer, Run<?, ?>> loadedBuilds = (SortedMap<Integer, Run<?, ?>>) ((LazyBuildMixIn.LazyLoadingJob<?,?>) job).getLazyBuildMixIn()._getRuns().getLoadedBuilds();
      Run<?, ?> build;
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
