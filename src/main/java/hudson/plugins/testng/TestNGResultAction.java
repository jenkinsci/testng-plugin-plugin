package hudson.plugins.testng;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.plugins.testng.results.TestResults;

public class TestNGResultAction
      extends hudson.tasks.test.AbstractTestResultAction<TestNGResultAction>
      implements org.kohsuke.stapler.StaplerProxy {

   private TestResults testNGResults;


   public TestNGResultAction(AbstractBuild owner, TestResults result, BuildListener listener) {
      super(owner);
      setTestNGResults(result);
   }

   public void setTestNGResults(TestResults testNGResults) {
      this.testNGResults = testNGResults;
   }

   protected TestNGResultAction(AbstractBuild owner) {
      super(owner);
   }


   public Object getTarget() {
      return null;
   }

    @Override
    public String getUrlName() {
        return "testngreports";
    }

   @Override
   public int getFailCount() {
      if (testNGResults != null) {
         return testNGResults.getFailedTestCount();
      }
      return 0;
   }

   @Override
   public int getTotalCount() {
      if (testNGResults != null) {
         return testNGResults.getTotalTestCount();
      }
      return 0;
   }

   @Override
   public Object getResult() {
      return testNGResults;
   }
}
