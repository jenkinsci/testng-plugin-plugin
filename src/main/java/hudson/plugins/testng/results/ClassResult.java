package hudson.plugins.testng.results;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hudson.model.Run;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;

/**
 * Handle results related to a single test class
 */
@SuppressFBWarnings(value="SE_NO_SERIALVERSIONID", justification="XStream does not care")
@SuppressWarnings("serial")
public class ClassResult extends BaseResult {

    private String pkgName; //name of package containing this class
    private List<MethodResult> testMethodList = new ArrayList<MethodResult>();

    //cache
    @SuppressFBWarnings(value="SE_BAD_FIELD", justification="HashMap is Serializable")
    private Map<String, GroupedTestRun> testRunMap = null;

    //cached values, updated via tally
    private transient long startTime;
    private transient long endTime;
    private transient int fail;
    private transient int skip;
    private transient int pass;

    public ClassResult(String pkgName, String name) {
        super(name);
        this.pkgName = pkgName;
    }

    public String getPkgName() {
        return pkgName;
    }

    public String getCanonicalName() {
        if (PackageResult.NO_PKG_NAME.equals(pkgName)) {
            return getName();
        } else {
            return pkgName + "." + getName();
        }
    }

    /**
     * Called only from jelly file
     *
     * @return
     */
    public Map<String, GroupedTestRun> getTestRunMap() {
        if (testRunMap != null) {
            return testRunMap;
        }
        //group all the test methods based on their run
        testRunMap = new HashMap<String, GroupedTestRun>();
        for (MethodResult methodResult : this.testMethodList) {
            String methodTestRunId = methodResult.getTestRunId();
            GroupedTestRun group;
            if (this.testRunMap.containsKey(methodTestRunId)) {
                group = this.testRunMap.get(methodTestRunId);
            } else {
                group = new GroupedTestRun(methodTestRunId,
                        methodResult.getParentTestName(),
                        methodResult.getParentSuiteName());
                this.testRunMap.put(methodTestRunId, group);
            }

            if (methodResult.isConfig()) {
                group.addConfigurationMethod(methodResult);
            } else {
                group.addTestMethod(methodResult);
            }
        }
        return testRunMap;
    }

    @Override
    public void setRun(Run<?, ?> run) {
        super.setRun(run);
        for (MethodResult _m : this.testMethodList) {
            _m.setRun(run);
        }
    }

    @Exported
    @Override
    public float getDuration() {
        return (endTime - startTime) / 1000f;
    }

    public long getStartTime() {
        return startTime; //in ms
    }

    public long getEndTime() {
        return endTime; //in ms
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

    public void addTestMethods(List<MethodResult> list) {
        this.testMethodList.addAll(list);
    }

    public void tally() {
        this.fail = 0;
        this.skip = 0;
        this.pass = 0;
        this.startTime = Long.MAX_VALUE; //start with max
        this.endTime = 0; //start with min
        Map<String, Integer> methodInstanceMap = new HashMap<String, Integer>();

        for (MethodResult methodResult : this.testMethodList) {
            if (!methodResult.isConfig()) {
                if ("FAIL".equals(methodResult.getStatus())) {
                    this.fail++;
                } else if ("SKIP".equals(methodResult.getStatus())) {
                    this.skip++;
                } else {
                    this.pass++;
                }
            }
            /*
            It's possible that timestamps were not parsed correctly, so check for -1 values.
            And then, we check for the oldest start time and latest end time to figure out
            time taken to execute a class. (Same would be done for PackageResults and
            TestNGResults as well.

            Note that this helps give a better idea of time taken for test execution when
            tests were run in parallel, but still doesn't give a good picture when all tests
            within a class finished execute within a second, because millisecond information
            is not available on the start time of method execution.
             */
            long timestamp = methodResult.getStartTime();
            if (timestamp != -1 && this.startTime > timestamp) {
                startTime = timestamp;
            }
            timestamp = methodResult.getEndTime();
            if (timestamp != -1 && this.endTime < timestamp) {
                endTime = timestamp;
            }
            methodResult.setParent(this);
            /*
             * Setup testUuids to ensure that methods with same names can be
             * reached using unique urls
             */
            String methodName = methodResult.getName();
            if (methodInstanceMap.containsKey(methodName)) {
                int currIdx = methodInstanceMap.get(methodName);
                methodResult.setTestUuid(String.valueOf(++currIdx));
                methodInstanceMap.put(methodName, currIdx);
            } else {
                methodInstanceMap.put(methodName, 0);
            }
        }
    }

    /*
        Overriding because instead of comparing token to name, for methods,
        we need to compare it with the safe name (which includes testUuid, if
        applicable)
     */
    @Override
    public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
        if (this.testMethodList != null) {
            for (MethodResult methodResult : this.testMethodList) {
                //append the uuid as well
                if (token.equals(methodResult.getSafeName())) {
                    return methodResult;
                }
            }
        }
        return null;
    }

    @Exported(name = "test-method")
    public List<MethodResult> getTestMethods() {
        List<MethodResult> list = new ArrayList<MethodResult>();
        for (MethodResult methodResult : this.testMethodList) {
            if (!methodResult.isConfig()) {
                list.add(methodResult);
            }
        }
        return list;
    }

    public List<MethodResult> getConfigurationMethods() {
        List<MethodResult> list = new ArrayList<MethodResult>();
        for (MethodResult methodResult : this.testMethodList) {
            if (methodResult.isConfig()) {
                list.add(methodResult);
            }
        }
        return list;
    }

    @Override
    public List<MethodResult> getChildren() {
        return testMethodList;
    }

    @Override
    public boolean hasChildren() {
        return testMethodList != null && !testMethodList.isEmpty();
    }

}
