package hudson.plugins.testng.util;

import java.util.concurrent.TimeUnit;

/**
 * These methods are used to format strings in jelly files
 *
 * @author nullin
 * @author farshidce
 *
 */
public class FormatUtil {

   /**
    * Formats the time into mill
    * @param duration
    * @return
    */
   public static String formatTimeInMilliSeconds(long duration) {
      try {
         StringBuffer durationInString = new StringBuffer("");
         //TODO: add hours here as well
         long minutes = TimeUnit.MILLISECONDS.toSeconds(duration) / 60;
         long seconds = TimeUnit.MILLISECONDS.toSeconds(duration - minutes * 60 * 1000);
         long milliseconds = duration - (minutes * 60 * 1000) - seconds * 1000;
         if (minutes > 0) {
            durationInString.append(minutes).append(" min ");
         }
         if (seconds > 0) {
            durationInString.append(seconds).append(" sec ");
         }
         if (milliseconds > 0) {
            durationInString.append(milliseconds).append(" msec");
         }
         return durationInString.toString();
      } catch (Exception e) {
         e.printStackTrace();
         return "-1";
      }
   }

   /**
    * Formats a long value and prepends it with a - or +
    * This functions is used for showing the diff values for test runs
    * @param value - long value
    * @return
    */
   public static String formatLong(long value) {
      if (value == 0) {
         return "0";
      } else if (value < 0) {
         return Long.toString(value);
      } else { // if (a < b)
         return "+" + Long.toString(value);
      }
   }

   /**
    * Replaces newline characters in string with <code>&lt;br/&gt;</code> to retain
    * the newlines when the string is displayed in HTML
    * It also replaces < , > , & and " characters with their corresponding html code
    * ref : http://www.theukwebdesigncompany.com/articles/entity-escape-characters.php
    *
    * @param str
    * @return
    */
   public static String escapeString(String str) {
      //escape the < with &lt
      if (str == null) {
         return "";
      }

      str = str.replace("&","&amp;");
      str = str.replace("<","&lt;");
      str = str.replace(">","&gt;");
      str = str.replace("\"","&quot;");
      str = str.replace("\n", "<br/>");
      return str;
   }

   /**
    * Formats the stack trace for easier readability
    * @param stackTrace
    * @return
    */
   public static String formatStackTraceForHTML(String stackTrace) {
      return escapeString(stackTrace);
   }
}
