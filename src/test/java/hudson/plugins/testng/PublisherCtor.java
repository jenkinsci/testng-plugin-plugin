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
    private int unstableSkips = 100;
    private int unstableFails = 0;
    private int failedSkips = 100;
    private int failedFails = 100;
    private int thresholdMode = 2; //default mode is 2 (percentage of tests)

    public Publisher getNewPublisher() {
        return new Publisher(reportFilenamePattern, escapeTestDescp, escapeExceptionMsg, showFailedBuilds, failureOnFailedTestConfig, 
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