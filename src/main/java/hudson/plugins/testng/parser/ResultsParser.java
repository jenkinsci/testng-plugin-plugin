package hudson.plugins.testng.parser;

import hudson.plugins.testng.results.MethodResultException;
import hudson.plugins.testng.results.TestResult;
import hudson.plugins.testng.results.TestResults;
import hudson.plugins.testng.results.ClassResult;
import hudson.plugins.testng.results.MethodResult;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Parses testng result XMLs generated using org.testng.reporters.XmlReporter
 * into objects that are then used to display results in Jenkins
 *
 * @author farshidce
 * @author nullin
 *
 */
public class ResultsParser {

   private static Logger log = Logger.getLogger(ResultsParser.class.getName());
   public static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

   /**
    * Parses the XML for relevant information
    *
    * @param file a file hopefully containing test related data in correct format
    * @return a collection of test results
    */
   public TestResults parse(File file) {
      TestResults allResults = new TestResults(UUID.randomUUID().toString() + "_TestResults");
      if (null == file) {
         log.severe("File not specified");
         return allResults;
      }

      if (!file.exists() || file.isDirectory()) {
        log.severe("'" + file.getAbsolutePath() + "' points to a non-existent file or directory");
         return allResults;
      }

      BufferedInputStream bufferedInputStream = null;
      try {
         bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
         XmlPullParser xmlPullParser = ResultPullParserHelper.createXmlPullParser(bufferedInputStream);

         // check that the first tag is <testng-results>
         if (ResultPullParserHelper.parseToTagIfFound(xmlPullParser, "testng-results")) {
            /*
             * We maintain only a single TestResult for all <test>s with the same name
             */
            Map<String, TestResult> testResultMap = new HashMap<String, TestResult>();
            /*
             * We maintain only a single ClassResult for all <class>s with the same fqdn
             */
            Map<String, ClassResult> classResultMap = new HashMap<String, ClassResult>();

            // skip until we get to the <suite> tag
            while (ResultPullParserHelper.parseToTagIfFound(xmlPullParser, "suite")) {
               List<TestResult> testNGTestList = new ArrayList<TestResult>();
               int suiteDepth = xmlPullParser.getDepth();
               // TODO: changes need to be made for jira # 8926
               // skip until we get to the <test> tag
               // see if there is a groups tag , then lets parse all the groups and
               // later on we have to create a map of groups and test methods ?
               // we have some sort of unique identifier for each test method which we should be able
               // to reuse for rendering purposes
               // so let's have a class called GroupResult
               while (ResultPullParserHelper.parseToTagIfFound(xmlPullParser, "test", suiteDepth)) {
                  // for-each <test> tag
                  int testDepth = xmlPullParser.getDepth();
                  String testName = xmlPullParser.getAttributeValue(null, "name");
                  TestResult testngTest = null;

                  if (testResultMap.containsKey(testName)) {
                     testngTest = testResultMap.get(testName);
                  } else {
                     testngTest = new TestResult(testName);
                     testResultMap.put(testName, testngTest);
                  }

                  List<ClassResult> testNGClassList = new ArrayList<ClassResult>();
                  while (ResultPullParserHelper.parseToTagIfFound(xmlPullParser, "class", testDepth)) {
                     int classDepth = xmlPullParser.getDepth();

                     String className = xmlPullParser.getAttributeValue(null, "name");
                     ClassResult testNGTestClass = null;

                     if (classResultMap.containsKey(testName)) {
                        testNGTestClass = classResultMap.get(testName);
                     } else {
                        testNGTestClass = new ClassResult(className);
                        classResultMap.put(className, testNGTestClass);
                     }

                     List<MethodResult> testMethodList = new ArrayList<MethodResult>();
                     String uuid = UUID.randomUUID().toString();
                     while (ResultPullParserHelper.parseToTagIfFound(xmlPullParser, "test-method", classDepth)) {
                        MethodResult testNGTestMethod = createTestMethod(xmlPullParser, testNGTestClass);
                        String testUuid = UUID.randomUUID().toString();
                        if (testNGTestMethod != null) {
                           testNGTestMethod.setException(createExceptionObject(xmlPullParser));
                           testNGTestMethod.setTestUuid(testUuid);
                           //this uuid is used later to group the tests and config-methods together
                           testNGTestMethod.setTestRunId(uuid);
                           updateTestMethodLists(allResults, testNGTestMethod);
                           // add to test methods list for each class
                           testMethodList.add(testNGTestMethod);
                        }
                     }

                     testNGTestClass.addTestMethods(testMethodList);
                     testNGClassList.add(testNGTestClass);

                  } //while for class ends

                  testngTest.addClassList(testNGClassList);
                  testNGTestList.add(testngTest);
               }

               allResults.addTestList(testNGTestList);
            } //while end for suites
         }
      } catch (XmlPullParserException e) {
         log.warning("Failed to parse XML: " + e.getMessage());
      } catch (FileNotFoundException e) {
        log.log(Level.SEVERE, "Failed to find XML file", e);
      } finally {
         try {
           if (bufferedInputStream != null) {
              bufferedInputStream.close();
           }
         } catch (IOException e) {
           log.log(Level.WARNING, "Failed to close input stream", e);
         }
      }

      return allResults;
   }

