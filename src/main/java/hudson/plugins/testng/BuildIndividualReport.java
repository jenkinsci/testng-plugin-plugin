package hudson.plugins.testng;

import hudson.model.AbstractBuild;
import hudson.plugins.testng.results.TestResults;

/**
 * This class serves the purpose of backward compatibility only. Having
 * it here ensures that results for builds using older version of this plugin
 * continue to be displayed
 *
 * @deprecated since v0.22
 * @author nullin
 * @see TestNGBuildAction
 */
public final class BuildIndividualReport extends TestNGBuildAction
{
   public BuildIndividualReport(AbstractBuild<?, ?> build,
         TestResults testngResults)
   {
      super(build, testngResults);
   }
}
