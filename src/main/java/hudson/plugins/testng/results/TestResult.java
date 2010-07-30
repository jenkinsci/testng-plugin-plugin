package hudson.plugins.testng.results;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;

import java.util.List;

public class TestResult implements ModelObject {
   private AbstractBuild<?, ?> owner;
   String name;
   List<ClassResult> classList;

   public void setOwner(AbstractBuild<?, ?> owner) {
      this.owner = owner;
   }

   public AbstractBuild<?, ?> getOwner() {
      return owner;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public List<ClassResult> getClassList() {
      return classList;
   }

   public void setClassList(List<ClassResult> classList) {
      this.classList = classList;
   }

   public String getDisplayName() {
      return getName();
   }

}
