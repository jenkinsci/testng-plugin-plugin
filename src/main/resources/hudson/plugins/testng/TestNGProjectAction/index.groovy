package hudson.plugins.testng.TestNGProjectAction

import hudson.plugins.testng.util.TestResultHistoryUtil

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

l.layout(title: "TestNG Results Trend") {
    st.include(page: "sidepanel.jelly", it: my.project)
    l.main_panel() {
        link(rel: "stylesheet", href: "${resURL}/plugin/testng-plugin/css/testng.css?v=${hudson.plugins.testng.PluginImpl.RESOURCE_VERSION}")
        // Recolors the PNG to match the active theme; see theme_graph.js.
        script(src: "${resURL}/plugin/testng-plugin/js/theme_graph.js?v=${hudson.plugins.testng.PluginImpl.RESOURCE_VERSION}")

        h1("TestNG Results Trends")
        if (my.isGraphActive()) {
            div(class: "testng-graph-frame") {
                // scale=2 for a retina-sharp PNG; width/height keep the on-screen size.
                img(class: "testng-theme-graph", lazymap: "graphMap?rel=../", alt: "[Test result trend chart]",
                        src: "graph?scale=2&v=${hudson.plugins.testng.PluginImpl.RESOURCE_VERSION}",
                        width: "500", height: "200")
            }
        } else {
            p("Need at least 2 builds with results to show trend graph")
        }

        br()
        def buildNumber = my.project.lastCompletedBuild.number
        h2() {
            text("Latest Test Results (")
            a(href: "${my.upUrl}${buildNumber}/${my.urlName}") {
                text("build #${buildNumber}")
            }
            text(")")
        }

        def lastCompletedBuildAction = my.lastCompletedBuildAction
        if (lastCompletedBuildAction) {
            p() {
                raw("${TestResultHistoryUtil.toSummary(lastCompletedBuildAction)}")
            }
        } else {
            p("No builds have successfully recorded TestNG results yet")
        }
    }
}