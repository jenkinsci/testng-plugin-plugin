package hudson.plugins.testng;

import java.io.IOException;

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
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

/**
 * Tests for {@link TestNGProjectAction}
 *
 * @author nullin
 */
public class TestNGProjectActionTest {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    /**
     * Test:
     *
     * 1. Make sure that settings configured in Project are saved
     * correctly in ProjectAction.
     * 2. Also validate that the latest build result is returned correctly by
     * ProjectAction
     * 3. And, verify that results are read correctly even when XML file doesn't have
     * 'testng' string in the name at all
     *
     * @throws Exception
     */
    @Test
    public void testSettings() throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("some.xml");
        publisher.setEscapeTestDescp(false);
        publisher.setEscapeExceptionMsg(true);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                //any testng xml will do
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_EXP_MSG_XML);
                build.getWorkspace().child("some.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();

        //assert of test result
        Assert.assertNotNull(build.getAction(AbstractTestResultAction.class));
        TestResult testResult;
        Assert.assertNotNull(testResult = (TestResult) build.getAction(AbstractTestResultAction.class).getResult());
        Assert.assertTrue(testResult.getTotalCount() > 0);

        //assert on project action
        TestNGProjectAction projAction;
        Assert.assertNotNull(projAction = build.getProject().getAction(TestNGProjectAction.class));
        Assert.assertFalse(projAction.getEscapeTestDescp());
        Assert.assertTrue(projAction.getEscapeExceptionMsg());
        Assert.assertSame(testResult, projAction.getLastCompletedBuildAction().getResult());

        String summary = TestResultHistoryUtil.toSummary(projAction.getLastCompletedBuildAction());
        Assert.assertTrue(summary.contains("gov.nasa.jpl.FoobarTests.b"));
    }

    @Test
    // For JENKINS-32746: TestNG Results Trend graph doesn't show all build results
    public void testHistoryRemoval() throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("some.xml");
        publisher.setEscapeTestDescp(false);
        publisher.setEscapeExceptionMsg(true);
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                //any testng xml will do
                String contents = CommonUtil.getContents(Constants.TESTNG_XML_EXP_MSG_XML);
                build.getWorkspace().child("some.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        int buildNumber = 5;
        FreeStyleBuild[] builds = new FreeStyleBuild[buildNumber];
        for (int i = 0; i < buildNumber; i++) {
            builds[i] = p.scheduleBuild2(0).get();
        }

        TestNGProjectAction action = p.getAction(TestNGProjectAction.class);

        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
        action.populateDataSetBuilder(dataSetBuilder);

        Assert.assertEquals(buildNumber, dataSetBuilder.build().getColumnCount());

        int[] buildsToRemove = { 2, 3 };
        for (int buildToRemove : buildsToRemove) {
            builds[buildToRemove].delete();
        }

        dataSetBuilder = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
        action.populateDataSetBuilder(dataSetBuilder);

        Assert.assertEquals((buildNumber - buildsToRemove.length), dataSetBuilder.build().getColumnCount());
    }
}
