package hudson.plugins.testng;

import java.io.IOException;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.test.TestResult;
import junit.framework.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TestBuilder;

/**
 * Tests for {@link TestNGProjectAction}
 *
 * @author nullin
 */
public class TestNGProjectActionTest extends HudsonTestCase {

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
        FreeStyleProject p = createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("some.xml")
                        .setEscapeTestDescp(false).setEscapeExceptionMsg(true);
        Publisher publisher = publisherCtor.getNewPublisher();
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
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
        Assert.assertNotNull(build.getTestResultAction());
        TestResult testResult;
        Assert.assertNotNull(testResult = (TestResult) build.getTestResultAction().getResult());
        Assert.assertTrue(testResult.getTotalCount() > 0);

        //assert on project action
        TestNGProjectAction projAction;
        Assert.assertNotNull(projAction = build.getProject().getAction(TestNGProjectAction.class));
        Assert.assertFalse(projAction.getEscapeTestDescp());
        Assert.assertTrue(projAction.getEscapeExceptionMsg());
        Assert.assertSame(testResult, projAction.getLastCompletedBuildAction().getResult());
    }
}
