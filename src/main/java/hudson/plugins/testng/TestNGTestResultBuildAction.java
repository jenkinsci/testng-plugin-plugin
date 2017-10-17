package hudson.plugins.testng;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.FilePath;
import hudson.model.Api;
import hudson.model.Run;
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

    private static final Logger LOGGER = Logger.getLogger(TestNGTestResultBuildAction.class.getName());

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
    protected Integer passCount; // null if uncomputed
    protected int failCount;
    protected int skipCount;

    public TestNGTestResultBuildAction(TestNGResult testngResults) {
        if (testngResults != null) {
            this.testngResultRef = new WeakReference<TestNGResult>(testngResults);

            //initialize the cached values when TestNGBuildAction is instantiated
            count(testngResults);
        }
    }

    private void count(TestNGResult testngResults) {
        this.passCount = testngResults.getPassCount();
        this.failCount = testngResults.getFailCount();
        this.skipCount = testngResults.getSkipCount();
    }

    private void countAndSave(TestNGResult testngResults) {
        int savedPassCount = passCount != null ? passCount : -1;
        int savedFailCount = failCount;
        int savedSkipCount = skipCount;
        count(testngResults);
        if (passCount != savedPassCount || failCount != savedFailCount || skipCount != savedSkipCount) {
            LOGGER.log(Level.FINE, "saving {0}", owner);
            try {
                owner.save();
            } catch (IOException x) {
                LOGGER.log(Level.WARNING, "failed to save " + owner, x);
            }
        }
    }

    private void countAsNeeded() {
        if (passCount == null) {
            countAndSave(getResult());
        }
    }

    @Override
    public TestNGResult getResult() {
        return getResult(super.run);
    }

    public TestNGResult getResult(Run build) {
        TestNGResult tr = testngResultRef != null ? testngResultRef.get() : null;
        if (tr == null) {
            tr = loadResults(build, null);
            countAndSave(tr);
            testngResultRef = new WeakReference<TestNGResult>(tr);
        }
        return tr;
    }

    static TestNGResult loadResults(Run<?, ?> owner, PrintStream logger) {
        LOGGER.log(Level.FINE, "loading results for {0}", owner);
        FilePath testngDir = Publisher.getTestNGReport(owner);
        FilePath[] paths = null;
        try {
            paths = testngDir.list("testng-results*.xml");
        } catch (Exception e) {
            //do nothing
        }

        if (paths == null) {
            TestNGResult tr = new TestNGResult();
            tr.setRun(owner);
            return tr;
        }

        ResultsParser parser = new ResultsParser(logger);
        TestNGResult result = parser.parse(paths);
        result.setRun(owner);
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
        countAsNeeded();
        return failCount;
    }

    @Override
    public int getSkipCount() {
        countAsNeeded();
        return skipCount;
    }

    @Override
    public int getTotalCount() {
        countAsNeeded();
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
