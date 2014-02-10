package hudson.plugins.testng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.testng.results.MethodResult;
import hudson.plugins.testng.results.PackageResult;
import hudson.plugins.testng.results.TestNGResult;
import junit.framework.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TestBuilder;

/**
 * Tests for {@link TestNGTestResultBuildAction}'s view page
 *
 * @author nullin
 */
public class TestNGTestResultBuildActionTest extends HudsonTestCase {

    /**
     * Test using precheckins xml
     *
     * @throws Exception
     */
    @Test
    public void testBuildAction_1() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml");
        Publisher publisher = publisherCtor.getNewPublisher();
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_PRECHECKINS);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        TestNGResult testngResult = (TestNGResult) build.getTestResultAction().getResult();

        //Get page
        HtmlPage page = createWebClient().goTo(build.getUrl() + PluginImpl.URL);

        //make sure no cell is empty
        List<HtmlElement> elements = page.selectNodes("//table[substring(@id, string-length(@id)- string-length('-tbl') +1)]/*/tr/td");
        for (HtmlElement element : elements) {
            assertTrue(!element.getTextContent().isEmpty());
        }

        //ensure only one failed test
        //there are three links in the cell, we pick the one without any id attr
        elements = page.selectNodes("//table[@id='fail-tbl']/tbody/tr/td/a[not(@id)]");
        assertEquals(1, elements.size());
        MethodResult mr = testngResult.getFailedTests().get(0);
        assertEquals(super.getURL() + mr.getOwner().getUrl() + mr.getId(),
                elements.get(0).getAttribute("href"));

        //ensure only one failed config method
        elements = page.selectNodes("//table[@id='fail-config-tbl']/tbody/tr/td/a");
        //asserting to 3, because a link for >>>, one for <<< and another for the method itself
        assertEquals(3, elements.size());
        mr = testngResult.getFailedConfigs().get(0);
        assertEquals(super.getURL() + mr.getOwner().getUrl() + mr.getId(),
                elements.get(2).getAttribute("href"));

        //ensure only one skipped test method
        elements = page.selectNodes("//table[@id='skip-tbl']/tbody/tr/td/a");
        assertEquals(1, elements.size());
        mr = testngResult.getSkippedTests().get(0);
        assertEquals(super.getURL() + mr.getOwner().getUrl() + mr.getId(),
                elements.get(0).getAttribute("href"));

        //ensure no skipped config
        elements = page.selectNodes("//table[@id='skip-config-tbl']");
        assertEquals(0, elements.size());

        //check list of packages and links
        elements = page.selectNodes("//table[@id='all-tbl']/tbody/tr/td/a");
        Map<String, PackageResult> pkgMap = testngResult.getPackageMap();
        assertEquals(pkgMap.keySet().size(), elements.size());

        //verify links to packages
        List<String> linksInPage = new ArrayList<String>();
        for (HtmlElement element : elements) {
            linksInPage.add(element.getAttribute("href"));
        }
        Collections.sort(linksInPage);

        List<String> linksFromResult = new ArrayList<String>();
        for (PackageResult pr : pkgMap.values()) {
            linksFromResult.add(pr.getName());
        }
        Collections.sort(linksFromResult);
        assertEquals(linksFromResult, linksInPage);

        //verify bar
        HtmlElement element = page.getElementById("fail-skip");
        assertStringContains(element.getTextContent(), "1 failure");
        assertFalse(element.getTextContent().contains("failures"));
        assertStringContains(element.getTextContent(), "1 skipped");
        element = page.getElementById("pass");
        assertStringContains(element.getTextContent(), "38 tests");
    }

    /**
     * Test using testng result xml
     *
     * @throws Exception
     */
    @Test
    public void testBuildAction_2() throws Exception {
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

        //Get page
        HtmlPage page = createWebClient().goTo(build.getUrl() + PluginImpl.URL);

        //make sure no cell is empty
        List<HtmlElement> elements = page.selectNodes("//table[substring(@id, string-length(@id)- string-length('-tbl') +1)]/*/tr/td");
        for (HtmlElement element : elements) {
            assertTrue(!element.getTextContent().isEmpty());
        }

        //ensure only one failed test
        elements = page.selectNodes("//table[@id='fail-tbl']");
        assertEquals(0, elements.size());

        //ensure only one failed config method
        elements = page.selectNodes("//table[@id='fail-config-tbl']");
        assertEquals(0, elements.size());

        //ensure only one skipped test method
        elements = page.selectNodes("//table[@id='skip-tbl']");
        assertEquals(0, elements.size());

        //ensure no skipped config
        elements = page.selectNodes("//table[@id='skip-config-tbl']");
        assertEquals(0, elements.size());

        //check list of packages and links
        elements = page.selectNodes("//table[@id='all-tbl']/tbody/tr/td/a");
        Map<String, PackageResult> pkgMap = testngResult.getPackageMap();
        assertEquals(pkgMap.keySet().size(), elements.size());

        //verify links to packages
        List<String> linksInPage = new ArrayList<String>();
        for (HtmlElement element : elements) {
            linksInPage.add(element.getAttribute("href"));
        }
        Collections.sort(linksInPage);

        List<String> linksFromResult = new ArrayList<String>();
        for (PackageResult pr : pkgMap.values()) {
            linksFromResult.add(pr.getName());
        }

        Collections.sort(linksFromResult);
        assertEquals(linksFromResult, linksInPage);
        assertTrue(linksInPage.contains("No Package"));

        //verify bar
        HtmlElement element = page.getElementById("fail-skip");
        assertStringContains(element.getTextContent(), "0 failures");
        assertFalse(element.getTextContent().contains("skipped"));
        element = page.getElementById("pass");
        assertStringContains(element.getTextContent(), "526 tests");
    }

    //unstableOnSkippedTests set to false
    @Test
    public void test_skipped_tests_default_setting() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml");
        Publisher publisher = publisherCtor.getNewPublisher();
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_SKIPPED_TEST);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        Assert.assertSame(Result.SUCCESS, build.getResult());
    }

    //unstableOnSkippedTests set to true
    @Test
    public void test_skipped_tests_enabled() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml")
                .setUnstableOnSkippedTests(true);
        Publisher publisher = publisherCtor.getNewPublisher();
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_SKIPPED_TEST);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        Assert.assertSame(Result.UNSTABLE, build.getResult());
    }

    //unstableOnSkippedTests set to true. Has no effect as build is already marked failed
    @Test
    public void test_skipped_tests_enabled_failedbuild() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml")
                .setUnstableOnSkippedTests(true);
        Publisher publisher = publisherCtor.getNewPublisher();
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_SKIPPED_TEST);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                build.setResult(Result.FAILURE);
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        Assert.assertSame(Result.FAILURE, build.getResult());
    }


}
