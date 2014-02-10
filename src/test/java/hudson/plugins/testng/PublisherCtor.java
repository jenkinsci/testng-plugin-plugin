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

    public Publisher getNewPublisher() {
        return new Publisher(reportFilenamePattern, escapeTestDescp, escapeExceptionMsg, showFailedBuilds, unstableOnSkippedTests);
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
}
