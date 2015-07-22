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
    private boolean failureOnFailedTestConfig = false;
    private int unstableSkips = 1;
    private int unstableFails = 1;
    private int failedSkips = 1;
    private int failedFails = 1;
    private int thresholdMode = 1; //default mode is 1 (number of tests)

    public Publisher getNewPublisher() {
        return new Publisher(reportFilenamePattern, escapeTestDescp, failureOnFailedTestConfig, escapeExceptionMsg, showFailedBuilds,
                unstableSkips, unstableFails, failedSkips, failedFails, thresholdMode);
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

    public PublisherCtor setFailureOnFailedTestConfig(boolean failureOnFailedTestConfig) {
       this.failureOnFailedTestConfig = failureOnFailedTestConfig;
       return this;
    }

   public PublisherCtor setUnstableSkips(int unstableSkips) {
        this.unstableSkips = unstableSkips;
        return this;
    }

    public PublisherCtor setUnstableFails(int unstableFails) {
        this.unstableFails = unstableFails;
        return this;
    }

    public PublisherCtor setFailedSkips(int failedSkips) {
        this.failedSkips = failedSkips;
        return this;
    }

    public PublisherCtor setFailedFails(int failedFails) {
        this.failedFails = failedFails;
        return this;
    }

    public PublisherCtor setThresholdType(int thresholdMode) {
        this.thresholdMode = thresholdMode;
        return this;
    }
}
