package hudson.plugins.testng.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.plugins.testng.CommonUtil;
import hudson.plugins.testng.Constants;
import hudson.plugins.testng.results.MethodResult;
import hudson.plugins.testng.results.PackageResult;
import hudson.plugins.testng.results.TestNGResult;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TestParser {

    @Test
    void testTestngXmlWithExistingResultXml() {
        URL resource = CommonUtil.getResource(Constants.TESTNG_XML_PRECHECKINS);
        assertNotNull(resource);
        TestNGResult results = CommonUtil.getResults(resource.getFile());
        assertFalse(results.getTestList().isEmpty(), "Collection shouldn't have been empty");
    }

    @Test
    void testTestngXmlWithSameTestNameDiffSuites() {
        URL resource = CommonUtil.getResource(Constants.TESTNG_XML_SAME_TEST_NAME);
        assertNotNull(resource);
        TestNGResult results = CommonUtil.getResults(resource.getFile());
        assertFalse(results.getTestList().isEmpty(), "Collection shouldn't have been empty");
        assertEquals(2, results.getTestList().size());
        results.tally();
        assertEquals(1, results.getPackageNames().size());
        assertEquals(
                3,
                results.getPackageMap().values().iterator().next().getChildren().size());
        assertEquals(4, results.getPassCount());
        assertEquals(4, results.getPassedTests().size());
    }

    @Test
    void testTestngXmlWithExistingResultXmlGetsTheRightDurations() {
        URL resource = CommonUtil.getResource(Constants.TESTNG_XML_DATAPROVIDER);
        assertNotNull(resource);
        TestNGResult results = CommonUtil.getResults(resource.getFile());
        assertFalse(results.getTestList().isEmpty(), "Collection shouldn't have been empty");

        // This test assumes that there is only 1 package in
        // sample-testng-dp-result that contains tests that add to 12 ms
        results.tally();
        Map<String, PackageResult> packageResults = results.getPackageMap();
        assertEquals(1, packageResults.size());
        PackageResult result = packageResults.values().iterator().next();
        assertEquals("org.jenkins", result.getName());
        // durations are all in seconds
        assertEquals(0.009f, result.getDuration());
    }

    @Test
    void testTestngXmlWithNonExistingResultXml() {
        TestNGResult results = CommonUtil.getResults("/invalid/path/to/file/new-test-result.xml");
        assertTrue(
                results.getTestList().isEmpty(),
                "Collection should have been empty. Number of results : "
                        + results.getTestList().size());
    }

    @Test
    void parseTestNG() {
        TestNGResult results = CommonUtil.getResults(
                CommonUtil.getResource(Constants.TESTNG_XML_TESTNG).getFile());
        results.tally();
    }

    @Test
    void testParseEmptyException() {
        TestNGResult results = CommonUtil.getResults(
                CommonUtil.getResource(Constants.TESTNG_XML_EMPTY_EXCEPTION).getFile());
        results.tally();
        assertEquals(1, results.getPassCount());
        MethodResult mr = results.getPassedTests().get(0);
        assertEquals(
                "$java.lang.IllegalStateException$$EnhancerByMockitoWithCGLIB$$c0ded2d3",
                mr.getException().getExceptionName());
    }

    @Test
    void testDateParser() throws ParseException {
        // example of date format used in testng report
        String dateString = "2010-07-20T11:49:17Z";
        SimpleDateFormat sdf = new SimpleDateFormat(ResultsParser.DATE_FORMAT);
        sdf.parse(dateString);
    }

    @Test
    void testReporterOutputForMethods() {
        String filename = Constants.TESTNG_XML_REPORTER_LOG_OUTPUT;
        URL resource = TestParser.class.getClassLoader().getResource(filename);
        assertNotNull(resource);
        TestNGResult results = CommonUtil.getResults(resource.getFile());
        assertFalse(results.getTestList().isEmpty(), "Collection shouldn't have been empty");
        assertEquals(1, results.getTestList().size());
        results.tally();
        assertEquals(1, results.getPackageNames().size());
        assertEquals(
                1,
                results.getPackageMap().values().iterator().next().getChildren().size());
        assertEquals(1, results.getPassCount());
        assertEquals(1, results.getPassedTests().size());
        assertEquals(1, results.getFailedTests().size());
        assertNotNull(results.getFailedTests().get(0).getException());
        assertNotNull(results.getFailedTests().get(0).getReporterOutput());
        assertEquals(
                "Some Reporter.log() statement<br/>Another Reporter.log() statement<br/>",
                results.getFailedTests().get(0).getReporterOutput());
        assertNull(results.getPassedTests().get(0).getReporterOutput());
    }
}
