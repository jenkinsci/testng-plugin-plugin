package hudson.plugins.testng.parser;

import org.junit.Test;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestParser {

   @Test
   public void testTestngXmlWithSuite() {
      String filename =
         "/space/testng-plugin/work/jobs/hello/workspace/new-test-result.xml";
      ResultsParser.parse(new File(filename));
   }

   @Test
   public void testDateParser() {
      String dateString = "2010-07-20T11:49:17Z";
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      Date dt = null;
      try {
         dt = sdf.parse(dateString);
      } catch (ParseException e) {
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }
      System.out.println(dt);
   }
}
