package hudson.plugins.testng.results;

import hudson.model.AbstractBuild;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class BaseResult implements Serializable {

   protected AbstractBuild<?, ?> owner;
   protected String name;
   protected BaseResult parent;

   /*
    * keeping fullName here just to avoid errors when jenkins
    * deserializes result objects from XML
    */
   @Deprecated
   protected String fullName;

   public String getFullName() {
      return fullName;
   }

   public void setParent(BaseResult parent) {
      this.parent = parent;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public BaseResult getParent() {
      return parent;
   }

   public AbstractBuild<?, ?> getOwner() {
      return owner;
   }

   public void setOwner(AbstractBuild<?, ?> owner) {
      this.owner = owner;
   }

}
