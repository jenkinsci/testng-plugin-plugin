package hudson.plugins.testng.TestNGProjectAction

import hudson.plugins.testng.TestNGProjectAction
import hudson.plugins.testng.util.TestResultHistoryUtil

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

link(rel: "stylesheet", href:"${app.rootUrl}/plugin/testng-plugin/css/c3.min.css")

script(src:"${app.rootUrl}/plugin/testng-plugin/js/d3.min.js")
script(src:"${app.rootUrl}/plugin/testng-plugin/js/c3.min.js")
script(src:"${app.rootUrl}/plugin/testng-plugin/js/draw_results.js")


l.layout(title: "TestNG Results Trend") {
    st.include(page: "sidepanel.jelly", it: my.project)
    l.main_panel() {

        h1("TestNG Results Trends")
        if (my.isGraphActive()) {
            div(id: "chart")
            //img(lazymap: "graphMap?rel=../", alt: "[Test result trend chart]", src: "graph")
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

script() {
    text("\nvar data = ${my.getChartJson()};")
    text("\nresultsGraph('chart', data);")
}