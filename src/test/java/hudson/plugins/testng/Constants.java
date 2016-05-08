package hudson.plugins.testng;

/**
 * Some constants
 *
 * @author nullin
 */
public class Constants {

    /**
     * data provider tests specific
     */
    public static final String TESTNG_XML_DATAPROVIDER = "testng-dp-result.xml";
    /**
     * Empty exception stack trace
     */
    public static final String TESTNG_XML_EMPTY_EXCEPTION = "testng-empty-exp.xml";
    /**
     * Reporter log output for complete suite as well as
     * test method specific output
     */
    public static final String TESTNG_XML_REPORTER_LOG_OUTPUT = "testng-reporter-log-result.xml";
    /**
     * Some test results for tests run as smoke tests
     */
    public static final String TESTNG_XML_PRECHECKINS = "testng-precheckins.xml";
    /**
     * A test method where instance name is specified
     */
    public static final String TESTNG_XML_INSTANCE_NAME = "testng-instance-name.xml";
    /**
     * Two <test> tags have the same value for name attribute
     */
    public static final String TESTNG_XML_SAME_TEST_NAME = "testng-same-test-name.xml";
    /**
     * XML generated on running unit test suites for TestNG
     */
    public static final String TESTNG_XML_TESTNG = "testng-results-testng.xml";
    /**
     * Exception message has HTML tags
     */
    public static final String TESTNG_XML_EXP_MSG_XML = "testng-xml-exp-msg.xml";
    /**
     * Test description has HTML tags
     */
    public static final String TESTNG_DESCRIPTION_HTML = "testng-description-html.xml";
    /**
     * Test description and Exception message have multi-lines
     */
    public static final String TESTNG_MULTILINE_EXCEPTION_AND_DESCRIPTION = "testng-xml-multiline-exp-msg-and-descp.xml";
    /**
     * Contains 1 passed and 1 skipped test
     */
    public static final String TESTNG_SKIPPED_TEST = "testng-skipped-tests.xml";
    
    /**
     * Contains 1 passed and 1 failed test
     */
    public static final String TESTNG_FAILED_TEST = "testng-failed-tests.xml";
    /**
     * Contains 1 failed config and 1 skipped test
     */
    public static final String TESTNG_FAILED_TEST_CONFIG = "testng-failed-test-config.xml";
}
