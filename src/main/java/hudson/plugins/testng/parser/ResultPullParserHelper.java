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
    * Attempts to find and move the parser forward to the opening tag with the
    * specified name
    *
    * @param xmlPullParser XML Parser
    * @param name name of the XML tag
    * @param initialDepth
    * @return true, if such a tag is found, false otherwise
    */
   public static boolean parseToTagIfFound(XmlPullParser xmlPullParser,
         String name, int initialDepth) {
      if (xmlPullParser == null || name == null) {
        return false;
      }
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
      } catch (XmlPullParserException e) {
         log.warning("next() threw exception : " + e.getMessage());
      } catch (IOException e) {
         e.printStackTrace();
      }

      return false;
   }

   public static boolean parseToTagIfFound(XmlPullParser xmlPullParser,
         String name) {
      return parseToTagIfFound(xmlPullParser, name, xmlPullParser.getDepth());
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

   public static XmlPullParser createXmlPullParser(BufferedInputStream
         bufferedInputStream) throws XmlPullParserException {
      XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
      xmlPullParserFactory.setNamespaceAware(true);
      xmlPullParserFactory.setValidating(false);

      XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
      xmlPullParser.setInput(bufferedInputStream, null);
      return xmlPullParser;
   }
}

