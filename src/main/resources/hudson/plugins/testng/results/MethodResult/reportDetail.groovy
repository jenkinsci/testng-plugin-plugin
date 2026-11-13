package hudson.plugins.testng.results.MethodResult

import hudson.plugins.testng.TestNGProjectAction
import hudson.plugins.testng.util.FormatUtil
import org.apache.commons.lang3.StringUtils

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

def testngProjAction = my.run.project.getAction(TestNGProjectAction.class)

// Recolors the Execution Trend PNG below to match the active Jenkins theme; see theme_graph.js.
script(src: "${resURL}/plugin/testng-plugin/js/theme_graph.js?v=${hudson.plugins.testng.PluginImpl.RESOURCE_VERSION}")

div(id: "report") {
    // Same app-bar/jp-pills header as the build report page, single status pill instead of counts.
    def status = my.status
    def statusColor = status?.equalsIgnoreCase("pass") ? "jenkins-!-success-color"
            : status?.equalsIgnoreCase("skip") ? "jenkins-!-skipped-color" : "jenkins-!-error-color"
    def statusIcon = status?.equalsIgnoreCase("pass") ? "symbol-check"
            : status?.equalsIgnoreCase("skip") ? "symbol-warning" : "symbol-close"

    l.app_bar(title: my.name, subtitle: my.parent.canonicalName) {
        div(class: "jenkins-details") {
            div(class: "jp-pills") {
                div(class: "jp-pill ${statusColor}", tooltip: "${status}", id: "status") {
                    l.icon(src: statusIcon)
                    text(" ${status}")
                }
            }
            div(class: "jenkins-details__item", "aria-label": "Took ${my.durationString}") {
                div(class: "jenkins-details__item__icon") {
                    l.icon(src: "symbol-timer")
                }
                text("Took ${my.durationString}")
            }
            a(href: "${my.parent.upUrl}", id: "parent", class: "jenkins-details__item") {
                text("${my.parent.canonicalName}")
            }
        }
    }

    l.card(title: "Details", id: "testng-card-details") {
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
    }

    if (my.parameters?.size() > 0) {
        l.card(title: "Parameters", id: "testng-card-parameters") {
            table(class: "jenkins-table jenkins-!-margin-bottom-0", id: "params", style: "white-space:normal") {
                thead() {
                    tr() {
                        th(style: "width:6.5em;")
                        th(title: "parameter value") {
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
    }

    l.card(title: "Execution Trend", id: "testng-card-trend") {
        div(class: "testng-graph-frame") {
            // scale=2 for a retina-sharp PNG; width/height keep the on-screen size unchanged.
            img(id: "trend", class: "testng-theme-graph",
                    src: "graph?scale=2&v=${hudson.plugins.testng.PluginImpl.RESOURCE_VERSION}",
                    lazymap: "graphMap", alt: "[Method Execution Trend Chart]",
                    width: "800", height: "150")
        }
    }

    if (my.reporterOutput) {
        l.card(title: "Reporter Output", id: "testng-card-reporter-output") {
            code(class: "testng-code-block") {
                raw("${my.reporterOutput}")
            }
        }
    }

    if (my.exception) {
        // Same card/pre/code + copy-button pattern as summary.jelly's expanded row.
        l.card(title: "Exception ${my.exception.exceptionName}", id: "testng-card-exception") {
            if (my.exception.message) {
                div(class: "testng-stacktrace-card") {
                    details(open: "true") {
                        summary() {
                            text("Message")
                            l.copyButton(text: my.exception.message, iconOnly: "true", tooltip: "Copy message")
                        }
                        pre(id: "exp-msg", class: "testng-code-block") {
                            raw("${testngProjAction == null || testngProjAction.escapeExceptionMsg ? my.annotate(my.exception.message) : my.exception.message.replace("\n", "<br/>")}")
                        }
                    }
                }
            }
            if (my.exception.stackTrace) {
                div(class: "testng-stacktrace-card") {
                    details(open: "true") {
                        summary() {
                            text("Stacktrace")
                            l.copyButton(text: my.exception.stackTrace, iconOnly: "true", tooltip: "Copy stacktrace")
                        }
                        pre(id: "exp-st", class: "testng-code-block", my.exception.stackTrace)
                    }
                }
            }
        }
    }
}
