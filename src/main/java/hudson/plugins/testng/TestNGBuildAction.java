package hudson.plugins.testng;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.plugins.testng.parser.ResultsParser;
import hudson.plugins.testng.results.TestResults;
import hudson.plugins.testng.util.TestResultHistoryUtil;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class TestNGBuildAction implements Action, Serializable {

   /**
    * Unique identifier for this class.
    */
   private static final long serialVersionUID = 31415926L;

   /**
    * The owner of this Action.
    */
   private final AbstractBuild<?, ?> build;

   /**
    * @deprecated since v0.23. Only here for supporting older version of this plug-in
    */
   private transient TestResults results;
   private transient SoftReference<TestResults> testResults;

   public TestNGBuildAction(AbstractBuild<?, ?> build, Collection<TestResults> testngResults) {
      this.build = build;
      TestResults tr = TestResults.total(true, testngResults);
      tr.setOwner(this.build);
      this.testResults = new SoftReference<TestResults>(tr);
   }

   /**
    * Getter for property 'build'.
    *
    * @return Value for property 'build'.
    */
   public synchronized AbstractBuild<?, ?> getBuild() {
      return build;
   }

   /**
    * Override to control when the floating box should be displayed.
    *
    * @return <code>true</code> if the floating box should be visible.
    */
   public boolean isFloatingBoxActive() {
      return true;
   }

   /**
    * Override to control when the action displays a trend graph.
    *
    * @return <code>true</code> if the action should show a trend graph.
    */
   public boolean isGraphActive() {
      return false;
   }

   public TestResults getResults() {
      if (results == null) {
        if (testResults == null) {
           testResults = loadResults(getBuild());
           return testResults.get();
        }

        TestResults tr = testResults.get();
        if (tr == null) {
          testResults = loadResults(getBuild());
          return testResults.get();
        } else {
          return tr;
        }
      } else {
        return results;
      }
   }

   static SoftReference<TestResults> loadResults(AbstractBuild<?, ?> owner)
   {
      ResultsParser parser = new ResultsParser();
      FilePath testngDir = Publisher.getTestNGReport(owner);
      FilePath[] paths = null;
      try {
         paths = testngDir.list("*.xml");
      } catch (Exception e) {
         //do nothing
      }

      TestResults tr = null;
      if (paths == null) {
        tr = new TestResults("");
        tr.setOwner(owner);
        return new SoftReference<TestResults>(tr);
      }

      Collection<TestResults> trList = new ArrayList<TestResults>();
      for (FilePath path : paths) {
         TestResults result = parser.parse(new File(path.getRemote()));
         if (result.getTestList().size() > 0) {
            trList.add(result);
         }
      }
      tr = TestResults.total(true, trList);
      tr.setOwner(owner);
      return new SoftReference<TestResults>(tr);
   }

   public TestResults getPreviousResults() {
      AbstractBuild<?, ?> previousBuild = getBuild().getPreviousBuild();
      while (previousBuild != null && previousBuild.getAction(getClass()) == null) {
         previousBuild = previousBuild.getPreviousBuild();
      }
      if (previousBuild == null) {
         return new TestResults("");
      } else {
         TestNGBuildAction action = previousBuild.getAction(getClass());
         return action.getResults();
      }
   }

   /**
    * The summary of this build report for display on the build index page.
    *
    * @return
    */
   public String getSummary() {
      return TestResultHistoryUtil.toSummary(this);
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
    * {@inheritDoc}
    */
   public String getUrlName() {
      return PluginImpl.URL;
   }

   public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
      return getResults().getDynamic(token, req, rsp);
   }
}
