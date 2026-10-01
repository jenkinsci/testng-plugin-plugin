package hudson.plugins.testng.TestNGTestResultBuildAction

import hudson.plugins.testng.util.FormatUtil

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

// <l:run-subpage> (https://weekly.ci.jenkins.io/design-library/layouts/) is
// core's own wrapper for build-scoped subpages -- it renders its own
// <l:layout>, so it REPLACES the previous l.layout()+sidepanel.jelly pair
// rather than nesting inside it. With Jenkins' "new build page" experimental
// flag on, it renders the full-width tabbed layout with no job sidebar; with
// the flag off it falls back to the classic sidepanel.jelly layout on its
// own, so this is safe either way. It needs "it" (the object Stapler is
// dispatching this page for) to expose .object/.displayName -- already true
// here, since TestNGTestResultBuildAction extends AbstractTestResultAction,
// which extends jenkins.model.Tab.
l.run_subpage() {
    link(rel: "stylesheet", href: "${resURL}/plugin/testng-plugin/css/testng.css?v=${hudson.plugins.testng.PluginImpl.RESOURCE_VERSION}")

    def result = my.result
    def failCount = result.failCount
    def skipCount = result.skipCount
    def totalCount = result.totalCount
    def passCount = totalCount - failCount - skipCount

    // <l:app-bar> (core) replaces a bare <h1> with the same page-title row
    // the JUnit plugin's own "Tests" page uses (confirmed via its real
    // hudson.tasks.test.TestResult/index.jelly source): title + a result
    // count subtitle, with fail/skip/pass "jp-pill" counts and a duration
    // item in its controls slot. History/description controls from that
    // same source are left out -- not relevant to this build-scoped page.
    l.app_bar(title: my.displayName, subtitle: "${totalCount}") {
        div(class: "jenkins-details") {
            div(class: "jp-pills") {
                if (failCount > 0) {
                    div(class: "jp-pill jenkins-!-error-color", tooltip: "${failCount} failing") {
                        l.icon(src: "symbol-close")
                        text(" ${failCount}")
                    }
                }
                if (skipCount > 0) {
                    div(class: "jp-pill jenkins-!-skipped-color", tooltip: "${skipCount} skipped") {
                        l.icon(src: "symbol-warning")
                        text(" ${skipCount}")
                    }
                }
                if (passCount > 0) {
                    div(class: "jp-pill jenkins-!-success-color", tooltip: "${passCount} passing") {
                        l.icon(src: "symbol-check")
                        text(" ${passCount}")
                    }
                }
            }
            div(class: "jenkins-details__item", "aria-label": "Took ${FormatUtil.formatTime(result.duration)}") {
                div(class: "jenkins-details__item__icon") {
                    l.icon(src: "symbol-timer")
                }
                text("Took ${FormatUtil.formatTime(result.duration)}")
            }
        }
    }
    st.include(page: "bar.groovy")
    st.include(page: "reportDetail.groovy")
}
