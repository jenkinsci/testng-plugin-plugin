package hudson.plugins.testng;

import java.io.PrintStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Api;
import hudson.plugins.testng.parser.ResultsParser;
import hudson.plugins.testng.results.MethodResult;
import hudson.plugins.testng.results.TestNGResult;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.test.AbstractTestResultAction;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * TestNG build action that exposes the results per build
 *
 * @author nullin
 * @since v1.0
 */
public class TestNGTestResultBuildAction extends AbstractTestResultAction implements Serializable {

    /**
     * Unique identifier for this class.
     */
    private static final long serialVersionUID = 31415926L;

    /**
     * try and be good citizen. We don't want to hold this in memory.
     * We also don't want to save this to build XML as we already save testng Reports
     */
    private transient Reference<TestNGResult> testngResultRef;

    /*
     * Cache test counts to speed up loading of graphs
     */
    protected transient int passCount;
    protected transient int failCount;
    protected transient int skipCount;

    public TestNGTestResultBuildAction(AbstractBuild<?, ?> build, TestNGResult testngResults) {
        super(build);

        if (testngResults != null) {
            testngResults.setOwner(build);
            this.testngResultRef = new WeakReference<TestNGResult>(testngResults);

            //initialize the cached values when TestNGBuildAction is instantiated
            this.passCount = testngResults.getPassCount();
            this.failCount = testngResults.getFailCount();
            this.skipCount = testngResults.getSkipCount();
        }
    }

    @Override
    public TestNGResult getResult() {
        return getResult(super.owner);
    }

    public TestNGResult getResult(AbstractBuild build) {
        if (testngResultRef == null) {
            testngResultRef = new WeakReference<TestNGResult>(loadResults(build, null));
            return testngResultRef.get();
        }

        TestNGResult tr = testngResultRef.get();
        if (tr == null) {
            testngResultRef = new WeakReference<TestNGResult>(loadResults(build, null));
            return testngResultRef.get();
        } else {
            return tr;
        }
    }

    static TestNGResult loadResults(AbstractBuild<?, ?> owner, PrintStream logger) {
        FilePath testngDir = Publisher.getTestNGReport(owner);
        FilePath[] paths = null;
        try {
            paths = testngDir.list("testng-results*.xml");
        } catch (Exception e) {
            //do nothing
        }

        if (paths == null) {
            TestNGResult tr = new TestNGResult();
            tr.setOwner(owner);
            return tr;
        }

        ResultsParser parser = new ResultsParser(logger);
        TestNGResult result = parser.parse(paths);
        result.setOwner(owner);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconFileName() {
        return PluginImpl.ICON_FILE_NAME;
    }

    @Override
    public int getFailCount() {
        return failCount;
    }

    @Override
    public int getSkipCount() {
        return skipCount;
    }

    @Override
    public int getTotalCount() {
        return failCount + passCount + skipCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return PluginImpl.DISPLAY_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrlName() {
        return PluginImpl.URL;
    }

    public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
        return getResult().getDynamic(token, req, rsp);
    }

    @Override
    public Api getApi() {
        return new Api(getResult());
    }

    /**
     * {@inheritDoc}
     * <p/>
     * NOTE: Executed when build action is read from disk - e.g. on Jenkins startup
     */
    @Override
    public Object readResolve() {
        super.readResolve();

        TestNGResult testResults = getResult();

        //initialize the cached values
        passCount = testResults.getPassCount();
        failCount = testResults.getFailCount();
        skipCount = testResults.getSkipCount();

        return this;
    }

    public List<CaseResult> getFailedTests() {

        class HackyCaseResult extends CaseResult {

            private MethodResult methodResult;

            public HackyCaseResult(MethodResult methodResult) {
                super(null, methodResult.getDisplayName(), methodResult.getErrorStackTrace());
                this.methodResult = methodResult;
            }

            public Status getStatus() {
                //We don't calculate age of results currently
                //so, can't state if the failure is a regression or not
                return Status.FAILED;
            }

            public String getClassName() {
                return methodResult.getClassName();
            }

            public String getDisplayName() {
                return methodResult.getDisplayName();
            }

            public String getErrorDetails() {
                return methodResult.getErrorDetails();
            }
        }

        List< CaseResult > results = new ArrayList<CaseResult>(getFailCount());
        for (MethodResult methodResult : getResult().getFailedTests()) {
            results.add(new HackyCaseResult(methodResult));
        }

        return results;
    }
}
