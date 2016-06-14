package hudson.plugins.testng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.gargoylesoftware.htmlunit.html.DomNodeUtil;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
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
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import static org.junit.Assert.*;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

/**
 * Tests for {@link TestNGTestResultBuildAction}'s view page
 *
 * @author nullin
 */
public class TestNGTestResultBuildActionTest {

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule
    public JenkinsRule r = new JenkinsRule();

    /**
     * Test using precheckins xml
     *
     * @throws Exception
     */
    @Test
    public void testBuildAction_1() throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml");
        Publisher publisher = publisherCtor.getNewPublisher();
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

        //Get page
        HtmlPage page = r.createWebClient().goTo(build.getUrl() + PluginImpl.URL);

        //make sure no cell is empty
        List<HtmlElement> elements = DomNodeUtil.selectNodes(page, "//table[substring(@id, string-length(@id)- string-length('-tbl') +1)]/*/tr/td");
        for (HtmlElement element : elements) {
            assertTrue(!element.getTextContent().isEmpty());
        }

        //ensure only one failed test
        //there are three links in the cell, we pick the one without any id attr
        elements = DomNodeUtil.selectNodes(page, "//table[@id='fail-tbl']/tbody/tr/td/a[not(@id)]");
        assertEquals(1, elements.size());
        MethodResult mr = testngResult.getFailedTests().get(0);
        assertEquals(r.getURL() + mr.getRun().getUrl() + mr.getId(),
                elements.get(0).getAttribute("href"));
        assertEquals(((ClassResult)mr.getParent()).getCanonicalName() + "." + mr.getName(),
                elements.get(0).getTextContent());

        //ensure only one failed config method
        elements = DomNodeUtil.selectNodes(page, "//table[@id='fail-config-tbl']/tbody/tr/td/a");
        //asserting to 3, because a link for >>>, one for <<< and another for the method itself
        assertEquals(3, elements.size());
        mr = testngResult.getFailedConfigs().get(0);
        assertEquals(r.getURL() + mr.getRun().getUrl() + mr.getId(),
                elements.get(2).getAttribute("href"));
        assertEquals(((ClassResult)mr.getParent()).getCanonicalName() + "." + mr.getName(),
                elements.get(2).getTextContent());

        //ensure only one skipped test method
        elements = DomNodeUtil.selectNodes(page, "//table[@id='skip-tbl']/tbody/tr/td/a");
        assertEquals(1, elements.size());
        mr = testngResult.getSkippedTests().get(0);
        assertEquals(r.getURL() + mr.getRun().getUrl() + mr.getId(),
                elements.get(0).getAttribute("href"));
        assertEquals(((ClassResult)mr.getParent()).getCanonicalName() + "." + mr.getName(),
                elements.get(0).getTextContent());

        //ensure no skipped config
        elements = DomNodeUtil.selectNodes(page, "//table[@id='skip-config-tbl']");
        assertEquals(0, elements.size());

