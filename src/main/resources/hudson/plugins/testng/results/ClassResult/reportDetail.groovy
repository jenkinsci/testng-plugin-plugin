package hudson.plugins.testng.results.ClassResult

import hudson.plugins.testng.util.FormatUtil
import org.apache.commons.lang.StringUtils

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

script(src:"${app.rootUrl}/plugin/testng-plugin/js/show_more.js")

for (group in my.testRunMap.values()) {
    h2("Test Methods")
    if (group.testName) {
        text("(from test '")
        b("${group.testName}")
        text("' in suite '")
        b("${group.suiteName}')")
    }

    if (!group.testMethods.isEmpty()) {
        table(border:"1px", class:"pane sortable") {
            thead() {
                tr() {
                    th(class:"pane-header") {
                        text("Method")
                    }
                    th(class:"pane-header", style:"width:5em", title:"Duration") {
                        text("Duration")
                    }
                    th(class:"pane-header", style:"width:5em", title:"Start time") {
                        text("Start Time")
                    }
                    th(class:"pane-header", style:"width:5em", title:"Status") {
                        text("Status")
                    }
                }
            }
            tbody() {
                for(method in group.testMethods) {
                    tr() {
                        td(align:"left") {
                            a(href:"${method.upUrl}") {
                                text("${method.name}")
                            }
                            if (method.groups || method.testInstanceName) {
                                div(id:"${method.safeName}_1", style:"display:inline") {
                                    text("(")
                                    a(href:"javascript:showMore(\"${method.safeName}\")") {
                                        text("...")
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
                                }
                            }
                        }
                        td(align:"right") {
                            text("${FormatUtil.formatTime(method.duration)}")
                        }
                        td(align:"right") {
                            text("${method.startedAt}")
                        }
                        td(align:"center", class:"${method.cssClass}") {
                            text("${method.status}")
                        }
                    }
                }
            }
        }
    } else {
        text("No Test method was found in this class")
    }

    h2("Configuration Methods")

    if(group.configurationMethods) {
        table(border:"1px", class:"pane sortable") {
            thead() {
                tr() {
                    th(class:"pane-header") {
                        text("Method")
                    }
                    th(class:"pane-header", style:"width:5em", title:"Duration") {
                        text("Duration")
                    }
                    th(class:"pane-header", style:"width:5em", title:"Start time") {
                        text("Start Time")
                    }
                    th(class:"pane-header", style:"width:5em", title:"Status") {
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
                        td(align:"center", class:"${method.cssClass}") {
                            text("${method.status}")
                        }
                    }
                }
            }
        }
    } else {
        text("No Configuration method was found in this class")
    }
}
