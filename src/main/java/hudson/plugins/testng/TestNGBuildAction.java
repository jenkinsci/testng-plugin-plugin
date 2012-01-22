package hudson.plugins.testng;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.Api;
import hudson.plugins.testng.parser.ResultsParser;
import hudson.plugins.testng.results.TestResults;
import hudson.plugins.testng.util.TestResultHistoryUtil;

import java.io.PrintStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

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
   private transient Reference<TestResults> testResults;

   /*
    * Cache test counts to speed up loading of graphs
    */
   private transient int passedTestCount;
   private transient int failedTestCount;
   private transient int skippedTestCount;

   public TestNGBuildAction(AbstractBuild<?, ?> build, TestResults testngResults) {
      this.build = build;
      testngResults.setOwner(this.build);
      this.testResults = new WeakReference<TestResults>(testngResults);

      //initialize the cached values when TestNGBuildAction is instantiated
      this.passedTestCount = testngResults.getPassedTestCount();
      this.failedTestCount = testngResults.getFailedTestCount();
      this.skippedTestCount = testngResults.getSkippedTestCount();
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
           testResults = new WeakReference<TestResults>(loadResults(getBuild(), null));
           return testResults.get();
        }

        TestResults tr = testResults.get();
        if (tr == null) {
           testResults = new WeakReference<TestResults>(loadResults(getBuild(), null));
          return testResults.get();
        } else {
          return tr;
        }
      } else {
        return results;
      }
   }

   static TestResults loadResults(AbstractBuild<?, ?> owner, PrintStream logger)
   {
      FilePath testngDir = Publisher.getTestNGReport(owner);
      FilePath[] paths = null;
      try {
         paths = testngDir.list("testng-results*.xml");
      } catch (Exception e) {
         //do nothing
      }

      TestResults tr = null;
      if (paths == null) {
        tr = new TestResults("");
        tr.setOwner(owner);
        return tr;
      }

      ResultsParser parser = new ResultsParser(logger);
      TestResults result = parser.parse(paths);
      result.setOwner(owner);
      return result;
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

   public Api getApi() {
      return new Api(getResults());
   }

   public int getPassedTestCount() {
      return this.passedTestCount;
   }

   public int getFailedTestCount() {
      return this.failedTestCount;
   }

   public int getSkippedTestCount() {
      return this.skippedTestCount;
   }

   /**
    * {@inheritDoc}
    *
    * NOTE: Executed when build action is read from disk - e.g. on Jenkins startup
    */
   protected Object readResolve() {
      TestResults testResults = getResults();

      //initialize the cached values
      passedTestCount = testResults.getPassedTestCount();
      failedTestCount = testResults.getFailedTestCount();
      skippedTestCount = testResults.getSkippedTestCount();

      return this;
   }
}
