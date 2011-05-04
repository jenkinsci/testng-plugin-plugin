package hudson.plugins.testng.parser;

import hudson.plugins.testng.results.ClassResult;
import hudson.plugins.testng.results.MethodResult;
import hudson.plugins.testng.results.MethodResultException;
import hudson.plugins.testng.results.TestResult;
import hudson.plugins.testng.results.TestResults;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ResultsParser {

   private PrintStream printStream;
   private static Logger log = Logger.getLogger(ResultsParser.class.getName());

   public ResultsParser(PrintStream printStream) {
     if (printStream == null) {
       throw new IllegalArgumentException("Printstream can not be null");
     }
     this.printStream = printStream;
   }

   /**
    * @param file
    * @param printStream
    * @return
    */
   public Collection<TestResults> parse(File file) {
      if (null == file) {
         printStream.println("File not specified");
         return Collections.EMPTY_LIST;
      }

      if (!file.exists() || file.isDirectory()) {
         printStream.println("'" + file.getAbsolutePath() + "' points to a non-existent file or directory");
         return Collections.EMPTY_LIST;
      }

      Collection<TestResults> results = new ArrayList<TestResults>();
      FileInputStream fileInputStream = ResultPullParserHelper.createFileInputStream(file);

      if (fileInputStream != null) {
         BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
         XmlPullParser xmlPullParser = ResultPullParserHelper.createXmlPullParser(bufferedInputStream);
         if (xmlPullParser != null) {
            // check that the first tag is <testng-results>
            if (ResultPullParserHelper.parseToTagIfFound(xmlPullParser, "testng-results", 0)) {
               // skip until we get to the <suite> tag
               while (ResultPullParserHelper.parseToTagIfFound(xmlPullParser, "suite", 1)) {
                  TestResults testNGTestResults = new TestResults(UUID.randomUUID().toString()
                        + "_TestNGResults");
                  List<TestResult> testNGTestList = new ArrayList<TestResult>();
                  int suiteDepth = xmlPullParser.getDepth();
                  //TODO: changes need to be made for jira # 8926
                  // skip until we get to the <test> tag
                  //see if there is a groups tag , then lets parse all the groups and
                  //later on we have to create a map of groups and test methods ?
                  //we have some sort of unique identifier for each test method which we should be able
                  //to reuse for rendering purposes
                  //so let's have a class called GroupResult
                  while (ResultPullParserHelper.parseToTagIfFound(xmlPullParser, "test", suiteDepth)) {
                     //for-each <test> tag
                     int testDepth = xmlPullParser.getDepth();
                     TestResult testngTest = new TestResult();
                     String name = xmlPullParser.getAttributeValue(null, "name");
                     testngTest.setName(name);

                     List<ClassResult> testNGClassList = new ArrayList<ClassResult>();
                     while (ResultPullParserHelper.parseToTagIfFound(xmlPullParser, "class", testDepth)) {
                        int classDepth = xmlPullParser.getDepth();
                        ClassResult testNGTestClass = new ClassResult();
                        testNGTestClass.setName(xmlPullParser.getAttributeValue(null, "name"));
                        testNGTestClass.setFullName(xmlPullParser.getAttributeValue(null, "name"));

                        List<MethodResult> testMethodList = new ArrayList<MethodResult>();
                        String uuid = UUID.randomUUID().toString();
                        while (ResultPullParserHelper.parseToTagIfFound(xmlPullParser, "test-method", classDepth)) {
                           MethodResult testNGTestMethod = createTestMethod(xmlPullParser, testNGTestClass);
                           String testUuid = UUID.randomUUID().toString();
                           if (testNGTestMethod != null) {
                              MethodResultException exception =
                                    createExceptionObject(xmlPullParser);
                              if (exception != null) {
                                 testNGTestMethod.setException(exception);
                              }
                               testNGTestMethod.setTestUuid(testUuid);
                              //this uuid is used later to group the tests and config-methods together
                              testNGTestMethod.setTestRunId(uuid);
                              updateTestMethodLists(testNGTestResults, testNGTestMethod);
                              // add to test methods list for each class
                              testMethodList.add(testNGTestMethod);
                           }
                        }

                        //if a class with the same name already exists we should add these new
                        //methods to that class
                        boolean classAlreadyAdded = false;
                        for (ClassResult classResult : testNGClassList) {
                           if (classResult.getName().equals(testNGTestClass.getName())) {
                              //we should merge test classes
                              classResult.addTestMethods(testMethodList);
                              classAlreadyAdded = true;
                              break;
                           }
                        }
                        if (!classAlreadyAdded) {
                           testNGTestClass.setTestMethodList(testMethodList);
                           testNGClassList.add(testNGTestClass);
                        }
                     }
                     testngTest.setClassList(testNGClassList);
                     testNGTestList.add(testngTest);
                  }
                  testNGTestResults.setTestList(testNGTestList);
                  results.add(testNGTestResults);

                  if (printStream != null) {
                     if (testNGTestResults.getTotalTestCount() > 0) {
                        printStream.println("Parsed TestNG XML Report at '" + file.getAbsolutePath()
                              + "' and collected "
                              + testNGTestResults.getTotalTestCount() + " test results");
                     } else {
                        printStream.println("Parsed TestNG XML Report at '" + file.getAbsolutePath()
                              + "' and did not find any test results");
                     }
                  }
               }
            }
         }

         try {
            bufferedInputStream.close();
         } catch (IOException e) {
            e.printStackTrace();
         } finally {
            try {
               fileInputStream.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
      return results;
   }

   private void updateTestMethodLists(TestResults testResults, MethodResult testNGTestMethod) {
      if (testNGTestMethod.isConfig()) {
         if ("FAIL".equals(testNGTestMethod.getStatus())) {
            testResults.getFailedConfigurationMethods().add(testNGTestMethod);
         } else {
            if ("SKIP".equals(testNGTestMethod.getStatus())) {
               testResults.getSkippedConfigurationMethods().add(testNGTestMethod);
            }
         }
      } else {
         if ("FAIL".equals(testNGTestMethod.getStatus())) {
            testResults.getFailedTests().add(testNGTestMethod);
         } else {
            if ("SKIP".equals(testNGTestMethod.getStatus())) {
               testResults.getSkippedTests().add(testNGTestMethod);
            } else {
               if ("PASS".equals(testNGTestMethod.getStatus())) {
                  testResults.getPassedTests().add(testNGTestMethod);
               }
            }
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
      SimpleDateFormat simpleDateFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      MethodResult testNGTestMethod = null;
      if (xmlPullParser != null) {
         testNGTestMethod = new MethodResult();
         testNGTestMethod.setName(xmlPullParser.getAttributeValue(null, "name"));
         testNGTestMethod.setStatus(xmlPullParser.getAttributeValue(null, "status"));
         testNGTestMethod.setDescription(xmlPullParser
               .getAttributeValue(null, "description"));
         try {
            testNGTestMethod.setDuration(Long.parseLong(xmlPullParser.getAttributeValue(
                  null, "duration-ms")));
         } catch (NumberFormatException e) {
            log.warning("unable to obtain duration-ms");
         }
         try {
            testNGTestMethod.setStartedAt(simpleDateFormat.parse(xmlPullParser.getAttributeValue(
                  null, "started-at")));
         } catch (ParseException e) {
            log.warning("unable to obtain started-at");
         }
         String isConfigStr = xmlPullParser.getAttributeValue(null, "is-config");
         testNGTestMethod.setFullName(testNGClass.getFullName() +
               "." + testNGTestMethod.getName());
         if (isConfigStr == null) {
            testNGTestMethod.setConfig(false);
         } else {
            // is-config attr is present on test-method. It's
            // always set to true
            testNGTestMethod.setConfig(true);
         }
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
      MethodResultException exception = new MethodResultException();
      //what happens if the nextTag is not a "exception" should I rever the state??
      if (xmlPullParser != null) {
         if (ResultPullParserHelper.parseToTagIfFound(xmlPullParser, "exception", xmlPullParser.getDepth())) {
            List<String> tags =
                  new ArrayList<String>();
            tags.add("message");
            tags.add("short-stacktrace");
            tags.add("full-stacktrace");
            int exceptionDepth = xmlPullParser.getDepth();
            while (tags.size() > 0) {
               String tagFound =
                 ResultPullParserHelper.parseToTagIfAnyFound(xmlPullParser, tags, exceptionDepth);
               if (tagFound == null) {
                  log.fine("did not find any of the tags. break from the loop");
                  break;
               } else {
                  try {
                     if (tagFound.equals("message")) {
                        exception.setMessage(xmlPullParser.nextText());
                     } else {
                        if (tagFound.equals("short-stacktrace")) {
                           exception.setShortStackTrace(xmlPullParser.nextText());
                        } else {
                           if (tagFound.equals("full-stacktrace")) {
                              exception.setFullStackTrace(xmlPullParser.nextText());
                           }
                        }
                     }
                  } catch (XmlPullParserException e) {
                     e.printStackTrace();
                  } catch (IOException e) {
                     e.printStackTrace();
                  }
                  tags.remove(tagFound);
               }
            }
            return exception;
         }
      }
      return null;
   }

}
