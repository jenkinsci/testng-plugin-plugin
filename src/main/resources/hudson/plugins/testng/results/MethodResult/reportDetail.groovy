package hudson.plugins.testng.results.MethodResult

import hudson.plugins.testng.TestNGProjectAction
import hudson.plugins.testng.util.FormatUtil

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

def testngProjAction = my.owner.project.getAction(TestNGProjectAction.class)

div() {
    h1("${my.name}")
    text("(from ")
    a(href: "${my.parent.upUrl}") {
        text("${my.parent.name}")
    }
    text(" took ${my.durationString})")

    span(class: "${my.cssClass}") {
        h1("${my.status}")
    }

    div(id: "description") {
        if (testngProjAction.escapeTestDescp) {
            text("${FormatUtil.escapeString(my.description)}")
        }
    }

    if (my.groups) {
        text("Group(s): ${my.displayGroups}")
    }

    if (my.parameters?.size() > 0) {
        table(border: "1px", class: "pane") {
            thead() {
                tr() {
                    th(class: "pane-header", style: "width:5em;")
                    th(class: "pane-header", title: "parameter value") {
                        text("Value")
                    }
                }
            }
            tbody() {
                def count = 1
                for (param in my.parameters) {
                    tr() {
                        td(align: "left") {
                            text("Parameter #${count++}")
                        }
                        td(align: "left") {
                            text("${param}")
                        }
                    }
                }
            }
        }
    }

    br()
    br()
    img(src: "graph", lazymap: "graphMap", alt: "[Method Execution Trend Chart]")

    if (my.reporterOutput) {
        div() {
            h3("Reporter Output")
            code(style: "margin-left:15px; display:block; border:1px black; background-color:#F0F0F0; width:800px") {
                text("${my.reporterOutput}")
            }
        }
    }

    if (my.exception) {
        h3() {
            text("Exception ")
            i("${my.exception.exceptionName}")
        }
        p() {
            b("Message: ")
            if (my.exception.message) {
                if (testngProjAction.escapeExceptionMsg) {
                    text("${FormatUtil.escapeString(my.exception.message)}")
                }
            } else {
                text("(none)")
            }
        }
        if (my.exception.stackTrace) {
            b("Stacktrace:")
            p() {
                text(raw("${FormatUtil.formatStackTraceForHTML(my.exception.stackTrace)}"))
            }
        }
    }
}
