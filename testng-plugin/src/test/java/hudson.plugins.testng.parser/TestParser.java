package hudson.plugins.testng.parser;

import org.junit.Test;

import hudson.plugins.testng.results.TestResults;

import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import junit.framework.Assert;

public class TestParser {

   @Test
   public void testTestngXmlWithExistingResultXml() {
      String filename = "sample-testng-results.xml";
      URL resource = TestParser.class.getClassLoader().getResource(filename);
      Assert.assertNotNull(resource);
      Collection<TestResults> results = ResultsParser.parse(new File(resource.getFile()), null);
      Assert.assertFalse("Collection shouldn't have been empty", results.isEmpty());
   }
   
   @Test
   public void testTestngXmlWithNonExistingResultXml() {
      String filename = "/invalid/path/to/file/new-test-result.xml";
      Collection<TestResults> results = ResultsParser.parse(new File(filename), null);
      Assert.assertTrue("Collection should have been empty. Number of results : " 
               + results.size(), results.isEmpty());
   }

   @Test
   public void testDateParser() {
      String dateString = "2010-07-20T11:49:17Z";
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      Date dt = null;
      try {
         dt = sdf.parse(dateString);
      } catch (ParseException e) {
         e.printStackTrace();
      }
      System.out.println(dt);
   }
}
