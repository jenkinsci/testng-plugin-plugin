package hudson.plugins.testng.results.ClassResult

import hudson.Functions
import hudson.plugins.testng.util.FormatUtil
import org.apache.commons.lang3.StringUtils

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

script(src:"${app.rootUrl}/plugin/testng-plugin/js/show_more.js")

def i = 0
for (group in my.testRunMap.values()) {
    def runInfo = group.testName ? " — ${group.testName} / ${group.suiteName}" : ""
    div(id: "run-${i++}") {
        // Each section below is rendered as its own Design Library <l:card>
        // (https://weekly.ci.jenkins.io/design-library/cards/) instead of a
        // bare <h2> heading.
        l.card(title: "Test Methods${runInfo}") {
            if (!group.testMethods.isEmpty()) {
                table(id: "test", class:"sortable jenkins-table jenkins-!-margin-bottom-0") {
                    thead() {
                        tr() {
                            th() {
                                text("Method")
                            }
                            th(align:"right", style:"width:5em", title:"Duration") {
                                text("Duration")
                            }
                            th(align:"right", style:"width:15em", title:"Start time") {
                                text("Start Time")
                            }
                            th(align:"center", style:"width:5em", title:"Status") {
                                text("Status")
                            }
                        }
                    }
                    tbody() {
                        for(method in group.testMethods) {
                            def methodJsSafeName = Functions.jsStringEscape(method.safeName)
                            tr() {
                                td(align:"left") {
                                    a(href:"${method.upUrl}") {
                                        text("${method.name}")
                                    }
                                    if (method.groups || method.testInstanceName || method.parameters?.size() > 0) {
                                        div(id:"${method.safeName}_1", style:"display:inline") {
                                            text(" (")
                                            a(href: "", class: "testng-show-more jenkins-button jenkins-button--tertiary jenkins-button--small",
                                                    "data-method-name": "${methodJsSafeName}") {
                                                l.icon(class: "icon-sm", src: "symbol-expand", tooltip: "Show more")
                                            }
                                            text(")")
                                        }
                                        div(id:"${method.safeName}_2", style:"display:none") {
                                            if (method.testInstanceName) {
                                                div() {
                                                    text("Instance Name: ${method.testInstanceName}")
                                                }
                                            }
                                            if (method.groups) {
                                                div() {
                                                    text("Group(s): ${StringUtils.join(method.groups, ", ")}")
                                                }
                                            }
                                            if (method.parameters?.size() > 0) {
                                                div(style: "white-space:normal") {
                                                    text("Parameter(s): ${StringUtils.join(method.parameters, ", ")}")
                                                }
                                            }
                                        }
                                    }
                                }
                                td(align:"right") {
                                    text("${FormatUtil.formatTime(method.duration)}")
                                }
                                td(align:"right") {
                                    text("${method.startedAt}")
                                }
                                td(align:"center") {
                                    span(class:"${method.cssClass} jenkins-table__badge") {
                                        text("${method.status}")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                text("No Test method was found in this class")
            }
        }

        l.card(title: "Configuration Methods${runInfo}") {
            if(group.configurationMethods) {
                table(id:"config", class:"sortable jenkins-table jenkins-!-margin-bottom-0") {
                    thead() {
                        tr() {
                            th() {
                                text("Method")
                            }
                            th(align:"right", style:"width:5em", title:"Duration") {
                                text("Duration")
                            }
                            th(align:"right", style:"width:15em", title:"Start time") {
                                text("Start Time")
                            }
                            th(align:"center", style:"width:5em", title:"Status") {
                                text("Status")
                            }
                        }
                    }
                    tbody() {
                        for(method in group.configurationMethods) {
                            tr() {
                                td(align:"left") {
                                    a(href:"${method.upUrl}") {
                                        text("${method.name}")
                                    }
                                }
                                td(align:"right") {
                                    text("${FormatUtil.formatTime(method.duration)}")
                                }
                                td(align:"right") {
                                    text("${method.startedAt}")
                                }
                                td(align:"center") {
                                    span(class:"${method.cssClass} jenkins-table__badge") {
                                        text("${method.status}")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                text("No Configuration method was found in this class")
            }
        }
    }
}
