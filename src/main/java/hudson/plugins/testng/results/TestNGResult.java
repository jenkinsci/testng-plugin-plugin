package hudson.plugins.testng.results;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.util.*;

import hudson.model.Run;
import hudson.plugins.testng.PluginImpl;
import org.kohsuke.stapler.export.Exported;

/**
 * Represents all the results gathered for a single build (or a single suite,
 * while parsing the test results)
 *
 * @author nullin
 * @author farshidce
 */
@SuppressFBWarnings(value="SE_BAD_FIELD", justification="ArrayList is Serializable")
public class TestNGResult extends BaseResult implements Serializable {

    private static final long serialVersionUID = -3491974223665601995L;
    private List<TestNGTestResult> testList = new ArrayList<TestNGTestResult>();
    private List<MethodResult> passedTests = new ArrayList<MethodResult>();
    private List<MethodResult> failedTests = new ArrayList<MethodResult>();
    private List<MethodResult> skippedTests = new ArrayList<MethodResult>();
    private List<MethodResult> failedConfigurationMethods = new ArrayList<MethodResult>();
    private List<MethodResult> skippedConfigurationMethods = new ArrayList<MethodResult>();
    private long startTime;
    private long endTime;
    private int passCount;
    private int failCount;
    private int skipCount;
    private int failedConfigCount;
    private int skippedConfigCount;
    private Map<String, PackageResult> packageMap = new HashMap<String, PackageResult>();

    /**
     * @param name input name is ignored
     * @deprecated don't use this constructor
     */
    public TestNGResult(String name) {
        super(PluginImpl.URL);
    }

    public TestNGResult() {
        super(PluginImpl.URL);
    }

    @Override
    public String getTitle() {
        return getDisplayName();
    }

    @Override
    public List<MethodResult> getFailedTests() {
        return failedTests;
    }

    @Override
    public List<MethodResult> getPassedTests() {
        return passedTests;
    }

    @Override
    public List<MethodResult> getSkippedTests() {
        return skippedTests;
    }

    public List<MethodResult> getFailedConfigs() {
        return failedConfigurationMethods;
    }

    public List<MethodResult> getSkippedConfigs() {
        return skippedConfigurationMethods;
    }

    /**
     * Gets the total number of passed tests.
     */
    public int getPassCount() {
        return passCount;
    }

    /**
     * Gets the total number of failed tests.
     */
    @Exported
    public int getFailCount() {
        return failCount;
    }

    /**
     * Gets the total number of skipped tests.
     */
    @Exported
    public int getSkipCount() {
        return skipCount;
    }

    public List<TestNGTestResult> getTestList() {
        return testList;
    }

    @Exported(name = "total")
    public int getTotalCount() {
        return super.getTotalCount();
    }

    @Exported
    @Override
    public float getDuration() {
        return (float) (endTime - startTime) / 1000f;
    }

    @Exported(name = "fail-config")
    public int getFailedConfigCount() {
        return failedConfigCount;
    }

    @Exported(name = "skip-config")
    public int getSkippedConfigCount() {
        return skippedConfigCount;
    }

    @Exported(name = "package")
    public Collection<PackageResult> getPackageList() {
        return packageMap.values();
    }

    public Map<String, PackageResult> getPackageMap() {
        return packageMap;
    }

    public Set<String> getPackageNames() {
        return packageMap.keySet();
    }

    /**
     * Adds only the {@code <test>}s that already aren't part of the list.
     *
     * @param testList
     */
    //TODO: whats going on here? why unique?
    public void addUniqueTests(List<TestNGTestResult> testList) {
        Set<TestNGTestResult> tmpSet = new HashSet<TestNGTestResult>(this.testList);
        tmpSet.addAll(testList);
        this.testList = new ArrayList<TestNGTestResult>(tmpSet);
    }

    public void setRun(Run<?, ?> run) {
        this.run = run;
        for (PackageResult pkg : packageMap.values()) {
            pkg.setRun(run);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestNGResult testngResult = (TestNGResult) o;
        return run == null ? testngResult.run == null
                : run.equals(testngResult.run);
    }

    @Override
    public int hashCode() {
        int result;
        result = (run != null ? run.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("TestNGResult {" +
                "totalTests=%d, " +
                "failedTests=%d, skippedTests=%d, failedConfigs=%d, " +
                "skippedConfigs=%d}", //name,
                passCount + failCount + skipCount, failCount,
                skipCount, failedConfigCount,
                skippedConfigCount);
    }

    /**
     * Updates the calculated fields
     */
    @Override
    public void tally() {
        failedConfigCount = failedConfigurationMethods.size();
        skippedConfigCount = skippedConfigurationMethods.size();
        failCount = failedTests.size();
        passCount = passedTests.size();
        skipCount = skippedTests.size();

        packageMap.clear();
        for (TestNGTestResult _test : testList) {
            for (ClassResult _class : _test.getClassList()) {
                String pkg = _class.getPkgName();
                if (packageMap.containsKey(pkg)) {
                    List<ClassResult> classResults = packageMap.get(pkg).getChildren();
                    if (!classResults.contains(_class)) {
                        classResults.add(_class);
                    }
                } else {
                    PackageResult tpkg = new PackageResult(pkg);
                    tpkg.getChildren().add(_class);
                    tpkg.setParent(this);
                    packageMap.put(pkg, tpkg);
                }
            }
        }

        startTime = Long.MAX_VALUE;
        endTime = 0;
        for (PackageResult pkgResult : packageMap.values()) {
            pkgResult.tally();
            if (this.startTime > pkgResult.getStartTime()) {
                startTime = pkgResult.getStartTime(); //cf. ClassResult#tally()
            }
            if (this.endTime < pkgResult.getEndTime()) {
                endTime = pkgResult.getEndTime();
            }
        }
    }

    @Exported(visibility = 999)
    public String getName() {
        return name;
    }

    @Override
    public BaseResult getParent() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public Collection<PackageResult> getChildren() {
        return packageMap.values();
    }

    @Override
    public boolean hasChildren() {
        return !packageMap.isEmpty();
    }
}
