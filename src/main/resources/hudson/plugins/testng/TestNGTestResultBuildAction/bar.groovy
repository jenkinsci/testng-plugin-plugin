package hudson.plugins.testng.TestNGTestResultBuildAction

import hudson.Functions
import hudson.plugins.testng.util.TestResultHistoryUtil

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

def prevResult = TestResultHistoryUtil.getPreviousBuildTestResults(my.run)

div() {
    if (my.result.totalCount == 0) {
        text("No test results")
    } else {
        div(id: "fail-skip") {
            text("${my.result.failCount} failure${my.failCount != 1 ? "s" : ""}")
            if (prevResult) {
                text("(${Functions.getDiffString(my.result.failCount - prevResult.failCount)})")
            }
            if (my.result.skipCount > 0) {
                text(", ${my.result.skipCount} skipped")
                if (prevResult) {
                    text("(${Functions.getDiffString(my.result.skipCount - prevResult.skipCount)})")
                }
            }
        }

        def failpc = my.result.failCount * 100 / my.result.totalCount
        def skippc = my.result.skipCount * 100 / my.result.totalCount
        def passpc = 100 - failpc - skippc
        div(class: "testng-progress-bar") {
            div(class: "testng-segment testng-segment--pass", style: "width:${passpc}%",
                    title: "${my.result.totalCount - my.result.failCount - my.result.skipCount} passed")
            div(class: "testng-segment testng-segment--fail", style: "width:${failpc}%",
                    title: "${my.result.failCount} failed")
            div(class: "testng-segment testng-segment--skip", style: "width:${skippc}%",
                    title: "${my.result.skipCount} skipped")
        }

        div(id: "pass", align: "right") {
            text("${my.result.totalCount} test${my.totalCount != 1 ? "s" : ""}")
            if (prevResult) {
                text("(${Functions.getDiffString(my.result.totalCount - prevResult.totalCount)})")
            }
        }
    }
}