package hudson.plugins.testng.util;

import java.util.concurrent.TimeUnit;

public class FormatUtil {


   public static String formatTimeInMilliSeconds(long duration) {
      try {
         String durationInString = "";
         long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
         long seconds = TimeUnit.MILLISECONDS.toSeconds(duration - minutes * 60 * 1000);
         long milliseconds = duration - (minutes * 60 * 1000) - seconds * 1000;
         if (minutes > 0) {
            durationInString += minutes + " mins ";
         }
         if (seconds > 0) {
            durationInString += seconds + " sec ";
         }
         if (milliseconds >= 0) {
            durationInString += milliseconds + " msec";
         }
         return durationInString;
      } catch (Exception e) {
         e.printStackTrace();
         return "-1";
      }
   }
}
