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

        div(id: "pie")
        script() {
            text("var passCount = ${my.result.passCount};")
            text("\nvar skipCount = ${my.result.skipCount};")
            text("\nvar failCount = ${my.result.failCount};")
            text("\nresultsGraph('pie', passCount, skipCount, failCount);")
        }

        div(id: "pass", align: "left") {
            text("${my.result.totalCount} test${my.totalCount != 1 ? "s" : ""}")
            if (prevResult) {
                text("(${Functions.getDiffString(my.result.totalCount - prevResult.totalCount)})")
            }
        }
    }
}