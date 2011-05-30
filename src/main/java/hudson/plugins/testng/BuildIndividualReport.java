package hudson.plugins.testng;

import hudson.model.AbstractBuild;
import hudson.plugins.testng.results.TestResults;

import java.util.Collection;

/**
 * This class serves the purpose of backward compatibility only. Having
 * it here ensures that results for builds using older version of this plugin
 * continue to be displayed
 *
 * @author nullin
 * @see TestNGBuildAction
 */
@Deprecated
public final class BuildIndividualReport extends TestNGBuildAction
{
   public BuildIndividualReport(AbstractBuild<?, ?> build,
         Collection<TestResults> testngResults)
   {
      super(build, testngResults);
   }
}