        //check list of packages and links
        elements = DomNodeUtil.selectNodes(page, "//table[@id='all-tbl']/tbody/tr/td/a");
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
        HtmlElement element = page.getElementById("fail-skip", true);
        r.assertStringContains(element.getTextContent(), "1 failure");
        assertFalse(element.getTextContent().contains("failures"));
        r.assertStringContains(element.getTextContent(), "1 skipped");
        element = page.getElementById("pass", true);
        r.assertStringContains(element.getTextContent(), "38 tests");
    }

    /**
     * Test using testng result xml
     *
     * @throws Exception
     */
    @Test
    public void testBuildAction_2() throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml");
        Publisher publisher = publisherCtor.getNewPublisher();
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

        //Get page
        HtmlPage page = r.createWebClient().goTo(build.getUrl() + PluginImpl.URL);

        //make sure no cell is empty
        List<HtmlElement> elements = DomNodeUtil.selectNodes(page, "//table[substring(@id, string-length(@id)- string-length('-tbl') +1)]/*/tr/td");
        for (HtmlElement element : elements) {
            assertTrue(!element.getTextContent().isEmpty());
        }

        //ensure only one failed test
        elements = DomNodeUtil.selectNodes(page, "//table[@id='fail-tbl']");
        assertEquals(0, elements.size());

        //ensure only one failed config method
        elements = DomNodeUtil.selectNodes(page, "//table[@id='fail-config-tbl']");
        assertEquals(0, elements.size());

        //ensure only one skipped test method
        elements = DomNodeUtil.selectNodes(page, "//table[@id='skip-tbl']");
        assertEquals(0, elements.size());

        //ensure no skipped config
        elements = DomNodeUtil.selectNodes(page, "//table[@id='skip-config-tbl']");
        assertEquals(0, elements.size());

        //check list of packages and links
        elements = DomNodeUtil.selectNodes(page, "//table[@id='all-tbl']/tbody/tr/td/a");
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
        HtmlElement element = page.getElementById("fail-skip", true);
        r.assertStringContains(element.getTextContent(), "0 failures");
        assertFalse(element.getTextContent().contains("skipped"));
        element = page.getElementById("pass", true);
        r.assertStringContains(element.getTextContent(), "526 tests");
    }

    @Test
    public void test_failed_config_default_setting() throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml");
        publisherCtor.setFailedSkips(100); //these prevent the skip that results from config failure from determining result
        publisherCtor.setUnstableSkips(100);
        Publisher publisher = publisherCtor.getNewPublisher();
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                                   BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_FAILED_TEST_CONFIG);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        assertSame(Result.SUCCESS, build.getResult());
    }

    @Test
    public void test_failed_config_enabled_failedbuild() throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml")
                .setFailureOnFailedTestConfig(true);
        publisherCtor.setFailedSkips(10); //these prevent the skip that results from config failure from determining result
        publisherCtor.setUnstableSkips(10);
        Publisher publisher = publisherCtor.getNewPublisher();
        p.getPublishersList().add(publisher);
        p.onCreatedFromScratch(); //to setup project action

        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                                   BuildListener listener) throws InterruptedException, IOException {
                String contents = CommonUtil.getContents(Constants.TESTNG_FAILED_TEST_CONFIG);
                build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
                return true;
            }
        });

        //run build
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        assertSame(Result.FAILURE, build.getResult());
    }
    @Test
    public void test_threshold_for_skips_default() throws Exception {
       FreeStyleProject p = r.createFreeStyleProject();
       PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml");
       Publisher publisher = publisherCtor.getNewPublisher();
       p.getPublishersList().add(publisher);
       p.onCreatedFromScratch(); //to setup project action

       p.getBuildersList().add(new TestBuilder() {
          @Override
          public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                                 BuildListener listener) throws InterruptedException, IOException {
             String contents = CommonUtil.getContents(Constants.TESTNG_SKIPPED_TEST);
             build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
             return true;
          }
       });

       //run build
       FreeStyleBuild build = p.scheduleBuild2(0).get();
       assertSame(Result.SUCCESS, build.getResult());
    }
    
   @Test
   public void test_threshold_for_skips_failure() throws Exception {
      FreeStyleProject p = r.createFreeStyleProject();
      PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml")
            .setFailedSkips(100).setUnstableSkips(100);
      Publisher publisher = publisherCtor.getNewPublisher();
      p.getPublishersList().add(publisher);
      p.onCreatedFromScratch(); //to setup project action

      p.getBuildersList().add(new TestBuilder() {
         @Override
         public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                                BuildListener listener) throws InterruptedException, IOException {
            String contents = CommonUtil.getContents(Constants.TESTNG_SKIPPED_TEST);
            build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
            return true;
         }
      });

      //run build
      FreeStyleBuild build = p.scheduleBuild2(0).get();
      assertSame(Result.SUCCESS, build.getResult());
   }

   @Test
   public void test_threshold_for_skips_unstable() throws Exception {
      FreeStyleProject p = r.createFreeStyleProject();
      PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml")
            .setUnstableSkips(0).setFailedSkips(100);
      Publisher publisher = publisherCtor.getNewPublisher();
      p.getPublishersList().add(publisher);
      p.onCreatedFromScratch(); //to setup project action

      p.getBuildersList().add(new TestBuilder() {
         @Override
         public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                                BuildListener listener) throws InterruptedException, IOException {
            String contents = CommonUtil.getContents(Constants.TESTNG_SKIPPED_TEST);
            build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
            return true;
         }
      });

      //run build
      FreeStyleBuild build = p.scheduleBuild2(0).get();
      assertSame(Result.UNSTABLE, build.getResult());
   }
   
   @Test
   public void test_threshold_for_fails_default() throws Exception {
      FreeStyleProject p = r.createFreeStyleProject();
      PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml");
      Publisher publisher = publisherCtor.getNewPublisher();
      p.getPublishersList().add(publisher);
      p.onCreatedFromScratch(); //to setup project action

      p.getBuildersList().add(new TestBuilder() {
         @Override
         public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                                BuildListener listener) throws InterruptedException, IOException {
            String contents = CommonUtil.getContents(Constants.TESTNG_FAILED_TEST);
            build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
            return true;
         }
      });

      //run build
      FreeStyleBuild build = p.scheduleBuild2(0).get();
      assertSame(Result.UNSTABLE, build.getResult());
   }

   @Issue("JENKINS-27121")
   @Test
   public void test_threshold_for_fails_default_pipeline() throws Exception {
      WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
      String contents = CommonUtil.getContents(Constants.TESTNG_FAILED_TEST);
      r.jenkins.getWorkspaceFor(p).child("testng.xml").write(contents, "UTF-8");
      p.setDefinition(new CpsFlowDefinition("node {step([$class: 'Publisher', reportFilenamePattern: 'testng.xml', escapeTestDescp: true, escapeExceptionMsg: true, showFailedBuilds: false, failureOnFailedTestConfig: false, unstableSkips: 100, unstableFails: 0, failedSkips: 100, failedFails: 100, thresholdMode: 2])}", true));
      WorkflowRun build = p.scheduleBuild2(0).get();
      r.assertBuildStatus(Result.UNSTABLE, build);
      TestNGTestResultBuildAction action = build.getAction(TestNGTestResultBuildAction.class);
      assertNotNull(action);
      TestNGResult result = action.getResult();
      assertEquals("checking result details", "TestNGResult {totalTests=2, failedTests=1, skippedTests=0, failedConfigs=0, skippedConfigs=0}", result.toString());
      r.assertLogContains("tests failed, which exceeded threshold of 0%. Marking build as UNSTABLE", build);
   }

  @Test
  public void test_threshold_for_fails_failure() throws Exception {
     FreeStyleProject p = r.createFreeStyleProject();
     PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml")
           .setFailedFails(100).setUnstableFails(100);
     Publisher publisher = publisherCtor.getNewPublisher();
     p.getPublishersList().add(publisher);
     p.onCreatedFromScratch(); //to setup project action

     p.getBuildersList().add(new TestBuilder() {
        @Override
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                               BuildListener listener) throws InterruptedException, IOException {
           String contents = CommonUtil.getContents(Constants.TESTNG_FAILED_TEST);
           build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
           return true;
        }
     });

     //run build
     FreeStyleBuild build = p.scheduleBuild2(0).get();
     assertSame(Result.SUCCESS, build.getResult());
  }

  @Test
  public void test_threshold_for_fails_unstable() throws Exception {
     FreeStyleProject p = r.createFreeStyleProject();
     PublisherCtor publisherCtor = new PublisherCtor().setReportFilenamePattern("testng.xml")
           .setFailedFails(100);
     Publisher publisher = publisherCtor.getNewPublisher();
     p.getPublishersList().add(publisher);
     p.onCreatedFromScratch(); //to setup project action

     p.getBuildersList().add(new TestBuilder() {
        @Override
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                               BuildListener listener) throws InterruptedException, IOException {
           String contents = CommonUtil.getContents(Constants.TESTNG_FAILED_TEST);
           build.getWorkspace().child("testng.xml").write(contents,"UTF-8");
           return true;
        }
     });

     //run build
     FreeStyleBuild build = p.scheduleBuild2(0).get();
     assertSame(Result.UNSTABLE, build.getResult());
  }
}
