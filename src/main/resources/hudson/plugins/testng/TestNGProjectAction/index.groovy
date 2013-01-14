package hudson.plugins.testng.TestNGProjectAction

import hudson.plugins.testng.util.TestResultHistoryUtil

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

l.layout(title: "TestNG Results Trend") {
    st.include(page: "sidepanel.jelly", it: my.project)
    l.main_panel() {

        h1("TestNG Results Trends")
        if (my.isGraphActive()) {
            img(lazymap: "graphMap?rel=${my.project.lastCompletedBuild.upUrl}", alt: "[Test result trend chart]", src: "graph")
        } else {
            p("Need at least 2 builds with results to show trend graph")
        }

        br()
        def buildNumber = my.project.lastCompletedBuild.number
        h2() {
            text("Latest Test Results (")
            a(href: "${my.project.lastCompletedBuild.upUrl}${buildNumber}/${my.urlName}") {
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