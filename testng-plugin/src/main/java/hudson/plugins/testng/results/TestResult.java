package hudson.plugins.testng.results;

import hudson.model.ModelObject;
import hudson.model.AbstractBuild;

import java.util.List;

public class TestResult extends BaseResult implements ModelObject {
   private AbstractBuild<?, ?> owner;
   private List<ClassResult> classList;

   public void setOwner(AbstractBuild<?, ?> owner) {
      this.owner = owner;
   }

   public AbstractBuild<?, ?> getOwner() {
      return owner;
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
