package hudson.plugins.testng.util;

import java.util.concurrent.TimeUnit;

public class FormatUtil {

   /**
    * Formats the time into mill
    * @param duration
    * @return
    */
   public static String formatTimeInMilliSeconds(long duration) {
      try {
         StringBuffer durationInString = new StringBuffer("");
         long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
         long seconds = TimeUnit.MILLISECONDS.toSeconds(duration - minutes * 60 * 1000);
         long milliseconds = duration - (minutes * 60 * 1000) - seconds * 1000;
         durationInString.append(minutes).append(" min ");
         durationInString.append(seconds).append(" sec ");
         durationInString.append(milliseconds).append(" msec");
         return durationInString.toString();
      } catch (Exception e) {
         e.printStackTrace();
         return "-1";
      }
   }

   /**
    * Replaces newline characters in string with <code>&lt;br/&gt;</code> to retain
    * the newlines when the string is displayed in HTML
    *
    * @param str
    * @return
    */
   public static String replaceNewLineWithBR(String str) {
      return str == null ? "" : str.replace("\n", "<br/>");
   }

   /**
    * Formats the stack trace for easier readability
    * @param stackTrace
    * @return
    */
   public static String formatStackTraceForHTML(String stackTrace) {
      return replaceNewLineWithBR(stackTrace);
   }
}
