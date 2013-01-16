package hudson.plugins.testng.results;

import hudson.model.AbstractBuild;
import hudson.plugins.testng.util.FormatUtil;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.Exported;

import java.util.*;

/**
 * Handles package level results
 */
@SuppressWarnings("serial")
public class PackageResult extends BaseResult {

    //TODO: switch to using a Map instead of List
    //list of all classes run from this package
    private List<ClassResult> classList = new ArrayList<ClassResult>();

    //cached
    private List<MethodResult> sortedTestMethodsByStartTime = null;

    //cached vars updated using tally method
    private transient float duration;
    private transient int fail;
    private transient int skip;
    private transient int pass;

    // Maximum size of methods in the method execution list
    public final int MAX_EXEC_MTHD_LIST_SIZE = 25;

    public PackageResult(String name) {
        super(name);
    }

    @Override
    public void setOwner(AbstractBuild<?, ?> owner) {
        super.setOwner(owner);
        for (ClassResult _class : classList) {
            _class.setOwner(owner);
        }
    }

    /**
     * Can't change this to return seconds as expected by {@link hudson.tasks.test.TestObject} because
     * it has already been exported
     *
     * @return duration in milliseconds
     */
    @Exported
    @Override
    public float getDuration() {
        return duration;
    }

    @Override
    @Exported(visibility = 9, name = "fail")
    public int getFailCount() {
        return fail;
    }

    @Override
    @Exported(visibility = 9, name = "skip")
    public int getSkipCount() {
        return skip;
    }

    @Override
    @Exported(visibility = 9)
    public int getTotalCount() {
        return super.getTotalCount();
    }

    @Override
    public int getPassCount() {
        return pass;
    }

    /**
     * Gets all the method results related to this package sorted by the time
     * the methods were executed
     *
     * @return
     */
    public List<MethodResult> getSortedTestMethodsByStartTime() {
        if (sortedTestMethodsByStartTime == null) {
            sortTestMethods();
        }
        return sortedTestMethodsByStartTime;
    }

    /**
     * Gets table row representation for all the method results associated with
     * this package (sorted based on start time)
     *
     * @return
     */
    @JavaScriptMethod
    public String getAllSortedTestMethodsByStartTime() {
        return getMethodExecutionTableContent(getSortedTestMethodsByStartTime());
    }

    /**
     * Gets table row representation for the first {@link #MAX_EXEC_MTHD_LIST_SIZE}
     * method results associated with this package (sorted based on start time)
     *
     * @return
     */
    @JavaScriptMethod
    public String getFirstXSortedTestMethodsByStartTime() {
        //returning the first MAX results only
        List<MethodResult> list = getSortedTestMethodsByStartTime();
        list = list.subList(0, list.size() > MAX_EXEC_MTHD_LIST_SIZE
                ? MAX_EXEC_MTHD_LIST_SIZE : list.size());
        return getMethodExecutionTableContent(list);
    }

    /**
     * Gets the table row representation for the specified method results
     *
     * @param mrList list of method result objects
     * @return table row representation
     */
    private String getMethodExecutionTableContent(List<MethodResult> mrList) {
        StringBuffer sb = new StringBuffer(mrList.size() * 100);

        for (MethodResult mr : mrList) {
            sb.append("<tr><td align=\"left\">");
            sb.append("<a href=\"").append(mr.getUpUrl()).append("\">");
            sb.append(mr.getParent().getName()).append(".").append(mr.getName());
            sb.append("</a>");
            sb.append("</td><td align=\"center\">");
            sb.append(FormatUtil.formatTime(mr.getDuration()));
            sb.append("</td><td align=\"center\">");
            sb.append(mr.getStartedAt());
            sb.append("</td><td align=\"center\"><span class=\"").append(mr.getCssClass()).append("\">");
            sb.append(mr.getStatus());
            sb.append("</span></td></tr>");
        }
        return sb.toString();
    }

    @Override
    public void tally() {
        duration = 0;
        fail = 0;
        skip = 0;
        pass = 0;
        for (ClassResult _c : classList) {
            _c.setParent(this);
            _c.tally();
            duration += _c.getDuration();
            fail += _c.getFailCount();
            skip += _c.getSkipCount();
            pass += _c.getPassCount();
        }
    }

    /**
     * Sorts the test method results associated with this package based on the
     * start time for method execution
     */
    public void sortTestMethods() {
        this.sortedTestMethodsByStartTime = new ArrayList<MethodResult>();
        //for each class
        Map<Date, List<MethodResult>> map = new HashMap<Date, List<MethodResult>>();
        for (ClassResult aClass : classList) {
            if (aClass.getTestMethods() != null) {
                for (MethodResult aMethod : aClass.getTestMethods()) {
                    Date startDate = aMethod.getStartedAt();
                    if (!aMethod.getStatus().equalsIgnoreCase("skip")
                            && startDate != null) {
                        if (map.containsKey(startDate)) {
                            map.get(startDate).add(aMethod);
                        } else {
                            List<MethodResult> list = new ArrayList<MethodResult>();
                            list.add(aMethod);
                            map.put(startDate, list);
                        }
                    }
                }
            }
        }
        List<Date> keys = new ArrayList<Date>(map.keySet());
        Collections.sort(keys);
        //now create the list with the order
        for (Date key : keys) {
            if (map.containsKey(key)) {
                this.sortedTestMethodsByStartTime.addAll(map.get(key));
            }
        }
    }

    @Override
    @Exported(name = "classs") // because stapler notices suffix 's' and remove it
    public List<ClassResult> getChildren() {
        return classList;
    }

    @Override
    public boolean hasChildren() {
        return classList != null && !classList.isEmpty();
    }

}
