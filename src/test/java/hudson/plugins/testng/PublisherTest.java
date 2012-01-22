package hudson.plugins.testng;

import hudson.FilePath;

import java.io.File;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @autor nullin
 */
public class PublisherTest extends TestCase
{
  public void testLocateReports() throws Exception
  {
    // Create a temporary workspace in the system
    File w = File.createTempFile("workspace", ".test");
    w.delete();
    w.mkdir();
    w.deleteOnExit();
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
    // Create a folder and move there 2 files
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
    Assert.assertEquals(2, reports.length);
    // Generate a includes string and look for files
    String includes = f1.getName() + "; " + f2.getName() + "; " + d1.getName();
    reports = Publisher.locateReports(workspace, includes);
    Assert.assertEquals(3, reports.length);
    // Save files in local workspace
    FilePath local = workspace.child("coverage_localfolder");
    boolean saved = Publisher.saveReports(local, reports, System.out);
    Assert.assertTrue(saved);
    Assert.assertEquals(3, local.list().size());
    local.deleteRecursive();
  }
}