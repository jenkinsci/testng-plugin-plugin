package hudson.plugins.testng.results;

import hudson.model.AbstractBuild;

public abstract class BaseResult {
   protected AbstractBuild<?, ?> owner;
   protected String name;
   protected BaseResult parent;
   protected String fullName;

   public String getFullName() {
      return fullName;
   }

   public void setFullName(String fullName) {
      this.fullName = fullName;
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
