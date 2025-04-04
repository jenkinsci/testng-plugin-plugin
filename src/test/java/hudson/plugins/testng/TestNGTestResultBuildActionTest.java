package hudson.plugins.testng;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import hudson.Functions;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.testng.results.ClassResult;
import hudson.plugins.testng.results.MethodResult;
import hudson.plugins.testng.results.PackageResult;
import hudson.plugins.testng.results.TestNGResult;
import hudson.tasks.test.AbstractTestResultAction;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.htmlunit.html.DomNodeUtil;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Tests for {@link TestNGTestResultBuildAction}'s view page
 *
 * @author nullin
 */
@WithJenkins
class TestNGTestResultBuildActionTest {

    /**
     * Test using precheckins xml
     *
     * @throws Exception
     */
    @Test
    void testBuildAction_1(JenkinsRule r) throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("testng.xml");
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); // to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_PRECHECKINS);
                build.getWorkspace().child("testng.xml").write(contents, "UTF-8");
                return true;
            }
        });

        // run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        TestNGResult testngResult =
                (TestNGResult) build.getAction(AbstractTestResultAction.class).getResult();

        // Get page
        HtmlPage page = r.createWebClient().goTo(build.getUrl() + PluginImpl.URL);

        // make sure no cell is empty
        List<HtmlElement> elements = DomNodeUtil.selectNodes(
                page, "//table[substring(@id, string-length(@id)- string-length('-tbl') +1)]/*/tr/td");
        for (HtmlElement element : elements) {
            assertFalse(element.getTextContent().isEmpty());
        }

        // ensure only one failed test
        // there are three links in the cell, we pick the one without any id attr
        elements = DomNodeUtil.selectNodes(page, "//table[@id='fail-tbl']/tbody/tr/td/a[not(@id)]");
        assertEquals(1, elements.size());
        MethodResult mr = testngResult.getFailedTests().get(0);
        assertEquals(
                r.getURL() + mr.getRun().getUrl() + mr.getId(), elements.get(0).getAttribute("href"));
        assertEquals(
                ((ClassResult) mr.getParent()).getCanonicalName() + "." + mr.getName(),
                elements.get(0).getTextContent());

        // ensure only one failed config method
        elements = DomNodeUtil.selectNodes(page, "//table[@id='fail-config-tbl']/tbody/tr/td/a");
        // asserting to 3, because a link for >>>, one for <<< and another for the method itself
        assertEquals(3, elements.size());
        mr = testngResult.getFailedConfigs().get(0);
        assertEquals(
                r.getURL() + mr.getRun().getUrl() + mr.getId(), elements.get(2).getAttribute("href"));
        assertEquals(
                ((ClassResult) mr.getParent()).getCanonicalName() + "." + mr.getName(),
                elements.get(2).getTextContent());

        // ensure only one skipped test method
        elements = DomNodeUtil.selectNodes(page, "//table[@id='skip-tbl']/tbody/tr/td/a");
        assertEquals(1, elements.size());
        mr = testngResult.getSkippedTests().get(0);
        assertEquals(
                r.getURL() + mr.getRun().getUrl() + mr.getId(), elements.get(0).getAttribute("href"));
        assertEquals(
                ((ClassResult) mr.getParent()).getCanonicalName() + "." + mr.getName(),
                elements.get(0).getTextContent());

        // ensure no skipped config
        elements = DomNodeUtil.selectNodes(page, "//table[@id='skip-config-tbl']");
        assertEquals(0, elements.size());

        // check list of packages and links
        elements = DomNodeUtil.selectNodes(page, "//table[@id='all-tbl']/tbody/tr/td/a");
        Map<String, PackageResult> pkgMap = testngResult.getPackageMap();
        assertEquals(pkgMap.size(), elements.size());

        // verify links to packages
        List<String> linksInPage = new ArrayList<>();
        for (HtmlElement element : elements) {
            linksInPage.add(element.getAttribute("href"));
        }
        Collections.sort(linksInPage);

        List<String> linksFromResult = new ArrayList<>();
        for (PackageResult pr : pkgMap.values()) {
            linksFromResult.add(pr.getName());
        }
        Collections.sort(linksFromResult);
        assertEquals(linksFromResult, linksInPage);

        // verify bar
        HtmlElement element = page.getHtmlElementById("fail-skip");
        r.assertStringContains(element.getTextContent(), "1 failure");
        assertFalse(element.getTextContent().contains("failures"));
        r.assertStringContains(element.getTextContent(), "1 skipped");
        element = page.getHtmlElementById("pass");
        r.assertStringContains(element.getTextContent(), "38 tests");
    }

    /**
     * Test using testng result xml
     *
     * @throws Exception
     */
    @Test
    void testBuildAction_2(JenkinsRule r) throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("testng.xml");
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); // to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_TESTNG);
                build.getWorkspace().child("testng.xml").write(contents, "UTF-8");
                return true;
            }
        });

        // run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        TestNGResult testngResult =
                (TestNGResult) build.getAction(AbstractTestResultAction.class).getResult();

        // Get page
        HtmlPage page = r.createWebClient().goTo(build.getUrl() + PluginImpl.URL);

        // make sure no cell is empty
        List<HtmlElement> elements = DomNodeUtil.selectNodes(
                page, "//table[substring(@id, string-length(@id)- string-length('-tbl') +1)]/*/tr/td");
        for (HtmlElement element : elements) {
            assertFalse(element.getTextContent().isEmpty());
        }

        // ensure only one failed test
        elements = DomNodeUtil.selectNodes(page, "//table[@id='fail-tbl']");
        assertEquals(0, elements.size());

        // ensure only one failed config method
        elements = DomNodeUtil.selectNodes(page, "//table[@id='fail-config-tbl']");
        assertEquals(0, elements.size());

        // ensure only one skipped test method
        elements = DomNodeUtil.selectNodes(page, "//table[@id='skip-tbl']");
        assertEquals(0, elements.size());

        // ensure no skipped config
        elements = DomNodeUtil.selectNodes(page, "//table[@id='skip-config-tbl']");
        assertEquals(0, elements.size());

        // check list of packages and links
        elements = DomNodeUtil.selectNodes(page, "//table[@id='all-tbl']/tbody/tr/td/a");
        Map<String, PackageResult> pkgMap = testngResult.getPackageMap();
        assertEquals(pkgMap.size(), elements.size());

        // verify links to packages
        List<String> linksInPage = new ArrayList<>();
        for (HtmlElement element : elements) {
            linksInPage.add(element.getAttribute("href"));
        }
        Collections.sort(linksInPage);

        List<String> linksFromResult = new ArrayList<>();
        for (PackageResult pr : pkgMap.values()) {
            linksFromResult.add(pr.getName());
        }

        Collections.sort(linksFromResult);
        assertEquals(linksFromResult, linksInPage);
        assertTrue(linksInPage.contains("No Package"));

        // verify bar
        HtmlElement element = page.getHtmlElementById("fail-skip");
        r.assertStringContains(element.getTextContent(), "0 failures");
        assertFalse(element.getTextContent().contains("skipped"));
        element = page.getHtmlElementById("pass");
        r.assertStringContains(element.getTextContent(), "526 tests");
    }

    @Test
    void test_failed_config_default_setting(JenkinsRule r) throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("testng.xml");
        publisher.setFailedSkips(100); // these prevent the skip that results from config failure from determining
        // result
        publisher.setUnstableSkips(100);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); // to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_FAILED_TEST_CONFIG);
                build.getWorkspace().child("testng.xml").write(contents, "UTF-8");
                return true;
            }
        });

        // run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        assertSame(Result.SUCCESS, build.getResult());
    }

    @Test
    void test_failed_config_enabled_failedbuild(JenkinsRule r) throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("testng.xml");
        publisher.setFailureOnFailedTestConfig(true);
        publisher.setFailedSkips(10); // these prevent the skip that results from config failure from determining
        // result
        publisher.setUnstableSkips(10);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); // to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_FAILED_TEST_CONFIG);
                build.getWorkspace().child("testng.xml").write(contents, "UTF-8");
                return true;
            }
        });

        // run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        assertSame(Result.FAILURE, build.getResult());
    }

    @Test
    void test_threshold_for_skips_default(JenkinsRule r) throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("testng.xml");
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); // to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_SKIPPED_TEST);
                build.getWorkspace().child("testng.xml").write(contents, "UTF-8");
                return true;
            }
        });

        // run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        assertSame(Result.SUCCESS, build.getResult());
    }

    @Test
    void test_threshold_for_skips_failure(JenkinsRule r) throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("testng.xml");
        publisher.setFailedSkips(100);
        publisher.setUnstableSkips(100);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); // to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_SKIPPED_TEST);
                build.getWorkspace().child("testng.xml").write(contents, "UTF-8");
                return true;
            }
        });

        // run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        assertSame(Result.SUCCESS, build.getResult());
    }

    @Test
    void test_threshold_for_skips_unstable(JenkinsRule r) throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("testng.xml");
        publisher.setUnstableSkips(0);
        publisher.setFailedSkips(100);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); // to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_SKIPPED_TEST);
                build.getWorkspace().child("testng.xml").write(contents, "UTF-8");
                return true;
            }
        });

        // run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        assertSame(Result.UNSTABLE, build.getResult());
    }

    @Test
    void test_threshold_for_fails_default(JenkinsRule r) throws Exception {
        assumeFalse(Functions.isWindows());
        /* Fails to delete a file on Windows agents of ci.jenkins.io.
         * Likely indicates a bug somewhere, but I'd rather have most
         * of the tests passing on ci.jenkins.io Windows rather than
         * blocking all Windows tests until this can be investigated.
         */

        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("testng.xml");
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); // to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_FAILED_TEST);
                build.getWorkspace().child("testng.xml").write(contents, "UTF-8");
                return true;
            }
        });

        // run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        assertSame(Result.UNSTABLE, build.getResult());
    }

    @Issue("JENKINS-27121")
    @Test
    void test_threshold_for_fails_default_pipeline(JenkinsRule r) throws Exception {
        assumeFalse(Functions.isWindows());
        /* Fails to delete a file on Windows agents of ci.jenkins.io.
         * Likely indicates a bug somewhere, but I'd rather have most
         * of the tests passing on ci.jenkins.io Windows rather than
         * blocking all Windows tests until this can be investigated.
         */

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        String contents = CommonUtil.getContents(Constants.TESTNG_FAILED_TEST);
        p.setDefinition(new CpsFlowDefinition(
                "node {\n  writeFile(file: 'testng-results.xml', text: '''"
                        + contents
                        + "''')\n  step([$class: 'Publisher'])\n}\n",
                true));
        WorkflowRun build = p.scheduleBuild2(0).get();
        r.assertBuildStatus(Result.UNSTABLE, build);
        TestNGTestResultBuildAction action = build.getAction(TestNGTestResultBuildAction.class);
        assertNotNull(action);
        TestNGResult result = action.getResult();
        assertEquals(
                "TestNGResult {totalTests=2, failedTests=1, skippedTests=0, failedConfigs=0, skippedConfigs=0}",
                result.toString(),
                "checking result details");
        r.assertLogContains("tests failed, which exceeded threshold of 0%. Marking build as UNSTABLE", build);
    }

    @Issue("JENKINS-27121")
    @Test
    void test_threshold_for_fails_default_pipeline_using_symbol(JenkinsRule r) throws Exception {
        assumeFalse(Functions.isWindows());
        /* Fails to delete a file on Windows agents of ci.jenkins.io.
         * Likely indicates a bug somewhere, but I'd rather have most
         * of the tests passing on ci.jenkins.io Windows rather than
         * blocking all Windows tests until this can be investigated.
         */

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        String contents = CommonUtil.getContents(Constants.TESTNG_FAILED_TEST);
        p.setDefinition(new CpsFlowDefinition(
                "node {\n  writeFile(file: 'testng-results.xml', text: '''" + contents + "''')\n  testNG()\n}\n",
                true));
        WorkflowRun build = p.scheduleBuild2(0).get();
        r.assertBuildStatus(Result.UNSTABLE, build);
        TestNGTestResultBuildAction action = build.getAction(TestNGTestResultBuildAction.class);
        assertNotNull(action);
        TestNGResult result = action.getResult();
        assertEquals(
                "TestNGResult {totalTests=2, failedTests=1, skippedTests=0, failedConfigs=0, skippedConfigs=0}",
                result.toString(),
                "checking result details");
        r.assertLogContains("tests failed, which exceeded threshold of 0%. Marking build as UNSTABLE", build);
    }

    @Test
    void test_threshold_for_fails_failure(JenkinsRule r) throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("testng.xml");
        publisher.setFailedFails(100);
        publisher.setUnstableFails(100);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); // to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_FAILED_TEST);
                build.getWorkspace().child("testng.xml").write(contents, "UTF-8");
                return true;
            }
        });

        // run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        assertSame(Result.SUCCESS, build.getResult());
    }

    @Test
    void test_threshold_for_fails_unstable(JenkinsRule r) throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("testng.xml");
        publisher.setFailedFails(100);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); // to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_FAILED_TEST);
                build.getWorkspace().child("testng.xml").write(contents, "UTF-8");
                return true;
            }
        });

        // run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        assertSame(Result.UNSTABLE, build.getResult());
    }
}
