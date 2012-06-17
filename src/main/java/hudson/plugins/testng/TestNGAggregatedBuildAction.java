package hudson.plugins.testng;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.Action;
import hudson.plugins.testng.results.TestResults;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestNGAggregatedBuildAction extends TestNGBuildAction implements MavenAggregatedReport {

    private static final long serialVersionUID = 1L;
    private MavenModuleSetBuild mmsb;
    
    
    private transient boolean testResultsInitialized = false;
    
    private static final TestResults DUMMY_RESULT = new TestResults("$$TestNGAggregatedBuildAction-dummy$$");

    public TestNGAggregatedBuildAction(MavenModuleSetBuild build) {
        super(build, DUMMY_RESULT); // pass a dummy result up to make the that results are calculated
        // on the 1st #getResults call
        this.mmsb = build;
    }
    
    @Override
    public TestResults getResults() {
        if (testResults == null) {
            testResults = new WeakReference<TestResults>(aggregateResults(this.mmsb));
            return testResults.get();
         }

         TestResults tr = testResults.get();
         if (tr == null || tr.getName().equals(DUMMY_RESULT.getName())) {
            testResults = new WeakReference<TestResults>(aggregateResults(this.mmsb));
           return testResults.get();
         } else{
           return tr;
         }
    }
    
    @Override
    public int getPassedTestCount() {
        if (!testResultsInitialized) {
            getResults();
        }
        
        return super.getPassedTestCount();
    }

    @Override
    public int getFailedTestCount() {
        if (!testResultsInitialized) {
            getResults();
        }
        
        return super.getFailedTestCount();
    }

    @Override
    public int getSkippedTestCount() {
        if (!testResultsInitialized) {
            getResults();
        }
        
        return super.getSkippedTestCount();
    }

    private TestResults aggregateResults(MavenModuleSetBuild build) {
        
        Map<MavenModule, MavenBuild> moduleLastBuilds = build.getModuleLastBuilds();
        
        TestResults aggregatedResults = new TestResults(UUID.randomUUID().toString());
        for (MavenBuild mb : moduleLastBuilds.values()) {
            if (mb.getAction(TestNGBuildAction.class) != null) {
                TestNGBuildAction action = mb.getAction(TestNGBuildAction.class);
                TestResults moduleResults = action.getResults();
                
                aggregatedResults.add(moduleResults);
            }
        }
        
        aggregatedResults.tally();
        
        passedTestCount = aggregatedResults.getPassedTestCount();
        failedTestCount = aggregatedResults.getFailedTestCount();
        skippedTestCount = aggregatedResults.getSkippedTestCount();
        
        testResultsInitialized = true;
        
        return aggregatedResults;
    }

    public void update(Map<MavenModule, List<MavenBuild>> moduleBuilds,
            MavenBuild newBuild) {
    }

    public Class<? extends AggregatableAction> getIndividualActionType() {
        return TestNGBuildAction.class;
    }

    public Action getProjectAction(MavenModuleSet moduleSet) {
        return new TestNGProjectAction(moduleSet, true, true); // TODO: how to take the configured values for escapeTestDescp, escapeExceptionMsg?
    }

    
    protected Object readResolve() {
     // The following doesn't work, because the module list is not initialized, yet, so aggregateResults
        // would return an empty TestResults
        
//       TestResults testResults = getResults();
//
//       //initialize the cached values
//       passedTestCount = testResults.getPassedTestCount();
//       failedTestCount = testResults.getFailedTestCount();
//       skippedTestCount = testResults.getSkippedTestCount();

       return this;
    }

}
