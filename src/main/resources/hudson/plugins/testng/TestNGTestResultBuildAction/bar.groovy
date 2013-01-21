package hudson.plugins.testng.TestNGTestResultBuildAction

import hudson.Functions
import hudson.plugins.testng.util.TestResultHistoryUtil

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

def prevResult = TestResultHistoryUtil.getPreviousBuildTestResults(my.owner)

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

        div(style: "width:100%; height:1em; background-color: #729FCF") {
            def failpc = my.result.failCount * 100 / my.result.totalCount
            def skippc = my.result.skipCount * 100 / my.result.totalCount
            div(style: "width:${failpc}%; height: 1em; background-color: #EF2929; float: left")
            div(style: "width:${skippc}%; height: 1em; background-color: #FCE94F; float: left")
        }

        div(id: "pass", align: "right") {
            text("${my.result.totalCount} test${my.totalCount != 1 ? "s" : ""}")
            if (prevResult) {
                text("(${Functions.getDiffString(my.result.totalCount - prevResult.totalCount)})")
            }
        }
    }
}