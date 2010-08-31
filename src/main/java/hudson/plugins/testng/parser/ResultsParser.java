package hudson.plugins.testng.parser;

import hudson.plugins.testng.results.*;

import java.io.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.xmlpull.v1.XmlPullParser;

public class ResultsParser {

   /**
    * @param file
    * @return
    */
   public static Collection<TestResults> parse(File file,
                                               PrintStream printStream) {
      if (null == file) {
         if (printStream != null) {
            printStream.println("file not specified");
         }
         return Collections.EMPTY_LIST;
      }
      
      if (!file.exists() || file.isDirectory()) {
         if (printStream != null) {
            printStream.println("'" + file.getAbsolutePath() + "' points to a non-existent file or directory");
         }
         return Collections.EMPTY_LIST;
      }
      
      ResultPullParserHelper xmlParserHelper = new ResultPullParserHelper();
      Collection<TestResults> results = new ArrayList<TestResults>();
      FileInputStream fileInputStream = xmlParserHelper.createFileInputStream(file);

      if (fileInputStream != null) {
         BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
         XmlPullParser xmlPullParser = xmlParserHelper.createXmlPullParser(bufferedInputStream);
         if (xmlPullParser != null) {
            // check that the first tag is <testng-results>
            if (xmlParserHelper.parseToTagIfFound(xmlPullParser, "testng-results", 0)) {
               // skip until we get to the <suite> tag
               while (xmlParserHelper.parseToTagIfFound(xmlPullParser, "suite", 1)) {
                  TestResults testNGTestResults = new TestResults(UUID.randomUUID().toString()
                        + "_TestNGResults");
                  List<TestResult> testNGTestList = new ArrayList<TestResult>();
                  int suiteDepth = xmlPullParser.getDepth();
                  // skip until we get to the <test> tag
                  while (xmlParserHelper.parseToTagIfFound(xmlPullParser, "test", suiteDepth)) {
                     //for-each <test> tag
                     int testDepth = xmlPullParser.getDepth();
                     TestResult testngTest = new TestResult();
                     String name = xmlPullParser.getAttributeValue(null, "name");
                     testngTest.setName(name);

                     List<ClassResult> testNGClassList = new ArrayList<ClassResult>();
                     while (xmlParserHelper.parseToTagIfFound(xmlPullParser, "class", testDepth)) {
                        int classDepth = xmlPullParser.getDepth();
                        ClassResult testNGTestClass = new ClassResult();
                        testNGTestClass.setName(xmlPullParser.getAttributeValue(null, "name"));
                        testNGTestClass.setFullName(xmlPullParser.getAttributeValue(null, "name"));
                        
                        List<MethodResult> testMethodList = new ArrayList<MethodResult>();
                        while (xmlParserHelper.parseToTagIfFound(xmlPullParser, "test-method", classDepth)) {
                           MethodResult testNGTestMethod = xmlParserHelper.createTestMethod(xmlPullParser, testNGTestClass);
                           if (testNGTestMethod != null) {
                              MethodResultException exception =
                                    xmlParserHelper.createExceptionObject(xmlPullParser);
                              if (exception != null) {
                                 testNGTestMethod.setException(exception);
                              }
                              ResultsParser.updateTestMethodLists(testNGTestResults, testNGTestMethod);
                              // add to test methods list for each class
                              testMethodList.add(testNGTestMethod);
                           }
                        }
                        testNGTestClass.setTestMethodList(testMethodList);
                        testNGClassList.add(testNGTestClass);
                     }
                     testngTest.setClassList(testNGClassList);
                     testNGTestList.add(testngTest);
                  }
                  testNGTestResults.setTestList(testNGTestList);
                  testNGTestResults.tally();
                  results.add(testNGTestResults);
                  
                  if (printStream != null) {
                     if (testNGTestResults.getTotalTestCount() > 0) {
                        printStream.println("parsed file : " + file.getAbsolutePath()
                              + " and collected testng results . populated "
                              + testNGTestResults.getTotalTestCount() + " test case results");
                     } else {
                        printStream.println("parsed file : " + file.getAbsolutePath()
                              + " and did not find any test result");
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

   private static void updateTestMethodLists(TestResults testResults, MethodResult testNGTestMethod) {
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
}
