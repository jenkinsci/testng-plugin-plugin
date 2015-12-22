package hudson.plugins.testng;

/**
 * Publisher constructor for help with tests
 *
 * @author nullin
 */
public class PublisherCtor {

    private String reportFilenamePattern = "**/testng-result.xml";
    private boolean escapeExceptionMsg = true;
    private boolean escapeTestDescp = true;
    private boolean showFailedBuilds = false;
    private boolean unstableOnSkippedTests = false;
    private boolean failureOnFailedTestConfig = false;
    private boolean ignoreTestFailureIfTestPassesOnce = false;

    public Publisher getNewPublisher() {
        return new Publisher(reportFilenamePattern, escapeTestDescp, escapeExceptionMsg, showFailedBuilds,
                unstableOnSkippedTests, failureOnFailedTestConfig, ignoreTestFailureIfTestPassesOnce);
    }

    public PublisherCtor setReportFilenamePattern(String reportFilenamePattern) {
        this.reportFilenamePattern = reportFilenamePattern;
        return this;
    }

    public PublisherCtor setEscapeExceptionMsg(boolean escapeExceptionMsg) {
        this.escapeExceptionMsg = escapeExceptionMsg;
        return this;
    }

    public PublisherCtor setEscapeTestDescp(boolean escapeTestDescp) {
        this.escapeTestDescp = escapeTestDescp;
        return this;
    }

    public PublisherCtor setShowFailedBuilds(boolean showFailedBuilds) {
        this.showFailedBuilds = showFailedBuilds;
        return this;
    }

    public PublisherCtor setUnstableOnSkippedTests(boolean unstableOnSkippedTests) {
        this.unstableOnSkippedTests = unstableOnSkippedTests;
        return this;
    }

    public PublisherCtor setFailureOnFailedTestConfig(boolean failureOnFailedTestConfig) {
        this.failureOnFailedTestConfig = failureOnFailedTestConfig;
        return this;
    }

    public PublisherCtor setIgnoreTestFailureIfTestPassesOnceConfig(boolean ignoreTestFailureIfTestPassesOnce) {
        this.ignoreTestFailureIfTestPassesOnce = ignoreTestFailureIfTestPassesOnce;
        return this;
    }
}
