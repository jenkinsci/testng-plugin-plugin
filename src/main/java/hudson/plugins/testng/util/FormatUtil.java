package hudson.plugins.testng.util;

/**
 * These methods are used to format strings in jelly files
 *
 * @author nullin
 * @author farshidce
 *
 */
public class FormatUtil {


    public static final String MORE_THAN_24HRS = "> 24hrs";
    private static final long HOUR_IN_SEC = 60 * 60;
    private static final long MIN_IN_SEC = 60;
    
    private FormatUtil() {}

   /**
    * Formats the time into a human readable format
    * @param duration time duration in seconds
    * @return time represented in a human readable format
    */
   public static String formatTime(float duration) {
      if (duration / (24 * HOUR_IN_SEC) > 1) {
         return MORE_THAN_24HRS;
      }
      try {
         byte hours = (byte) (duration / HOUR_IN_SEC);
         duration -= hours * HOUR_IN_SEC;
         byte minutes = (byte) (duration / MIN_IN_SEC);
         duration -= minutes * MIN_IN_SEC;
         byte seconds = (byte) duration;
         duration -= seconds;
         int milliseconds = Math.round(duration * 1000f);
         return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
      } catch (Exception e) {
         e.printStackTrace();
         return "-1";
      }
   }

   /**
    * Formats a long value and prepends it with a - or +
    * This functions is used for showing the diff values for test runs
    * @param value long value
    * @return a long value prepended with a - or +
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

   // TODO try replacing with Util.escape
   /**
    * Replaces newline characters in string with {@code <br/>} to retain
    * the newlines when the string is displayed in HTML.
    * It also replaces {@code <}, {@code >}, {@code &}, and {@code "} characters with their corresponding html code.
    * ref : http://www.theukwebdesigncompany.com/articles/entity-escape-characters.php
    *
    * @param str a string
    * @return escaped string
    */
   public static String escapeString(String str) {
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
    * @param stackTrace a stack trace
    * @return the stack trace formatted for easier readability
    */
   public static String formatStackTraceForHTML(String stackTrace) {
      return escapeString(stackTrace);
   }
}
