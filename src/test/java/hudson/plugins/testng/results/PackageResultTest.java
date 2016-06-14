package hudson.plugins.testng.results;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

/**
 * Tests for {@link PackageResult}'s view page
 *
 * TODO: add tests for checking fail and skip diffs between two test runs
 *
 * @author nullin
 */
public class PackageResultTest {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    /**
     * Test using precheckins
     *
     * 1. verify links to classes are correct
     * 2. verify all cells have a value
     * 3. verify all classes are present
     * 4. verify only first 25 test methods are shown
     * 5. verify that clicking to see all, gets all methods
     * 6. verify that the links for all methods are correct
     *
     * @throws Exception
     */
    @Test
    public void testPrecheckinPackageResults() throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("testng.xml");
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
        PackageResult pkgResult = (PackageResult) testngResult.findCorrespondingResult(PluginImpl.URL + "/precheckins");

        //Get page
        String urlPrefix = build.getUrl() + PluginImpl.URL;
        HtmlPage page = r.createWebClient().goTo(urlPrefix + "/precheckins");

        List<HtmlElement> elements = DomNodeUtil.selectNodes(page, "//table[@id='allClasses']/tbody/tr/td/a");

        //ensure correct number of classes is displayed
        assertEquals(pkgResult.getChildren().size(), elements.size());

        //verify links to classes
        List<String> linksInPage = new ArrayList<String>();
        for (HtmlElement element : elements) {
            linksInPage.add(element.getAttribute("href"));
        }
        Collections.sort(linksInPage);

        List<String> linksFromResult = new ArrayList<String>();
        for (ClassResult cr : pkgResult.getChildren()) {
            //would have used cr.getUpUrl() but for some reason
            //as part of test, Jenkins.instance.rootUrl() returns 'null'
            linksFromResult.add(r.getURL() + cr.getRun().getUrl() + cr.getId());
        }
        Collections.sort(linksFromResult);

        assertEquals(linksFromResult, linksInPage);

        //verify that all cells have a value (are not empty)
        elements = DomNodeUtil.selectNodes(page, "//table[@id='allClasses']/tbody/tr/td");
        for (HtmlElement element : elements) {
            assertNotSame("", element.getTextContent());
        }

        //verify only first 25 methods are shown
        HtmlElement divShowAllLink = page.getElementById("showAllLink", true);
        assertNotNull(divShowAllLink);
        assertEquals("Showing only first " + PackageResult.MAX_EXEC_MTHD_LIST_SIZE + " test methods. Click to see all",
                     divShowAllLink.getTextContent());

        elements = DomNodeUtil.selectNodes(page, "//tbody[@id='sortedMethods']/tr");
        assertEquals(PackageResult.MAX_EXEC_MTHD_LIST_SIZE, elements.size());

        //verify clicking on link gets all methods back
        divShowAllLink.getElementsByTagName("a").get(0).click(); //click to get all test methods

        elements = DomNodeUtil.selectNodes(page, "//tbody[@id='sortedMethods']/tr/td/a");
        assertEquals(pkgResult.getSortedTestMethodsByStartTime().size(), elements.size());

        //verify links for test methods
        linksInPage.clear();
        for (HtmlElement element : elements) {
            linksInPage.add(element.getAttribute("href"));
        }
        Collections.sort(linksInPage);

        linksFromResult.clear();
        for (MethodResult mr : pkgResult.getSortedTestMethodsByStartTime()) {
            //would have used mr.getUpUrl() but for some reason
            //as part of test, Jenkins.instance.rootUrl() returns 'null'
            linksFromResult.add(r.getURL() + mr.getRun().getUrl() + mr.getId());
        }
        Collections.sort(linksFromResult);

        assertEquals(linksFromResult, linksInPage);

        //assert that link to get all methods is no longer visible
        r.assertStringContains(divShowAllLink.getAttribute("style"), "none");

        //verify bar
        HtmlElement element = page.getElementById("fail-skip", true);
        r.assertStringContains(element.getTextContent(), "1 failure");
        assertFalse(element.getTextContent().contains("failures"));
        r.assertStringContains(element.getTextContent(), "1 skipped");
        element = page.getElementById("pass", true);
        r.assertStringContains(element.getTextContent(), "38 tests");
    }

    /**
     * Test using my.package package
     *
     * 1. verify that there are 0 failures and 1 test
     * 2. verify there is no section to get all methods
     *
     * @throws Exception
     */
    @Test
    public void testMyPackagePackageResults() throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("testng.xml");
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_EMPTY_EXCEPTION);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();

        //Get page
        String urlPrefix = build.getUrl() + PluginImpl.URL;
        HtmlPage page = r.createWebClient().goTo(urlPrefix + "/my.package");

        //verify only first 25 methods are shown
        assertNull(page.getElementById("showAllLink"));

        //verify bar
        HtmlElement element = page.getElementById("fail-skip", true);
        r.assertStringContains(element.getTextContent(), "0 failures");
        assertFalse(element.getTextContent().contains("skipped"));
        element = page.getElementById("pass", true);
        r.assertStringContains(element.getTextContent(), "1 test");
        assertFalse(element.getTextContent().contains("tests"));
    }
}
