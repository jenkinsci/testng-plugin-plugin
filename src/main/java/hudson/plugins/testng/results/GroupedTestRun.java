package hudson.plugins.testng.results;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used for rendering Class Results
 *
 * @author farshidce
 *
 */
public class GroupedTestRun {

   private List<MethodResult> testMethods = new ArrayList<MethodResult>();
   private List<MethodResult> configurationMethods = new ArrayList<MethodResult>();
   private String testRunId;

   public String getTestRunId() {
      return testRunId;
   }

   public void setTestRunId(String testRunId) {
      this.testRunId = testRunId;
   }

   public List<MethodResult> getTestMethods() {
      return testMethods;
   }

   public List<MethodResult> getConfigurationMethods() {
      return configurationMethods;
   }

   public void addTestMethod(MethodResult methodResult) {
      this.testMethods.add(methodResult);
   }

   public void addConfigurationMethod(MethodResult methodResult) {
      this.configurationMethods.add(methodResult);
   }

}
