package hudson.plugins.testng.results;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gargoylesoftware.htmlunit.html.DomNodeUtil;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.plugins.testng.CommonUtil;
import hudson.plugins.testng.Constants;
import hudson.plugins.testng.PluginImpl;
import hudson.plugins.testng.Publisher;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

/**
 * Tests for {@link ClassResult}'s view page
 *
 * TODO: speed up the tests by running build once in the setup
 *
 * @author nullin
 */
public class ClassResultTest {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    /**
     * Test using precheckins.legacyops
     *
     * Verifies:
     * 1. multiple testruns show up correctly
     * 2. All links show up correctly
     * 3. Failures and skips show up correctly
     * 4. All cells have a value
     * 5. All test runs are unique
     *
     * @throws Exception
     */
    @Test
    public void testPrecheckinLegacyOpsClassResults() throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("testng.xml");
        publisher.setEscapeTestDescp(false);
        publisher.setEscapeExceptionMsg(false);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_PRECHECKINS);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        TestNGResult testngResult = (TestNGResult) build.getAction(AbstractTestResultAction.class).getResult();
        TestResult classResult = testngResult.findCorrespondingResult(PluginImpl.URL + "/precheckins/LegacyOps");
        Map<String, GroupedTestRun> testRunMap = ((ClassResult)classResult).getTestRunMap();

        //Get page
        String urlPrefix = build.getUrl() + PluginImpl.URL;
        HtmlPage page = r.createWebClient().goTo(urlPrefix + "/precheckins/LegacyOps/");

        List<HtmlElement> elements = DomNodeUtil.selectNodes(page, "//div[starts-with(@id, 'run-')]/span[@id='run-info']");

        assertEquals(testRunMap.values().size(), elements.size());

        Set<String> values = new HashSet<String>(); //will verify that testName|suiteName are not repeated
        for (HtmlElement element : elements) {
            String content = element.getTextContent();
            content = content.replace("(from test '", "");
            String testName = content.substring(0, content.indexOf("' in suite '"));
            String suiteName = content.replace(testName, "").replace("' in suite '", "");
            suiteName = suiteName.substring(0, suiteName.length() - 2); //drop trailing '

            boolean found = false;
            for (GroupedTestRun groupedTestRun : testRunMap.values()) {
                if (groupedTestRun.getSuiteName().equals(suiteName)
                        && groupedTestRun.getTestName().equals(testName)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Failed to find testname " + testName + " and suitename " + suiteName, found);
            values.add(suiteName + "|" +  testName);
        }

        elements = DomNodeUtil.selectNodes(page, "//div[starts-with(@id, 'run-')]/table/*/tr");
        assertEquals(6 * (2 + 7), elements.size()); //total number of rows in all tables

        elements = DomNodeUtil.selectNodes(page, "//div[starts-with(@id, 'run-')]/table/*/tr/td");
        int failCount = 0;
        int skipCount = 0;
        for (HtmlElement element : elements) {
            //none of the cells should be empty
            assertNotSame("", element.getTextContent());
            if ("FAIL".equals(element.getTextContent())) {
                failCount++;
            }
            if ("SKIP".equals(element.getTextContent())) {
                skipCount++;
            }
        }

        assertEquals(1, failCount);
        assertEquals(1, skipCount);

        //unique set of values for test name and suite name combinations
        assertEquals(testRunMap.values().size(), values.size());

        //verify all links
        elements = DomNodeUtil.selectNodes(page, "//div[starts-with(@id, 'run-')]/table/*/tr/td/a");
        List<String> linksInPage = new ArrayList<String>();
        for (HtmlElement element : elements) {
            linksInPage.add(element.getAttribute("href"));
        }
        Collections.sort(linksInPage);

        List<String> linksFromResult = new ArrayList<String>();
        for (MethodResult mr : ((ClassResult) classResult).getChildren()) {
            //would have used mr.getUpUrl() but for some reason
            //as part of test, Jenkins.instance.rootUrl() returns 'null'
            linksFromResult.add(r.getURL() + mr.getRun().getUrl() + mr.getId());
        }
        Collections.sort(linksFromResult);

        assertEquals(linksFromResult, linksInPage);
    }

    /**
     * Test for:
     *
     * 1. Show more section works correctly
     * 2. Case of no config methods is handled correctly
     *
     * @throws Exception
     */
    @Test
    public void testClassResults() throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("testng.xml");
        publisher.setEscapeTestDescp(false);
        publisher.setEscapeExceptionMsg(false);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_TESTNG);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        TestNGResult testngResult = (TestNGResult) build.getAction(AbstractTestResultAction.class).getResult();
        TestResult classResult = testngResult.findCorrespondingResult(PluginImpl.URL + "/test/CommandLineTest");
        Map<String, GroupedTestRun> testRunMap = ((ClassResult)classResult).getTestRunMap();

        //Get page
        String urlPrefix = build.getUrl() + PluginImpl.URL;
        HtmlPage page = r.createWebClient().goTo(urlPrefix + "/test/CommandLineTest");

        List<HtmlElement> elements = DomNodeUtil.selectNodes(page, "//div[starts-with(@id, 'run-')]/table[@id='config']");
        // there are no configuration methods
        assertEquals(0, elements.size());
        r.assertStringContains(page.getElementById("run-0").getTextContent(), "No Configuration method was found in this class");

        //use first test with show more section
        elements = DomNodeUtil.selectNodes(page, "//div[starts-with(@id, 'run-')]/table[@id='test']/tbody/tr/td/div[@id='junitParsing_1']");
        assertEquals(1, elements.size());
        HtmlElement showMore = elements.get(0);
        elements = DomNodeUtil.selectNodes(page, "//div[starts-with(@id, 'run-')]/table[@id='test']/tbody/tr/td/div[@id='junitParsing_2']");
        assertEquals(1, elements.size());
        HtmlElement moreSection = elements.get(0);

        r.assertStringContains(moreSection.getTextContent(), "current"); //group name

        //click the link
        showMore.getElementsByTagName("a").get(0).click();

        r.assertStringContains(showMore.getAttribute("style"), "none");
        assertEquals("", moreSection.getAttribute("style"));
    }
}
