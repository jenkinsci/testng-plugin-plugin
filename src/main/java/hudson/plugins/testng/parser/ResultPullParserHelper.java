package hudson.plugins.testng.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Helper class for parsing TestNG result XML files
 *
 * @author farshidce
 * @author nullin
 */
public class ResultPullParserHelper {

   private static final Logger log = Logger.getLogger(ResultPullParserHelper.class.getName());

   /**
    * @param xmlPullParser
    * @param name
    * @param initialDepth
    * @return
    */
   public static boolean parseToTagIfFound(XmlPullParser xmlPullParser,
                                    String name,
                                    int initialDepth) {
      if (xmlPullParser != null && name != null) {
         try {
            while (xmlPullParser.getDepth() >= initialDepth) {
               if (isStartTag(xmlPullParser)) {
                  log.fine("current node name : " + xmlPullParser.getName());
                  if (name.equals(xmlPullParser.getName())) {
                     return true;
                  }
               }
               xmlPullParser.next();
            }
            //at this point we should be seeing a tag with .getName as "exception"
         } catch (XmlPullParserException e) {
            log.warning("next() threw exception : " + e.getMessage());
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
      return false;
   }


   /**
    * @param xmlPullParser
    * @param tags
    * @param initialDepth
    * @return
    */
   public static String parseToTagIfAnyFound(XmlPullParser xmlPullParser,
                                      List<String> tags,
                                      int initialDepth) {
      //find the first tag and then return that ?
      if (xmlPullParser != null && tags != null && tags.size() > 0) {
         try {
            while (xmlPullParser.getDepth() >= initialDepth) {
               if (isStartTag(xmlPullParser)) {
                  log.fine("current node name : " + xmlPullParser.getName());
                  if (tags.contains(xmlPullParser.getName())) {
                     return xmlPullParser.getName();
                  }
               }
               xmlPullParser.next();
            }
            //at this point we should be seeing a tag with .getName as "exception"
         } catch (XmlPullParserException e) {
            log.warning("next() threw exception : " + e.getMessage());
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
      return null;
   }

   /**
    * @param xmlPullParser
    * @return
    */
   private static boolean isStartTag(XmlPullParser
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
    * @param xmlPullParser
    * @return
    */
   private static boolean isText(XmlPullParser
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
    * @param file
    * @return
    */
   public static FileInputStream createFileInputStream(File file) {
      if (file != null && file.exists()) {
         try {
            return new FileInputStream(file);
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         }
      }
      return null;
   }

   public static XmlPullParser createXmlPullParser(BufferedInputStream
         bufferedInputStream) {
      if (bufferedInputStream != null) {
         try {
            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
            xmlPullParserFactory.setNamespaceAware(true);
            xmlPullParserFactory.setValidating(false);

            XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
            xmlPullParser.setInput(bufferedInputStream, null);
            return xmlPullParser;
         } catch (XmlPullParserException e) {
            log.severe("unable to create a new XmlPullParserFactory instance : error message : "
                  + e.getMessage());
         }
      }
      return null;
      //define a  custom exception saying could not initialize the parser
   }
}

