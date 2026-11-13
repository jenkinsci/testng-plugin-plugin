package hudson.plugins.testng;

import hudson.Plugin;
import hudson.model.Run;

/**
 * Entry point of TestNG Results plugin.
 *
 * <p>TODO: should move to newer supported way of initializing plugins
 */
public class PluginImpl extends Plugin {

    public static final String DISPLAY_NAME = "TestNG Results";
    public static final String GRAPH_NAME = "TestNG Results Trend";
    public static final String URL = "testngreports";
    // Jenkins Symbol string, not a file path, so it auto-scales like core's own icons.
    public static final String ICON_FILE_NAME = "symbol-testng plugin-testng-plugin";

    // Cache-busting token for this plugin's JS/CSS ("?v=" param); changes every reload.
    public static final long RESOURCE_VERSION = System.currentTimeMillis();

    public void start() throws Exception {
        // this is the name with which older build actions are stored with in build.xml files
        // here for backward compatibility
        Run.XSTREAM.alias("hudson.plugins.testng.TestNGBuildAction", TestNGTestResultBuildAction.class);
        // this will be written to the build.xml file when saving TestNG build action
        Run.XSTREAM.alias("testngBuildAction", TestNGTestResultBuildAction.class);
    }
}
