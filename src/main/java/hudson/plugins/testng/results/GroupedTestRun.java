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
   //unique id that associates methods run for a particular suite -> test -> class
   private String testRunId;
   //name of the associated test i.e. the <test>'s name attr
   private String testName;
   //name of the associate suite
   private String suiteName;

   public GroupedTestRun(String testRunId, String testName, String suiteName) {
      this.testRunId = testRunId;
      this.testName = testName;
      this.suiteName = suiteName;
   }

   public String getTestRunId() {
      return testRunId;
   }

   public String getTestName() {
      return testName;
   }

   public String getSuiteName() {
      return suiteName;
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
