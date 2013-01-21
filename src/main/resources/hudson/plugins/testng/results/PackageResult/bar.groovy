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

        div(id: "bar", style: "width:100%; height:1em; background-color: #729FCF") {
            def failpc = my.failCount * 100 / my.totalCount
            def skippc = my.skipCount * 100 / my.totalCount
            div(style: "width:${failpc}%; height: 1em; background-color: #EF2929; float: left")
            div(style: "width:${skippc}%; height: 1em; background-color: #FCE94F; float: left")
        }

        div(id: "pass", align: "right") {
            text("${my.totalCount} test${my.totalCount != 1 ? "s" : ""}")
            if (prevResult) {
                text("(${Functions.getDiffString(my.totalCount - prevResult.totalCount)})")
            }
        }
    }
}