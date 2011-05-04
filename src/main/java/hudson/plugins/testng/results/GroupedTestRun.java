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

   private List<MethodResult> testMethods;
   private List<MethodResult> configurationMethods;
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
      if (this.testMethods == null) {
         this.testMethods = new ArrayList<MethodResult>();
      }
      this.testMethods.add(methodResult);
   }

   public void addConfigurationMethod(MethodResult methodResult) {
      if (this.configurationMethods == null) {
         this.configurationMethods = new ArrayList<MethodResult>();
      }
      this.configurationMethods.add(methodResult);
   }

}
