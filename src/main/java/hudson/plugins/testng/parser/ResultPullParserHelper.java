package hudson.plugins.testng.parser;


import hudson.plugins.testng.results.ClassResult;
import hudson.plugins.testng.results.MethodResult;
import hudson.plugins.testng.results.MethodResultException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultPullParserHelper {

   /**
    * 
    */
   public ResultPullParserHelper() {
   }

   /**
    *
    * @param xmlPullParser
    * @param name
    * @param initialDepth
    * @return
    */
   public boolean parseToTagIfFound(XmlPullParser xmlPullParser,
                                    String name,
                                    int initialDepth) {
      if (xmlPullParser != null && name != null) {
         try {
            while (xmlPullParser.getDepth() >= initialDepth) {
               if (isStartTag(xmlPullParser)) {
                  log("current node name : " + xmlPullParser.getName());
                  if (name.equals(xmlPullParser.getName())) {
                     return true;
                  }
               }
               xmlPullParser.next();

            }
            //at this point we should be seeing a tag with .getName as "exception"
         } catch (XmlPullParserException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
      return false;
   }


   /**
    *
    * @param xmlPullParser
    * @param tags
    * @param initialDepth
    * @return
    */
   public String parseToTagIfAnyFound(XmlPullParser xmlPullParser,
                                      List<String> tags,
                                      int initialDepth) {
      //find the first tag and then return that ?
      if (xmlPullParser != null && tags != null && tags.size() > 0) {
         try {
            while (xmlPullParser.getDepth() >= initialDepth) {
               if (isStartTag(xmlPullParser)) {
                  log("current node name : " + xmlPullParser.getName());
                  if (tags.contains(xmlPullParser.getName())) {
                     return xmlPullParser.getName();
                  }
               }
               xmlPullParser.next();
            }
            //at this point we should be seeing a tag with .getName as "exception"
         } catch (XmlPullParserException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
      return null;
   }


   /**
    *
    * @param xmlPullParser
    * @param testNGClass
    * @return
    */
   public MethodResult createTestMethod(XmlPullParser
         xmlPullParser, ClassResult testNGClass) {
      SimpleDateFormat simpleDateFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      MethodResult testNGTestMethod = null;
      if (xmlPullParser != null) {
         testNGTestMethod = new MethodResult();
         testNGTestMethod.setName(xmlPullParser.getAttributeValue(
               null, "name"));
         testNGTestMethod.setStatus(xmlPullParser.getAttributeValue(
               null, "status"));
         testNGTestMethod.setDescription(xmlPullParser
               .getAttributeValue(null, "description"));
         try {
            testNGTestMethod.setDuration(Long.parseLong(xmlPullParser.getAttributeValue(
                  null, "duration-ms")));
         } catch (NumberFormatException e) {
            log("unable to obtain duration-ms");
         }
         try {
            testNGTestMethod.setStartedAt(simpleDateFormat.parse(xmlPullParser.getAttributeValue(
                  null, "started-at")));
         } catch (ParseException e) {
            log("unable to obtain started-at");
         }         
         String isConfigStr = xmlPullParser.getAttributeValue(null,
               "is-config");
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
      printTestMethod(testNGTestMethod);
      return testNGTestMethod;
   }

   /**
    *
    * @param testMethod
    */
   public void printTestMethod(MethodResult
         testMethod) {
      if (testMethod != null) {
         log("name : " + testMethod.getName());
         log("duration : " + testMethod.getDuration());
         log("name : " + testMethod.getException());
         log("status : " + testMethod.getStatus());
         log("description : " + testMethod.getDescription());
         log("startedAt : " + testMethod.getStartedAt());
         if (testMethod.getException() != null) {
            log("exceptionMessage : " + testMethod.getException().getMessage());
         }

      } else {
         log("testMethod is null");
      }
   }

   public MethodResultException createExceptionObject(XmlPullParser xmlPullParser) {
      MethodResultException exception = new MethodResultException();
      //what happens if the nextTag is not a "exception" should I rever the state??
      if (xmlPullParser != null) {
         if (parseToTagIfFound(xmlPullParser, "exception", xmlPullParser.getDepth())) {
            List<String> tags =
                  new ArrayList<String>();
            tags.add("message");
            tags.add("short-stacktrace");
            tags.add("full-stacktrace");
            int exceptionDepth = xmlPullParser.getDepth();
            while (tags.size() > 0) {
               String tagFound =
                     parseToTagIfAnyFound(xmlPullParser, tags, exceptionDepth);
               if (tagFound == null) {
                  log("did not find any of the tags. break from the loop");
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

   /**
    *
    * @param xmlPullParser
    * @return
    */
   public boolean isStartTag(XmlPullParser
         xmlPullParser) {
      try {
         if (xmlPullParser != null) {
            return xmlPullParser.getEventType() == XmlPullParser.START_TAG;
         }
      } catch (XmlPullParserException e) {
         e.printStackTrace();
      }
      return false;
   }

   /**
    *
    * @param xmlPullParser
    * @return
    */
   public boolean isText(XmlPullParser
         xmlPullParser) {
      try {
         if (xmlPullParser != null) {
            return xmlPullParser.getEventType() == XmlPullParser.TEXT;
         }
      } catch (XmlPullParserException e) {
         e.printStackTrace();
      }
      return false;
   }


   /**
    *
    * @param file
    * @return
    */
   public FileInputStream createFileInputStream(File
         file) {
      if (file != null && file.exists()) {
         try {
            return new FileInputStream(file);
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         }
      }
      return null;
   }

   public XmlPullParser createXmlPullParser(BufferedInputStream
         bufferedInputStream) {
      try {
         if (bufferedInputStream != null) {
            bufferedInputStream.available();
            try {
               XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
               xmlPullParserFactory.setNamespaceAware(true);
               xmlPullParserFactory.setValidating(false);
               try {
                  XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
                  try {
                     xmlPullParser.setInput(bufferedInputStream, null);
                     return xmlPullParser;
                  } catch (XmlPullParserException e) {
                     e.printStackTrace();
                  }
               } catch (XmlPullParserException e) {
                  e.printStackTrace();
               }
            } catch (XmlPullParserException e) {
               e.printStackTrace();
               log("unable to create a new XmlPullParserFactory instance");
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
      return null;
      //define a  custom exception saying could not initialize the parser
   }


   private static void log(Object s) {
      System.out.println(s);
   }

}

