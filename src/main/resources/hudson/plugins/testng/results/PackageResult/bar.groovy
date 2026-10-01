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

        def failpc = my.failCount * 100 / my.totalCount
        def skippc = my.skipCount * 100 / my.totalCount
        def passpc = 100 - failpc - skippc
        div(id: "bar", class: "testng-progress-bar") {
            div(class: "testng-segment testng-segment--pass", style: "width:${passpc}%",
                    title: "${my.totalCount - my.failCount - my.skipCount} passed")
            div(class: "testng-segment testng-segment--fail", style: "width:${failpc}%",
                    title: "${my.failCount} failed")
            div(class: "testng-segment testng-segment--skip", style: "width:${skippc}%",
                    title: "${my.skipCount} skipped")
        }

        div(id: "pass", align: "right") {
            text("${my.totalCount} test${my.totalCount != 1 ? "s" : ""}")
            if (prevResult) {
                text("(${Functions.getDiffString(my.totalCount - prevResult.totalCount)})")
            }
        }
    }
}