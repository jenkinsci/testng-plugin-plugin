package hudson.plugins.testng.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hudson.model.AbstractBuild;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;

/**
 * Handle results related to a single test class
 */
@SuppressWarnings("serial")
public class ClassResult extends BaseResult {

    private List<MethodResult> testMethodList = new ArrayList<MethodResult>();

    //cache
    private Map<String, GroupedTestRun> testRunMap = null;

    //cached values, updated via tally
    private transient float duration;
    private transient int fail;
    private transient int skip;
    private transient int pass;

    public ClassResult(String name) {
        super(name);
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
    public void setOwner(AbstractBuild<?, ?> owner) {
        super.setOwner(owner);
        for (MethodResult _m : this.testMethodList) {
            _m.setOwner(owner);
        }
    }

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

    public void addTestMethods(List<MethodResult> list) {
        this.testMethodList.addAll(list);
    }

    public void tally() {
        this.duration = 0;
        this.fail = 0;
        this.skip = 0;
        this.pass = 0;
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
            this.duration += methodResult.getDuration();
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
