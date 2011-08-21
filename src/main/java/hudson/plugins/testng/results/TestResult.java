package hudson.plugins.testng.results;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a single TestNG XML <test> tag
 *
 * @author nullin
 *
 */
public class TestResult extends BaseResult {

   private List<ClassResult> classList = new ArrayList<ClassResult>();

   public TestResult(String name) {
      super(name);
   }

   public List<ClassResult> getClassList() {
      return classList;
   }

   /**
    * Adds only the classes that already aren't part of the list
    * @param classList
    */
   public void addClassList(List<ClassResult> classList) {
     Set<ClassResult> tmpSet = new HashSet<ClassResult>(this.classList);
     tmpSet.addAll(classList);
     this.classList = new ArrayList<ClassResult>(tmpSet);
   }

}
