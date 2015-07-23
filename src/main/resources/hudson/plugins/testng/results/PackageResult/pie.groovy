package hudson.plugins.testng.results.PackageResult

import hudson.Functions

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

def prevResult = my.previousResult

div() {
    if (my.totalCount == 0) {
        text("No test results")
    } else {
        div(id:"fail-skip") {
            text("${my.failCount} failure${my.failCount != 1 ? "s" : ""}")
            if (prevResult) {
                text("(${Functions.getDiffString(my.failCount - prevResult.failCount)})")
            }
            if (my.skipCount > 0) {
                text(", ${my.skipCount} skipped")
                if (prevResult) {
                    text("(${Functions.getDiffString(my.skipCount - prevResult.skipCount)})")
                }
            }
        }

        div(id: "pie")
        script() {
            text("var passCount = ${my.passCount};")
            text("\nvar skipCount = ${my.skipCount};")
            text("\nvar failCount = ${my.failCount};")
            text("\nresultsGraph('pie', passCount, skipCount, failCount);")
        }

        div(id: "pass", align: "left") {
            text("${my.totalCount} test${my.totalCount != 1 ? "s" : ""}")
            if (prevResult) {
                text("(${Functions.getDiffString(my.totalCount - prevResult.totalCount)})")
            }
        }
    }
}