   private void updateTestMethodLists(TestResults testResults, MethodResult testNGTestMethod) {
      if (testNGTestMethod.isConfig()) {
         if ("FAIL".equals(testNGTestMethod.getStatus())) {
            testResults.getFailedConfigurationMethods().add(testNGTestMethod);
         } else if ("SKIP".equals(testNGTestMethod.getStatus())) {
               testResults.getSkippedConfigurationMethods().add(testNGTestMethod);
         }
      } else {
         if ("FAIL".equals(testNGTestMethod.getStatus())) {
            testResults.getFailedTests().add(testNGTestMethod);
         } else if ("SKIP".equals(testNGTestMethod.getStatus())) {
               testResults.getSkippedTests().add(testNGTestMethod);
         } else if ("PASS".equals(testNGTestMethod.getStatus())) {
                  testResults.getPassedTests().add(testNGTestMethod);
         }
      }
   }

   /**
    * @param xmlPullParser
    * @param testNGClass
    * @return
    */
   private MethodResult createTestMethod(XmlPullParser
         xmlPullParser, ClassResult testNGClass) {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
      MethodResult testNGTestMethod = new MethodResult();
      testNGTestMethod.setName(xmlPullParser.getAttributeValue(null, "name"));
      testNGTestMethod.setStatus(xmlPullParser.getAttributeValue(null, "status"));
      testNGTestMethod.setDescription(xmlPullParser.getAttributeValue(null, "description"));

      try {
         testNGTestMethod.setDuration(Long.parseLong(xmlPullParser.getAttributeValue(
               null, "duration-ms")));
      } catch (NumberFormatException e) {
         log.warning("unable to obtain duration-ms");
      }

      try {
         testNGTestMethod.setStartedAt(simpleDateFormat.parse(
               xmlPullParser.getAttributeValue(null, "started-at")));
      } catch (ParseException e) {
         log.warning("unable to obtain started-at");
      }

      String isConfigStr = xmlPullParser.getAttributeValue(null, "is-config");
      if (isConfigStr == null) {
         testNGTestMethod.setConfig(false);
      } else {
         // is-config attr is present on test-method. It's
         // always set to true
         testNGTestMethod.setConfig(true);
      }

      if (log.getLevel() == Level.FINE) {
         printTestMethod(testNGTestMethod);
      }
      return testNGTestMethod;
   }

   /**
    * @param testMethod
    */
   private void printTestMethod(MethodResult testMethod) {
      if (testMethod != null) {
         log.info("name : " + testMethod.getName());
         log.info("duration : " + testMethod.getDuration());
         log.info("name : " + testMethod.getException());
         log.info("status : " + testMethod.getStatus());
         log.info("description : " + testMethod.getDescription());
         log.info("startedAt : " + testMethod.getStartedAt());
         if (testMethod.getException() != null) {
            log.info("exceptionMessage : " + testMethod.getException().getMessage());
         }
      } else {
         log.info("testMethod is null");
      }
   }

   private MethodResultException createExceptionObject(XmlPullParser xmlPullParser) {
      List<String> tags = new ArrayList<String>();
      tags.add("message");
      tags.add("short-stacktrace");
      tags.add("full-stacktrace");

      if (xmlPullParser == null) {
        return null;
      }

      String message = null;
      String shortStackTrace = null;
      String fullStackTrace = null;

      // TODO: [farshidce] what happens if the nextTag is not a "exception" should I never the state??
      if (ResultPullParserHelper.parseToTagIfFound(xmlPullParser, "exception", xmlPullParser.getDepth())) {
         int exceptionDepth = xmlPullParser.getDepth();
         while (tags.size() > 0) {
            String tagFound =
              ResultPullParserHelper.parseToTagIfAnyFound(xmlPullParser, tags, exceptionDepth);
            if (tagFound == null) {
               break;
            }
            try {
               if (tagFound.equals("message")) {
                  message = xmlPullParser.nextText();
               }
               if (tagFound.equals("short-stacktrace")) {
                 shortStackTrace = xmlPullParser.nextText();
               }
               if (tagFound.equals("full-stacktrace")) {
                 fullStackTrace = xmlPullParser.nextText();
               }
            } catch (XmlPullParserException e) {
               e.printStackTrace();
            } catch (IOException e) {
               e.printStackTrace();
            } finally {
               tags.remove(tagFound);
            }
         }
         return new MethodResultException(message, shortStackTrace, fullStackTrace);
      }
      return null;
   }

}
