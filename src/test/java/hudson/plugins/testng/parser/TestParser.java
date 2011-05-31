package hudson.plugins.testng.parser;

import hudson.plugins.testng.results.TestResults;
import hudson.plugins.testng.results.PackageResult;

import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class TestParser {

   @Test
   public void testTestngXmlWithExistingResultXml() {
      String filename = "sample-testng-results.xml";
      URL resource = TestParser.class.getClassLoader().getResource(filename);
      Assert.assertNotNull(resource);
      ResultsParser parser = new ResultsParser(System.out);
      TestResults results = parser.parse(new File(resource.getFile()));
      Assert.assertFalse("Collection shouldn't have been empty", results.getTestList().isEmpty());
   }

   @Test
   public void testTestngXmlWithExistingResultXmlGetsTheRightDurations() {
      String filename = "sample-testng-dp-result.xml";
      URL resource = TestParser.class.getClassLoader().getResource(filename);
      Assert.assertNotNull(resource);
      ResultsParser parser = new ResultsParser(System.out);
      TestResults results = parser.parse(new File(resource.getFile()));
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
      String filename = "/invalid/path/to/file/new-test-result.xml";
      ResultsParser parser = new ResultsParser(System.out);
      TestResults results = parser.parse(new File(filename));
      Assert.assertTrue("Collection should have been empty. Number of results : "
               + results.getTestList().size(), results.getTestList().isEmpty());
   }

   @Test
   public void testDateParser() {
      String dateString = "2010-07-20T11:49:17Z";
      SimpleDateFormat sdf = new SimpleDateFormat(ResultsParser.DATE_FORMAT);
      Date dt = null;
      try {
         dt = sdf.parse(dateString);
      } catch (ParseException e) {
         e.printStackTrace();
      }
      System.out.println(dt);
   }
}
