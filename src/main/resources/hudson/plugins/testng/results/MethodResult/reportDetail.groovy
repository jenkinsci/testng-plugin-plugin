package hudson.plugins.testng.results.MethodResult

import hudson.plugins.testng.TestNGProjectAction
import hudson.plugins.testng.util.FormatUtil
import org.apache.commons.lang.StringUtils

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

def testngProjAction = my.run.project.getAction(TestNGProjectAction.class)

div(id: "report") {
    h1("${my.name}")
    text("(from ")
    a(href: "${my.parent.upUrl}", id: "parent") {
        text("${my.parent.canonicalName}")
    }
    text(" took ${my.durationString})")

    span(class: "${my.cssClass}", id: "status") {
        h1("${my.status}")
    }

    div(id: "description") {
        //descriptions by default are escaped in testng result XML
        //if we are not dealing with HTML content, just replace \n by <br/> to make contents more readable
        if (my.description) {
            raw("${testngProjAction==null || testngProjAction.escapeTestDescp ? my.annotate(my.description) : my.description.replace("\n", "<br/>")}")
        }
    }

    if (my.testInstanceName) {
        div(id: "inst-name") {
            text("Instance Name: ${my.testInstanceName}")
        }
    }

    if (my.parentTestName) {
        div(id: "parent-test-name") {
            text("Test Name: ${my.parentTestName}")
        }
    }

    if (my.parentSuiteName) {
        div(id: "parent-suite-name") {
            text("Suite Name: ${my.parentSuiteName}")
        }
    }

    if (my.groups) {
        div(id: "groups") {
            p("Group(s): ${StringUtils.join(my.groups, ", ")}")
        }
    }

    if (my.parameters?.size() > 0) {
        table(border: "1px", class: "pane", id: "params", style: "white-space:normal") {
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
    img(id: "trend", src: "graph", lazymap: "graphMap", alt: "[Method Execution Trend Chart]")

    if (my.reporterOutput) {
        div(id: "reporter-output") {
            h3("Reporter Output")
            code(style: "white-space:pre; margin-left:15px; display:block; border:1px black; background-color:#F0F0F0") {
                raw("${my.reporterOutput}")
            }
        }
    }

    if (my.exception) {
        h3() {
            text("Exception ")
            i("${my.exception.exceptionName}")
        }
        p(id:"exp-msg") {
            b("Message: ")
            if (my.exception.message) {
                raw("${testngProjAction == null || testngProjAction.escapeExceptionMsg ? my.annotate(my.exception.message) : my.exception.message.replace("\n", "<br/>")}")
            } else {
                text("(none)")
            }
        }
        if (my.exception.stackTrace) {
            b("Stacktrace:")
            br()
            br()
            pre(id:"exp-st", my.exception.stackTrace)
        }
    }
}
