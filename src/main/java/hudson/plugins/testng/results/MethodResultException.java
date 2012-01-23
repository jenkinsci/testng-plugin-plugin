package hudson.plugins.testng.results;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MethodResultException implements Serializable {

   private String exceptionName;
   private String message;
   private String stackTrace;
   /**
    * @deprecated since v0.21
    */
   private String shortStackTrace;
   /**
    * @deprecated since v0.21
    */
   private String fullStackTrace;

   public MethodResultException(String message,
        String shortStackTrace,
        String fullStackTrace)
   {
      this.message = message == null ? null : message.trim();
      trySettingData(shortStackTrace, fullStackTrace);
   }

   public String getExceptionName() {
      if (exceptionName == null) {
        trySettingData(shortStackTrace, fullStackTrace);
      }
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
     int index = -1;

     if (message == null) {
       //no message means first line will only show exception class name
       index = stackTrace.indexOf("\n");
       if (index != -1) {
         exceptionName = stackTrace.substring(0, index);
         stackTrace = stackTrace.substring(index + 1, stackTrace.length());
       }
     } else {
       message = message.trim();
       //message being present means first line will be of type
       //<exception class name>: <message>
       index = stackTrace.indexOf(": ");
       if (index != -1) {
         exceptionName = stackTrace.substring(0, index);
         stackTrace = stackTrace.substring(index + 2, stackTrace.length()).replace(message, "");
       }
     }
   }

   public String getMessage() {
      return message;
   }

   public String getStackTrace() {
      if (stackTrace == null) {
        trySettingData(shortStackTrace, fullStackTrace);
      }
      return stackTrace;
   }

   public String toString() {
      StringBuffer str = new StringBuffer();
      str.append(exceptionName).append(": ");
      if (message != null) {
         str.append(message);
      }
      str.append("\n");
      str.append(stackTrace);
      return str.toString();
   }
}
