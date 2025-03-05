package hudson.plugins.testng;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;
import org.jenkinsci.plugins.structs.describable.DescribableModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Tests the {@link Publisher}
 *
 * @author nullin
 */
@WithJenkins
public class PublisherTest {

    private JenkinsRule r;

    @TempDir
    private File tmp;

    /** Reset SECURITY-2788 escape hatch before each test. */
    @BeforeEach
    void disallowUnescapedHTML(JenkinsRule r) {
        this.r = r;
        Publisher.setAllowUnescapedHTML(false);
    }

    @WithoutJenkins
    @Test
    void testLocateReports() throws Exception {
        // Create a temporary workspace in the system
        File w = newFolder(tmp, "junit");
        FilePath workspace = new FilePath(w);
        // Create 4 files in the workspace
        File f1 = File.createTempFile("testng-results", ".xml", w);
        f1.deleteOnExit();
        File f2 = File.createTempFile("anyname", ".xml", w);
        f2.deleteOnExit();
        File f3 = File.createTempFile("testng-results", ".xml", w);
        f3.deleteOnExit();
        File f4 = File.createTempFile("anyname", ".xml", w);
        f4.deleteOnExit();
        // Create a folder and move 2 files there
        File d1 = new File(workspace.child("subdir").getRemote());
        d1.mkdir();
        d1.deleteOnExit();
        File f5 = new File(workspace.child(d1.getName()).child(f3.getName()).getRemote());
        File f6 = new File(workspace.child(d1.getName()).child(f4.getName()).getRemote());
        f3.renameTo(f5);
        f4.renameTo(f6);
        f5.deleteOnExit();
        f6.deleteOnExit();
        // Look for files in the entire workspace recursively without providing
        // the includes parameter
        FilePath[] reports = Publisher.locateReports(workspace, "**/testng*.xml");
        assertEquals(2, reports.length);
        // Generate a includes string and look for files
        String includes = f1.getName() + "; " + f2.getName() + "; " + d1.getName();
        reports = Publisher.locateReports(workspace, includes);
        assertEquals(3, reports.length);
        // Save files in local workspace
        FilePath local = workspace.child("publishertest_localfolder");
        boolean saved = Publisher.saveReports(local, reports, System.out);
        assertTrue(saved);
        assertEquals(3, local.list().size());
        local.deleteRecursive();
    }

    @WithoutJenkins
    @Test
    void testBuildAborted() throws Exception {
        Publisher publisher = new Publisher();
        publisher.setReportFilenamePattern("testng.xml");
        publisher.setEscapeTestDescp(false);
        publisher.setEscapeExceptionMsg(false);
        publisher.setShowFailedBuilds(false);
        Launcher launcherMock = mock(Launcher.class);
        AbstractBuild<?, ?> buildMock = mock(AbstractBuild.class);
        BuildListener listenerMock = mock(BuildListener.class);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);

        when(buildMock.getResult()).thenReturn(Result.ABORTED);
        when(listenerMock.getLogger()).thenReturn(ps);

        publisher.perform(buildMock, buildMock.getWorkspace(), launcherMock, listenerMock);

        String str = os.toString();
        assertTrue(str.contains("Build Aborted"));
    }

    @Test
    void testRoundTrip() throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        Publisher before = new Publisher();
        before.setReportFilenamePattern("");
        before.setEscapeTestDescp(false);
        before.setEscapeExceptionMsg(false);
        before.setShowFailedBuilds(true);
        before.setFailureOnFailedTestConfig(false);
        before.setUnstableSkips(0);
        before.setUnstableFails(0);
        before.setFailedSkips(0);
        before.setFailedFails(0);
        before.setThresholdMode(1);
        p.getPublishersList().add(before);

        /* Even though set to false by earlier calls to setters, setting is ignored */
        assertTrue(before.getEscapeTestDescp()); // SECURITY-2788 - prevent XSS from test description
        assertTrue(before.getEscapeExceptionMsg()); // SECURITY-2788 - prevent XSS from test exception

        r.submit(r.createWebClient().getPage(p, "configure").getFormByName("config"));

        Publisher after = p.getPublishersList().get(Publisher.class);

        assertTrue(after.getEscapeTestDescp()); // SECURITY-2788 - prevent XSS from test description
        assertTrue(after.getEscapeExceptionMsg()); // SECURITY-2788 - prevent XSS from test exception

        r.assertEqualBeans(before, after, "reportFilenamePattern,escapeTestDescp,escapeExceptionMsg,showFailedBuilds");
    }

    @Issue("JENKINS-27121")
    @WithoutJenkins
    @Test
    void testDefaultFields() {
        DescribableModel<Publisher> model = new DescribableModel<>(Publisher.class);
        Map<String, Object> args = new TreeMap<>();
        Publisher p = model.instantiate(args);
        assertEquals("**/testng-results.xml", p.getReportFilenamePattern());
        assertTrue(p.getEscapeExceptionMsg());
        assertTrue(p.getEscapeTestDescp());
        assertFalse(p.getShowFailedBuilds());
        assertEquals(args, model.uninstantiate(model.instantiate(args)));
        args.put("reportFilenamePattern", "results.xml");
        assertEquals(args, model.uninstantiate(model.instantiate(args)));
        args.put("showFailedBuilds", true);
        assertEquals(args, model.uninstantiate(model.instantiate(args)));
    }

    @Issue("SECURITY-2788")
    @WithoutJenkins
    @Test
    void testUnescapedFields() {
        Publisher.setAllowUnescapedHTML(true);
        DescribableModel<Publisher> model = new DescribableModel<>(Publisher.class);
        Map<String, Object> args = new TreeMap<>();
        Publisher p = model.instantiate(args);

        assertTrue(p.getEscapeExceptionMsg());
        p.setEscapeExceptionMsg(false);
        assertFalse(p.getEscapeExceptionMsg());
        p.setEscapeExceptionMsg(true);
        assertTrue(p.getEscapeExceptionMsg());

        assertTrue(p.getEscapeTestDescp());
        p.setEscapeTestDescp(false);
        assertFalse(p.getEscapeTestDescp());
        p.setEscapeTestDescp(true);
        assertTrue(p.getEscapeTestDescp());
    }

    /* Used by other tests to modify allowUnescapedHTML flag */
    public static void setAllowUnescapedHTML(boolean value) {
        Publisher.setAllowUnescapedHTML(value);
    }

    private static File newFolder(File root, String... subDirs) throws IOException {
        String subFolder = String.join("/", subDirs);
        File result = new File(root, subFolder);
        if (!result.mkdirs()) {
            throw new IOException("Couldn't create folders " + root);
        }
        return result;
    }
}
