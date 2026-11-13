package hudson.plugins.testng.TestNGTestResultBuildAction

import hudson.plugins.testng.util.TestResultHistoryUtil

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

//displayed on the build summary page

// Jenkins Symbol instead of the old "clipboard.png" bitmap.
t.summary(icon: "symbol-list") {
    a(href: "${my.urlName}") {
        text("${my.displayName}")
    }
    p() {
        raw("${TestResultHistoryUtil.toSummary(my)}")
    }
}