package hudson.plugins.testng.results.PackageResult

import hudson.plugins.testng.util.FormatUtil

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

script(src:"${resURL}/plugin/testng-plugin/js/toggle_table.js")
//see https://issues.jenkins-ci.org/browse/JENKINS-18867 & https://issues.jenkins-ci.org/browse/JENKINS-18875
st.bind(var:"thisPkgResult", value: my)
st.adjunct(includes: "hudson.plugins.testng.results.PackageResult.report-detail")

h2("All Classes")
a(class: "testng-toggle-table", "data-toggle-table-id": "allClasses") {
    text("hide/expand the table")
}

table(id:"allClasses", border:"1px", class:"pane sortable") {
    thead() {
        tr() {
            th(class:"pane-header") {
                text("Class")
            }
            th(class:"pane-header", style:"width:5em", title:"Duration") {
                text("Duration")
            }
            th(class:"pane-header", style:"width:5em", title:"Failed Test Count") {
                text("Fail")
            }
            th(class:"pane-header", style:"width:5em", title:"Failed Test Count Diff") {
                text("(diff)")
            }
            th(class:"pane-header", style:"width:5em", title:"Skip Test Count") {
                text("Skip")
            }
            th(class:"pane-header", style:"width:5em", title:"Skip Test Count Diff") {
                text("(diff)")
            }
            th(class:"pane-header", style:"width:5em", title:"Total Test Count") {
                text("Total")
            }
            th(class:"pane-header", style:"width:5em", title:"Total Test Count Diff") {
                text("(diff)")
            }
//            th(class:"pane-header", style:"width:5em", title:"Age") {
//                text("Age")
//            }
        }
    }
    tbody() {
        for (clazz in my.children) {
            def prevClazz = clazz.previousResult
            tr() {
                td(align:"left") {
                    a(href:"${clazz.upUrl}") {
                        text("${clazz.name}")
                    }
                }
                td(align:"center") {
                    text("${FormatUtil.formatTime(clazz.duration)}")
                }
                td(align:"center") {
                    text("${clazz.failCount}")
                }
                td(align:"center") {
                    text("${FormatUtil.formatLong(prevClazz == null ? 0 : clazz.failCount - prevClazz.failCount)}")
                }
                td(align: "center") {
                    text("${clazz.skipCount}")
                }
                td(align: "center") {
                    text("${FormatUtil.formatLong(prevClazz == null ? 0 : clazz.skipCount - prevClazz.skipCount)}")
                }
                td(align:"center") {
                    text("${clazz.totalCount}")
                }
                td(align: "center") {
                    text("${FormatUtil.formatLong(prevClazz == null ? 0 : clazz.totalCount - prevClazz.totalCount)}")
                }
//                td(align: "center") {
//                    text("${clazz.age}")
//                }
            }
        }
    }
}

h2("Order of Execution by Test Method")
if (my.sortedTestMethodsByStartTime) {
    if (my.sortedTestMethodsByStartTime.size() > my.MAX_EXEC_MTHD_LIST_SIZE) {
        div(id:"showAllLink") {
            p() {
                text("Showing only first ${my.MAX_EXEC_MTHD_LIST_SIZE} test methods. ")
                a(class: "testng-show-all-exec-methods") {
                    text("Click to see all")
                }
            }
        }
    }
    a(class: "testng-toggle-table", "data-toggle-table-id": "exec-tbl") {
        text("hide/expand the table")
    }
    table(border:"1px", class:"pane sortable", id:"exec-tbl") {
        thead() {
            tr() {
                th(class:"pane-header", title:"Method") {
                    text("Method")
                }
                th(class:"pane-header", title:"Description") {
                    text("Description")
                }
                th(class:"pane-header", style:"width:5em", title:"Duration") {
                    text("Duration")
                }
                th(class:"pane-header", style:"width:15em", title:"Start Time") {
                    text("Start Time")
                }
                th(class:"pane-header", style:"width:5em", title:"Status") {
                    text("Status")
                }
            }
        }
        tbody(id:"sortedMethods") {
            //updated via ajax
        }
    }
} else {
    div("No Tests found or all Tests were skipped")
}
