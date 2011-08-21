package hudson.plugins.testng.results;

import hudson.model.Api;
import hudson.model.ModelObject;
import hudson.model.AbstractBuild;

import java.io.Serializable;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@SuppressWarnings("serial")
@ExportedBean
public abstract class BaseResult implements ModelObject, Serializable {

   protected AbstractBuild<?, ?> owner;
   protected final String name;
   protected BaseResult parent;

   public BaseResult(String name) {
      this.name = name;
   }

   /**
    * @deprecated since v0.21 keeping fullName here just to avoid errors when jenkins
    * deserializes result objects from XML
    */
   protected String fullName;

   @Exported(visibility = 999)
   public String getName() {
      return name;
   }

   public BaseResult getParent() {
      return parent;
   }

   public void setParent(BaseResult parent) {
      this.parent = parent;
   }

   public AbstractBuild<?, ?> getOwner() {
      return owner;
   }

   public void setOwner(AbstractBuild<?, ?> owner) {
      this.owner = owner;
   }

   public String getDisplayName() {
      return getName();
   }

   public String getUrl() {
      return getName();
   }

   public Api getApi() {
      return new Api(this);
   }

}
