package hudson.plugins.testng.TestNGTestResultBuildAction

import hudson.plugins.testng.util.TestResultHistoryUtil

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

//displayed on the build summary page

/*
    TODO: ${my.iconFileName} doesn't work here though it used to
    work just fine in .jelly.
    Even though we are specifying url starting with /... it's
    parsed as a relative url
 */
t.summary(icon: "clipboard.png") {
    a(href: "${my.urlName}") {
        text("${my.displayName}")
    }
    p() {
        raw("${TestResultHistoryUtil.toSummary(my)}")
    }
}