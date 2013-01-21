package hudson.plugins.testng.results;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.Launcher;
import hudson.model.*;
import hudson.plugins.testng.CommonUtil;
import hudson.plugins.testng.Constants;
import hudson.plugins.testng.PluginImpl;
import hudson.plugins.testng.Publisher;
import hudson.tasks.test.TestResult;
import junit.framework.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;

/**
 * Tests for MethodResult's view page
 *
 * TODO: speed up the tests by running build once in the setup
 *
 * @author nullin
 */
public class MethodResultTest extends HudsonTestCase {

    @Test
    public void testEscapeExceptionMessageTrue() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        Publisher publisher = new Publisher("testng.xml", false, true /*escapeExpMsg*/);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_EXP_MSG_XML);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        Assert.assertEquals(Result.UNSTABLE, build.getResult());

        //Compare output
        String methodUrl = build.getUrl() + PluginImpl.URL + "/gov.nasa.jpl/gov.nasa.jpl.FoobarTests/b";
        HtmlPage page = createWebClient().goTo(methodUrl);
        HtmlElement expMsg = page.getElementById("exp-msg");
        String contents = expMsg.getTextContent();
        assertStringContains(contents, "&lt;/a&gt;"); //escaped HTML
    }

    @Test
    public void testEscapeExceptionMessageFalse() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        Publisher publisher = new Publisher("testng.xml", false, false /*escapeExpMsg*/);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_EXP_MSG_XML);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();

        //Compare output
        String methodUrl = build.getUrl() + PluginImpl.URL + "/gov.nasa.jpl/gov.nasa.jpl.FoobarTests/b";
        HtmlPage page = createWebClient().goTo(methodUrl);
        HtmlElement expMsg = page.getElementById("exp-msg");
        String contents = expMsg.getTextContent();
        assertStringContains(contents, "</a>"); //non-escaped HTML
    }

    @Test
    public void testEscapeDescriptionFalse() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        Publisher publisher = new Publisher("testng.xml", false /*escapeDescription*/, false);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_DESCRIPTION_HTML);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();

        //Compare output
        String methodUrl = build.getUrl() + PluginImpl.URL + "/com.test/com.test.UploadTest/uploadFile";
        HtmlPage page = createWebClient().goTo(methodUrl);
        HtmlElement expMsg = page.getElementById("description");
        String contents = expMsg.getTextContent();
        assertStringContains(contents, "</a>"); //non-escaped HTML
        assertStringContains(contents, "<a href=\""); //non-escaped HTML
    }

    @Test
    public void testEscapeDescriptionTrue() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        Publisher publisher = new Publisher("testng.xml", true /*escapeDescription*/, false);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_DESCRIPTION_HTML);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();

        //Compare output
        String methodUrl = build.getUrl() + PluginImpl.URL + "/com.test/com.test.UploadTest/uploadFile";
        HtmlPage page = createWebClient().goTo(methodUrl);
        HtmlElement expMsg = page.getElementById("description");
        String contents = expMsg.getTextContent();
        assertStringContains(contents, "&lt;/a&gt;"); //non-escaped HTML
    }

    @Test
    public void testReporterLogOutput() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        Publisher publisher = new Publisher("testng.xml", false, false);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_REPORTER_LOG_OUTPUT);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();

        //Compare output
        String methodUrl = build.getUrl() + PluginImpl.URL + "/org.example.test/org.example.test.ExampleIntegrationTest/FirstTest";
        HtmlPage page = createWebClient().goTo(methodUrl);
        HtmlElement reporterOutput = page.getElementById("reporter-output");
        String contents = reporterOutput.getTextContent();
        assertStringContains(contents, "Some Reporter.log() statement");
        assertStringContains(contents, "Another Reporter.log() statement");
    }

    /**
     * Test some conditions in method result view using testng result xml
     *
     * @throws Exception
     */
    @Test
    public void testMethodResults1() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        Publisher publisher = new Publisher("testng.xml", false, false);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_TESTNG);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        TestNGResult testngResult = (TestNGResult) build.getTestResultAction().getResult();
        TestResult methodResult = testngResult.findCorrespondingResult(PluginImpl.URL + "/test/test.Test1/includedGroups_1");

        //Compare output for a method
        String urlPrefix = build.getUrl() + PluginImpl.URL;
        HtmlPage page = createWebClient().goTo(urlPrefix + "/test/test.Test1/includedGroups_1/");
        HtmlElement element = page.getElementById("parent");
        String contents = element.getTextContent();
        //information about class and time taken
        assertStringContains(contents, "test.Test1");
        assertTrue(element.getAttribute("href").endsWith(urlPrefix + "/test/test.Test1"));

        //duration string
        assertStringContains(page.getElementById("report").getTextContent(), methodResult.getDurationString());

        //header containing method name
        element = page.getElementsByTagName("h1").get(0);
        assertEquals("includedGroups", element.getTextContent());

        //method status information
        element = page.getElementById("status");
        assertEquals("result-passed", element.getAttribute("class"));
        assertEquals("PASS", element.getTextContent());

        //this method has single group
        element = page.getElementById("groups");
        assertEquals(element.getTextContent(), "Group(s): current");

        //should have an img
        element = page.getElementById("report").getElementsByTagName("img").get(0);
        assertNotNull(element);
        assertEquals("trend", element.getAttribute("id"));
        assertEquals("graph", element.getAttribute("src"));
        assertEquals("graphMap", element.getAttribute("lazymap"));
        assertEquals("[Method Execution Trend Chart]", element.getAttribute("alt"));

        //following shouldn't be present on page
        assertNull(page.getElementById("inst-name"));
        assertNull(page.getElementById("params"));
        assertNull(page.getElementById("reporter-output"));
        assertNull(page.getElementById("exp-msg"));

        //method run using two parameters
        page = createWebClient().goTo(urlPrefix + "/test.dataprovider/test.dataprovider.Sample1Test/verifyNames_1/");
        element = page.getElementById("params");
        contents = element.getTextContent();
        //information about class and time taken
        assertStringContains(contents, "Parameter #1");
        assertStringContains(contents, "Parameter #2");
        assertStringContains(contents, "Anne Marie");
        assertStringContains(contents, "37");
    }

    /**
     * Testing method result view with data provider tests that pass/fail
     * @throws Exception
     */
    @Test
    public void testMethodResults2() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        Publisher publisher = new Publisher("testng.xml", false, false);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_DATAPROVIDER);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();

        //Compare output for a dp method that failed
        String urlPrefix = build.getUrl() + PluginImpl.URL;
        WebClient wc = createWebClient();
        HtmlPage page = wc.goTo(urlPrefix + "/org.jenkins/org.jenkins.TestDataProvider/test/");

        //method status information
        HtmlElement element = page.getElementById("status");
        assertEquals("result-failed", element.getAttribute("class"));
        assertEquals("FAIL", element.getTextContent());

        //this method has single parameter
        element = page.getElementById("params");
        String contents = element.getTextContent();
        assertStringContains(contents, "Parameter #1");
        assertStringContains(contents, "Value");
        assertFalse(contents.contains("Parameter #2"));

        //this method has no groups or reporter output
        assertNull(page.getElementById("groups"));
        assertNull(page.getElementById("reporter-output"));

        //this method has exception with no message
        element = page.getElementsByTagName("h3").get(0);
        assertEquals("Exception java.lang.AssertionError", element.getTextContent());
        element = page.getElementById("exp-msg");
        assertStringContains(element.getTextContent(), "(none)");
        element = page.getElementById("exp-st");
        assertStringContains(element.getTextContent(), "org.jenkins.TestDataProvider.test(TestDataProvider.java:15)");

        //compare output for a dp method that passed
        page = wc.goTo(urlPrefix + "/org.jenkins/org.jenkins.TestDataProvider/test_2/");

        //method status information
        element = page.getElementById("status");
        assertEquals("result-passed", element.getAttribute("class"));
        assertEquals("PASS", element.getTextContent());

        //this method has single parameter
        element = page.getElementById("params");
        contents = element.getTextContent();
        assertStringContains(contents, "Parameter #1");
        assertStringContains(contents, "2");
        assertFalse(contents.contains("Parameter #2"));

        assertNull(page.getElementById("inst-name"));
        assertNull(page.getElementById("groups"));
        assertNull(page.getElementById("reporter-output"));
        assertNull(page.getElementById("exp-msg"));
        assertNull(page.getElementById("exp-st"));
    }


    /**
     * Testing method result view with tests that have instance names
     * @throws Exception
     */
    @Test
    public void testMethodResults3() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        Publisher publisher = new Publisher("testng.xml", false, false);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_INSTANCE_NAME);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();

        //Compare output for a dp method that failed
        String urlPrefix = build.getUrl() + PluginImpl.URL;
        WebClient wc = createWebClient();
        HtmlPage page = wc.goTo(urlPrefix + "/testng.instancename/testng.instancename.MyITestFactoryTest/factoryTest1/");

        //method instance name information
        HtmlElement element = page.getElementById("inst-name");
        assertStringContains(element.getTextContent(), "FACTORY_VMFS");
    }
}
