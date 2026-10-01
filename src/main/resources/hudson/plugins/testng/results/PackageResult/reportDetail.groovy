package hudson.plugins.testng.results.PackageResult

import hudson.plugins.testng.util.FormatUtil

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

//see https://issues.jenkins-ci.org/browse/JENKINS-18867 & https://issues.jenkins-ci.org/browse/JENKINS-18875
st.bind(var:"thisPkgResult", value: my)
st.adjunct(includes: "hudson.plugins.testng.results.PackageResult.report-detail")

// Same collapsible-table pattern as TestNGTestResultBuildAction/reportDetail.groovy (duplicated per-file).
script(src: "${resURL}/plugin/testng-plugin/js/toggle_card_table.js?v=${hudson.plugins.testng.PluginImpl.RESOURCE_VERSION}")

def controlsHtml(wrapperId) {
    def chevronUpSvg = '<svg class="icon-sm" xmlns="http://www.w3.org/2000/svg" aria-hidden="true" viewBox="0 0 512 512"><path d="M112 328l144-144 144 144" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="48"/></svg>'
    return "<a href='#' class='testng-card-toggle jenkins-card__reveal' data-toggle-target='${wrapperId}' tooltip='Hide table'><span class='testng-card-toggle-icon'>${chevronUpSvg}</span></a>"
}

def collapsibleTable(wrapperId, cardTitle, cardId, tableBuilder) {
    l.card(title: cardTitle, id: cardId, controls: controlsHtml(wrapperId)) {
        div(id: wrapperId) {
            tableBuilder()
        }
    }
}

collapsibleTable("all-classes-wrap", "All Classes", "testng-card-all-classes") {
    table(id:"allClasses", class:"sortable jenkins-table jenkins-!-margin-bottom-0") {
        thead() {
            tr() {
                th() {
                    text("Class")
                }
                th(align:"center", style:"width:5em", title:"Duration") {
                    text("Duration")
                }
                th(align:"center", style:"width:5em", title:"Failed Test Count") {
                    text("Fail")
                }
                th(align:"center", style:"width:5em", title:"Failed Test Count Diff") {
                    text("(diff)")
                }
                th(align:"center", style:"width:5em", title:"Skip Test Count") {
                    text("Skip")
                }
                th(align:"center", style:"width:5em", title:"Skip Test Count Diff") {
                    text("(diff)")
                }
                th(align:"center", style:"width:5em", title:"Total Test Count") {
                    text("Total")
                }
                th(align:"center", style:"width:5em", title:"Total Test Count Diff") {
                    text("(diff)")
                }
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
                }
            }
        }
    }
}

if (my.sortedTestMethodsByStartTime) {
    collapsibleTable("exec-tbl-wrap", "Order of Execution by Test Method", "testng-card-exec-order") {
        if (my.sortedTestMethodsByStartTime.size() > my.MAX_EXEC_MTHD_LIST_SIZE) {
            div(id:"showAllLink") {
                p() {
                    text("Showing only first ${my.MAX_EXEC_MTHD_LIST_SIZE} test methods. ")
                    a(href: "", class: "testng-show-all-exec-methods jenkins-button jenkins-button--tertiary jenkins-button--small") {
                        text("Click to see all")
                    }
                }
            }
        }
        table(class:"sortable jenkins-table jenkins-!-margin-bottom-0", id:"exec-tbl") {
            thead() {
                tr() {
                    th(title:"Method") {
                        text("Method")
                    }
                    th(title:"Description") {
                        text("Description")
                    }
                    th(align:"center", style:"width:5em", title:"Duration") {
                        text("Duration")
                    }
                    th(align:"center", style:"width:15em", title:"Start Time") {
                        text("Start Time")
                    }
                    th(align:"center", style:"width:5em", title:"Status") {
                        text("Status")
                    }
                }
            }
            tbody(id:"sortedMethods") {
                //updated via ajax
            }
        }
    }
} else {
    l.card(title: "Order of Execution by Test Method", id: "testng-card-exec-order") {
        text("No Tests found or all Tests were skipped")
    }
}
