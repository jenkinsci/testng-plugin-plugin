package hudson.plugins.testng.results;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.testng.CommonUtil;
import hudson.plugins.testng.Constants;
import hudson.plugins.testng.PluginImpl;
import hudson.plugins.testng.Publisher;
import hudson.plugins.testng.PublisherCtor;
import hudson.tasks.test.TestResult;
import junit.framework.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TestBuilder;

/**
 * Tests for {@link MethodResult}'s view page
 *
 * @author nullin
 */
public class MethodResultTest extends HudsonTestCase {

    @Test
    public void testEscapeExceptionMessageTrue() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml")
                        .setEscapeTestDescp(false).setEscapeExceptionMsg(true);
        Publisher publisher = publisherCtor.getNewPublisher();
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
        assertStringContains(contents, "</a>"); //escaped HTML so it shows up as string
    }

    @Test
    public void testEscapeExceptionMessageFalse() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml")
                        .setEscapeTestDescp(false).setEscapeExceptionMsg(false);
        Publisher publisher = publisherCtor.getNewPublisher();
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
        assertFalse(contents.contains("</a>")); //non-escaped HTML so it shouldn't show up as text
    }

    @Test
    public void testEscapeDescriptionFalse() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml")
                        .setEscapeTestDescp(false).setEscapeExceptionMsg(false);
        Publisher publisher = publisherCtor.getNewPublisher();
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
        HtmlElement description = page.getElementById("description");
        String contents = description.getTextContent();
        assertFalse(contents.contains("</a>")); //non-escaped HTML so it doesn't show up as text
        assertFalse(contents.contains("<a href=\"")); //non-escaped HTML
    }

    @Test
    public void testEscapeDescriptionTrue() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml")
                        .setEscapeTestDescp(true).setEscapeExceptionMsg(false);
        Publisher publisher = publisherCtor.getNewPublisher();
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
        HtmlElement description = page.getElementById("description");
        String contents = description.getTextContent();
        assertStringContains(contents, "</a>"); //escaped HTML so it shows up as text
    }

    /**
     * Tests to make sure that newline characters are escaped correctly in description and
     * exception message even when escape settings are set to false.
     *
     * Note that newline in description has to be denoted by &#10; as it's an attribute in
     * testng result XML, where as exception message doesn't as it's wrapped in a CDATA
     *
     * @throws Exception
     */
    @Test
    public void testMultilineDescriptionAndExceptionMessage() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml")
                        .setEscapeTestDescp(false).setEscapeExceptionMsg(false);
        Publisher publisher = publisherCtor.getNewPublisher();
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_MULTILINE_EXCEPTION_AND_DESCRIPTION);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();

        //Compare output
        String methodUrl = build.getUrl() + PluginImpl.URL + "/com.fakepkg.test/com.fakepkg.test.FoobarTests/test";
        HtmlPage page = createWebClient().goTo(methodUrl);
        HtmlElement description = page.getElementById("description");
        assertEquals(2, description.getElementsByTagName("br").size());

        HtmlElement exp = page.getElementById("exp-msg");
        assertEquals(2, exp.getElementsByTagName("br").size());

    }

    @Test
    public void testReporterLogOutput() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml");
        Publisher publisher = publisherCtor.getNewPublisher();
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
        String methodUrl = build.getUrl() + PluginImpl.URL
                + "/org.example.test/org.example.test.ExampleIntegrationTest/FirstTest";
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
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml");
        Publisher publisher = publisherCtor.getNewPublisher();
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
        TestResult methodResult = testngResult.findCorrespondingResult(
                        PluginImpl.URL + "/test/test.Test1/includedGroups_1");

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
        page = createWebClient().goTo(urlPrefix
                + "/test.dataprovider/test.dataprovider.Sample1Test/verifyNames_1/");
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
    public void testMethodResults_dataProviderTests() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml");
        Publisher publisher = publisherCtor.getNewPublisher();
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
        assertStringContains(element.getTextContent(),
                             "org.jenkins.TestDataProvider.test(TestDataProvider.java:15)");

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
    public void testMethodResults_testInstanceNames() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml");
        Publisher publisher = publisherCtor.getNewPublisher();
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
