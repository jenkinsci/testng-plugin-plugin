package hudson.plugins.testng;

import static org.junit.jupiter.api.Assertions.*;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.plugins.testng.util.TestResultHistoryUtil;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Tests for {@link TestNGProjectAction}
 *
 * @author nullin
 */
@WithJenkins
class TestNGProjectActionTest {

    private JenkinsRule r;

    @BeforeEach
    void allowUnescapedHTML(JenkinsRule r) {
        this.r = r;
        /* Open the SECURITY-2788 escape hatch */
        Publisher.setAllowUnescapedHTML(true);
    }

    @AfterEach
    void disallowUnescapedHTML() {
        /* Close the SECURITY-2788 escape hatch */
        Publisher.setAllowUnescapedHTML(false);
    }

    /**
     * Test:
     *
     * <p>1. Make sure that settings configured in Project are saved correctly in ProjectAction. 2.
     * Also validate that the latest build result is returned correctly by ProjectAction 3. And,
     * verify that results are read correctly even when XML file doesn't have 'testng' string in the
     * name at all
     *
     * @throws Exception
     */
    @Test
    void testSettings() throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("some.xml");
        publisher.setEscapeTestDescp(false); // Relies on SECURITY-2788 escape hatch being open
        publisher.setEscapeExceptionMsg(true);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); // to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                // any testng xml will do
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_EXP_MSG_XML);
                build.getWorkspace().child("some.xml").write(contents, "UTF-8");
                return true;
            }
        });

        // run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();

        // assert of test result
        assertNotNull(build.getAction(AbstractTestResultAction.class));
        TestResult testResult;
        assertNotNull(
                testResult = (TestResult)
                        build.getAction(AbstractTestResultAction.class).getResult());
        assertTrue(testResult.getTotalCount() > 0);

        // assert on project action
        TestNGProjectAction projAction;
        assertNotNull(projAction = build.getProject().getAction(TestNGProjectAction.class));
        assertFalse(projAction.getEscapeTestDescp()); // Relies on SECURITY-2788 escape hatch being open
        assertTrue(projAction.getEscapeExceptionMsg());
        assertSame(testResult, projAction.getLastCompletedBuildAction().getResult());

        String summary = TestResultHistoryUtil.toSummary(projAction.getLastCompletedBuildAction());
        assertTrue(summary.contains("gov.nasa.jpl.FoobarTests.b"));
    }

    // For JENKINS-32746: TestNG Results Trend graph doesn't show all build results
    @Test
    void testHistoryRemoval() throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("some.xml");
        publisher.setEscapeExceptionMsg(true);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); // to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                // any testng xml will do
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_EXP_MSG_XML);
                build.getWorkspace().child("some.xml").write(contents, "UTF-8");
                return true;
            }
        });

        // run build
        int buildNumber = 5;
        FreeStyleBuild[] builds = new FreeStyleBuild[buildNumber];
        for (int i = 0; i < buildNumber; i++) {
            builds[i] = p.scheduleBuild2(0).get();
        }

        TestNGProjectAction action = p.getAction(TestNGProjectAction.class);

        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder = new DataSetBuilder<>();
        action.populateDataSetBuilder(dataSetBuilder);

        assertEquals(buildNumber, dataSetBuilder.build().getColumnCount());

        int[] buildsToRemove = {2, 3};
        for (int buildToRemove : buildsToRemove) {
            builds[buildToRemove].delete();
        }

        dataSetBuilder = new DataSetBuilder<>();
        action.populateDataSetBuilder(dataSetBuilder);

        assertEquals(
                (buildNumber - buildsToRemove.length), dataSetBuilder.build().getColumnCount());
    }

    private FreeStyleProject runNewProjectWithTestNGResults(boolean escapeTestDescp, boolean escapeExceptionMsg)
            throws Exception {
        FreeStyleProject project = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("some.xml");
        publisher.setEscapeExceptionMsg(escapeExceptionMsg);
        publisher.setEscapeTestDescp(escapeTestDescp);
        project.getPublishersList().add(publisher);
        project.onCreatedFromScratch(); // to setup project action

        project.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                // any testng xml will do
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_EXP_MSG_XML);
                build.getWorkspace().child("some.xml").write(contents, "UTF-8");
                return true;
            }
        });

        project.scheduleBuild2(0).get();
        return project;
    }

    @Test
    void testGetEscapeTestDescp() throws Exception {
        Publisher.setAllowUnescapedHTML(false); // Close the escape hatch for this test
        FreeStyleProject project = runNewProjectWithTestNGResults(false, false);
        /* false ignored because hatch is closed */
        TestNGProjectAction action = project.getAction(TestNGProjectAction.class);
        assertTrue(action.getEscapeTestDescp());
    }

    @Test
    void testGetEscapeTestDescpAllowXSS() throws Exception {
        Publisher.setAllowUnescapedHTML(true); // Open the escape hatch for this test
        FreeStyleProject project = runNewProjectWithTestNGResults(false, false);
        /* false honored because hatch is open */
        TestNGProjectAction action = project.getAction(TestNGProjectAction.class);
        assertFalse(action.getEscapeTestDescp());
    }

    @Test
    void testGetEscapeExceptionMsg() throws Exception {
        Publisher.setAllowUnescapedHTML(false); // Close the escape hatch for this test
        FreeStyleProject project = runNewProjectWithTestNGResults(false, false);
        /* false ignored because hatch is closed */
        TestNGProjectAction action = project.getAction(TestNGProjectAction.class);
        assertTrue(action.getEscapeExceptionMsg());
    }

    @Test
    void testGetEscapeExceptionMsgAllowXSS() throws Exception {
        Publisher.setAllowUnescapedHTML(true); // Open the escape hatch for this test
        FreeStyleProject project = runNewProjectWithTestNGResults(false, false);
        /* false honored because hatch is open */
        TestNGProjectAction action = project.getAction(TestNGProjectAction.class);
        assertFalse(action.getEscapeExceptionMsg());
    }
}
