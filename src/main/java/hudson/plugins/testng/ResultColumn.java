package hudson.plugins.testng;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

public class ResultColumn extends ListViewColumn {

   public String getTestResultString(Job<?,?> job) {
      AbstractTestResultAction tests = getTestResultAction(job);
      String resultString = "No tests run";
      if(tests != null && tests.getTotalCount() > 0) {
         resultString = tests.getFailCount() + "";
         if(!tests.getFailureDiffString().equals("")) {
            resultString = resultString + "(" + tests.getFailureDiffString().replace(" / ", "") + ") ";
         }
         resultString = resultString + " failed/" + tests.getTotalCount() + " total";
      }

      return resultString;
   }
   
   public AbstractTestResultAction<?> getTestResultAction(Job<?,?> job) {
       Run<?,?> b = job.getLastCompletedBuild();
       return b != null ? b.getAction(AbstractTestResultAction.class) : null;
   }

   public static class TestResultDescriptor extends ListViewColumnDescriptor {

      @Override
      public ListViewColumn newInstance(final StaplerRequest request,
                                        final JSONObject formData) throws FormException {
         return new ResultColumn();
      }

      @Override
      public boolean shownByDefault() {
         return false;
      }

      @Override
      public String getDisplayName() {
         return "Latest TestNG Results";
      }
   }

   @Extension
   public static final Descriptor<ListViewColumn> DESCRIPTOR = new TestResultDescriptor();

   @Override
   public Descriptor<ListViewColumn> getDescriptor() {
      return DESCRIPTOR;
   }
}