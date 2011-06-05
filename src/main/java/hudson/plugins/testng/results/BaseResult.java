package hudson.plugins.testng.results;

import hudson.model.ModelObject;
import hudson.model.AbstractBuild;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class BaseResult implements ModelObject, Serializable {

   protected AbstractBuild<?, ?> owner;
   protected String name;
   protected BaseResult parent;

   /**
    * @deprecated since v0.21 keeping fullName here just to avoid errors when jenkins
    * deserializes result objects from XML
    */
   protected String fullName;

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

}
