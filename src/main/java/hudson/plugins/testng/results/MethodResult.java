package hudson.plugins.testng.results;

import hudson.model.ModelObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.util.Date;

public class MethodResult extends BaseResult implements ModelObject {
   private String status;
   private String description;
   private boolean isConfig;
   private long duration;
   private MethodResultException exception;
   private Date startedAt;


   public Date getStartedAt() {
      return startedAt;
   }

   public void setStartedAt(Date startedAt) {
      this.startedAt = startedAt;
   }

   public String getFullUrl() {
      return super.getParent().getParent().getName()
            + "/" + super.getParent().getName() + "/" + getUrl();
   }

   public MethodResultException getException() {
      return exception;
   }

   public void setException(MethodResultException exception) {
      this.exception = exception;
   }

   public String getUrl() {
      return getName();
   }

   public long getDuration() {
      return duration;
   }

   public void setDuration(long duration) {
      this.duration = duration;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public boolean isConfig() {
      return isConfig;
   }

   public void setConfig(boolean isConfig) {
      this.isConfig = isConfig;
   }

   public String getDisplayName() {
      return getName();
   }

   public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
      if (token.equals("/" + getName())) {
         return this;
      }

      return null;
   }

   public Object getCssClass() {
      if (this.status != null) {
         if(this.status.equalsIgnoreCase("pass")) {
            return "result-passed";
         } else if(this.status.equalsIgnoreCase("skip")) {
           return "result-skipped";
         } else if(this.status.equalsIgnoreCase("fail")) {
            return "result-failed";
         }
         
      }
      return "result-passed";
   }
}
