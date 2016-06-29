package hudson.plugins.testng.results;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a single TestNG XML {@code <test>} tag.
 *
 * @author nullin
 */
public class TestNGTestResult {

    //list of test classes
    private List<ClassResult> classList = new ArrayList<ClassResult>();

    //<test name="####">
    private String name;

    public TestNGTestResult(String name) {
        this.name = name;
    }

    public List<ClassResult> getClassList() {
        return classList;
    }

    public String getName() {
        return name;
    }

    /**
     * Adds only the classes that already aren't part of the list
     *
     * @param classList list of class results
     */
    public void addClassList(List<ClassResult> classList) {
        Set<ClassResult> tmpSet = new HashSet<ClassResult>(this.classList);
        tmpSet.addAll(classList);
        this.classList = new ArrayList<ClassResult>(tmpSet);
    }

}
