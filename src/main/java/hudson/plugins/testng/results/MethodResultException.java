package hudson.plugins.testng.results;

import java.io.Serializable;

public class MethodResultException implements Serializable {

   private String message;
   private String shortStackTrace;
   private String fullStackTrace;

   public String getMessage() {
      return message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public String getShortStackTrace() {
      return shortStackTrace;
   }

   public void setShortStackTrace(String shortStackTrace) {
      this.shortStackTrace = shortStackTrace;
   }

   public String getFullStackTrace() {
      return fullStackTrace;
   }

   public void setFullStackTrace(String fullStackTrace) {
      this.fullStackTrace = fullStackTrace;
   }
}
