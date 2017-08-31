package hudson.plugins.testng.results;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;

@SuppressFBWarnings(value="NM_CLASS_NOT_EXCEPTION",
        justification="Not an exception, but represents one associated with a test result parsed by this plugin")
@SuppressWarnings("serial")
public class MethodResultException implements Serializable {

   private String exceptionName;
   private String message;
   private String stackTrace;

   public MethodResultException(String exceptionName,
        String message,
        String shortStackTrace,
        String fullStackTrace)
   {
      this.message = message == null ? null : message.trim();
      this.exceptionName = exceptionName;
      trySettingData(shortStackTrace, fullStackTrace);
   }

   public String getExceptionName() {
      return exceptionName;
   }

   /**
    * Magic:
    * We changed this class to remove short st and full st fields but folks will have
    * previous builds which will have this data. With those builds, the report will
    * display incomplete information. So, to show complete information when fields
    * are unmarshalled from xml by jenkins, we go ahead and attempt to gather this information
    * and update the new fields that are now used in the UI.
    *
    * Works on/updates instance variables.
    *
    * @param shortStackTrace
    * @param fullStackTrace
    */
   private void trySettingData(String shortStackTrace, String fullStackTrace)
   {
     String tmpStackTrace = shortStackTrace;
     if (((shortStackTrace == null) || "".equals(shortStackTrace)) && (fullStackTrace != null)) {
        // overwrite short st with full st, if available
        tmpStackTrace = fullStackTrace;
     }

     stackTrace = tmpStackTrace.trim();
     int index;

     if (message == null) {
       //no message means first line will only show exception class name
       index = stackTrace.indexOf("\n");
       if (index != -1) {
         if (exceptionName == null || exceptionName.isEmpty()) {
            exceptionName = stackTrace.substring(0, index);
         }
         stackTrace = stackTrace.substring(index + 1, stackTrace.length());
       }
     } else {
       message = message.trim();
       //message being present means first line will be of type
       //<exception class name>: <message>
       index = stackTrace.indexOf(": ");
       if (index != -1) {
         if (exceptionName == null || exceptionName.isEmpty()) {
            exceptionName = stackTrace.substring(0, index);
         }
         stackTrace = stackTrace.substring(index + 2, stackTrace.length()).replace(message, "");
       }
     }
   }

   public String getMessage() {
      return message;
   }

   public String getStackTrace() {
      return stackTrace;
   }

   public String toString() {
      StringBuilder str = new StringBuilder();
      str.append(exceptionName).append(": ");
      if (message != null) {
         str.append(message);
      }
      str.append("\n");
      str.append(stackTrace);
      return str.toString();
   }
}
