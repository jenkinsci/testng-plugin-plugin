package hudson.plugins.testng.parser;

import hudson.FilePath;
import hudson.plugins.testng.results.PackageResult;
import hudson.plugins.testng.results.TestResults;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

public class TestParser {

   private TestResults getResults(String filename) {
      ResultsParser parser = new ResultsParser();
      FilePath[] filePaths = new FilePath[1];
      filePaths[0] = new FilePath(new File(filename));
      return parser.parse(filePaths);
   }

   private URL getResource(String filename) {
      return TestParser.class.getClassLoader().getResource(filename);
   }

   @Test
   public void testTestngXmlWithExistingResultXml() {
      URL resource = getResource("sample-testng-results.xml");
      Assert.assertNotNull(resource);
      TestResults results = getResults(resource.getFile());
      Assert.assertFalse("Collection shouldn't have been empty", results.getTestList().isEmpty());
   }

   @Test
   public void testTestngXmlWithSameTestNameDiffSuites() {
      URL resource = getResource("testng-results-same-test.xml");
      Assert.assertNotNull(resource);
      TestResults results = getResults(resource.getFile());
      Assert.assertFalse("Collection shouldn't have been empty", results.getTestList().isEmpty());
      Assert.assertEquals(2, results.getTestList().size());
      results.tally();
      Assert.assertEquals(1, results.getPackageNames().size());
      Assert.assertEquals(3, results.getPackageMap().values().iterator().next().getClassList().size());
      Assert.assertEquals(4, results.getPassedTestCount());
      Assert.assertEquals(4, results.getPassedTests().size());
   }

   @Test
   public void testTestngXmlWithExistingResultXmlGetsTheRightDurations() {
      URL resource = getResource("sample-testng-dp-result.xml");
      Assert.assertNotNull(resource);
      TestResults results = getResults(resource.getFile());
      Assert.assertFalse("Collection shouldn't have been empty", results.getTestList().isEmpty());

      // This test assumes that there is only 1 package in
      // sample-testng-dp-result that contains tests that add to 12 ms
      results.tally();
      Map<String, PackageResult> packageResults = results.getPackageMap();
      for(PackageResult result: packageResults.values()) {
        Assert.assertEquals("org.farshid", result.getName());
        Assert.assertEquals(12, result.getDuration());
      }
   }

   @Test
   public void testTestngXmlWithNonExistingResultXml() {
      TestResults results = getResults("/invalid/path/to/file/new-test-result.xml");
      Assert.assertTrue("Collection should have been empty. Number of results : "
               + results.getTestList().size(), results.getTestList().isEmpty());
   }

   @Test
   public void parseTestNG() {
      TestResults results = getResults(getResource("testng-results-testng.xml").getFile());
      results.tally();
   }

   @Test
   public void testParseEmptyException() {
      TestResults results = getResults(getResource("sample-testng-empty-exp.xml").getFile());
      results.tally();
      Assert.assertEquals(1, results.getPassedTestCount());
   }

   @Test
   public void testDateParser() throws ParseException {
      //example of date format used in testng report
      String dateString = "2010-07-20T11:49:17Z";
      SimpleDateFormat sdf = new SimpleDateFormat(ResultsParser.DATE_FORMAT);
      sdf.parse(dateString);
   }
   
   @Test
   public void testReporterLogParser() throws ParseException {
       String filename = "sample-testng-reporter-log-result.xml";
       URL resource = TestParser.class.getClassLoader().getResource(filename);
       Assert.assertNotNull(resource);
       TestResults results = getResults(resource.getFile());
       Assert.assertFalse("Collection shouldn't have been empty", results.getTestList().isEmpty());
       Assert.assertEquals(1, results.getTestList().size());
       results.tally();
       Assert.assertEquals(1, results.getPackageNames().size());
       Assert.assertEquals(1, results.getPackageMap().values().iterator().next().getClassList().size());
       Assert.assertEquals(1, results.getPassedTestCount());
       Assert.assertEquals(1, results.getPassedTests().size());
       Assert.assertEquals(1, results.getFailedTests().size());
       Assert.assertNotNull(results.getFailedTests().get(0).getException());
       Assert.assertNotNull(results.getFailedTests().get(0).getReporterOuputLines());
       Assert.assertEquals(2, results.getFailedTests().get(0).getReporterOuputLines().size());
       Assert.assertEquals("Some Reporter.log() statement", results.getFailedTests().get(0).getReporterOuputLines().get(0));
       Assert.assertEquals("Another Reporter.log() statement", results.getFailedTests().get(0).getReporterOuputLines().get(1));
       Assert.assertEquals(0, results.getPassedTests().get(0).getReporterOuputLines().size());
   }
}
