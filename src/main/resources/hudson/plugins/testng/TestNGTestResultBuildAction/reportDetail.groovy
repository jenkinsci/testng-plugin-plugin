package hudson.plugins.testng.TestNGTestResultBuildAction

import hudson.Functions
import hudson.plugins.testng.util.FormatUtil

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

script(src: "${resURL}/plugin/testng-plugin/js/toggle_table.js")
script(src: "${resURL}/plugin/testng-plugin/js/toggle_mthd_summary.js")

h2("Failed Tests")

if (my.result.failCount != 0) {
    a(class: "testng-toggle-table", "data-toggle-table-id": "fail-tbl") {
        text("hide/expand the table")
    }
    table(id:"fail-tbl", border:"1px", class:"pane sortable") {
        thead() {
            tr() {
                th(class: "pane-header") {
                    text("Test Method")
                }
                th(class: "pane-header") {
                    text("Duration")
                }
            }
        }
        tbody() {
            for (failedTest in my.result.failedTests) {
                def failedTestSafeId = Functions.jsStringEscape(failedTest.id)
                def failedTestSafeUpUrl = Functions.jsStringEscape(failedTest.upUrl)
                tr() {
                    td(align: "left") {
                        a(id: "${failedTest.id}-showlink", class: "testng-show-stack-trace",
                                "data-failed-test-safe-id": "${failedTestSafeId}", "data-failed-test-safe-up-url": "${failedTestSafeUpUrl}/summary") {
                            text(">>>")
                        }
                        a(style: "display:none", id: "${failedTest.id}-hidelink", class: "testng-hide-stack-trace",
                                "data-failed-test-safe-id": "${failedTestSafeId}") {
                            text("<<<")
                        }
                        text(" ")
                        a(href:"${failedTest.upUrl}") {
                            text("${failedTest.parent.canonicalName}.${failedTest.name}")
                        }
                        div(id:"${failedTest.id}", style: "display:none", class: "hidden") {
                            text("Loading...")
                        }
                    }
                    td(align: "right") {
                        text("${FormatUtil.formatTime(failedTest.duration)}")
                    }
                }
            }
        }
    }
} else {
    text("No Test method failed")
}

if (my.result.failedConfigCount != 0) {
    h2("Failed Configuration Methods")
    printMethods("Configuration", "fail-config-tbl", my.result.failedConfigs, true)
}

if (my.result.skipCount != 0) {
    h2("Skipped Tests")
    printMethods("Test", "skip-tbl", my.result.skippedTests, false)
}

if (my.result.skippedConfigCount != 0) {
    h2("Skipped Configuration Methods")
    printMethods("Configuration", "skip-config-tbl", my.result.skippedConfigs, false)
}

h2("All Tests (grouped by their packages)")

a(class: "testng-toggle-table", "data-toggle-table-id": "all-tbl") {
    text("hide/expand the table")
}

table(id:"all-tbl", border:"1px", class:"pane sortable") {
    thead() {
        tr() {
            th(class:"pane-header") {
                text("Package")
            }
            th(class:"pane-header", style:"width:5em", title:"Duration") {
                text("Duration")
            }
            th(class:"pane-header", style:"width:5em", title:"Failed tests count") {
                text("Fail")
            }
            th(class:"pane-header", style:"width:5em", title:"Failed tests count diff") {
                text("(diff)")
            }
            th(class:"pane-header", style:"width:5em", title:"Skipped tests count") {
                text("Skip")
            }
            th(class:"pane-header", style:"width:5em", title:"Skipped tests count diff") {
                text("(diff)")
            }
            th(class:"pane-header", style:"width:5em", title:"Total tests count") {
                text("Total")
            }
            th(class:"pane-header", style:"width:5em", title:"Total tests count diff") {
                text("(diff)")
            }
//            th(class:"pane-header", style:"width:5em", title:"Package Age") {
//                text("Age")
//            }
        }
    }
    tbody () {
        for (pkg in my.result.packageMap.values()) {
            def prevPkg = pkg.previousResult
            tr() {
                td(align: "left") {
                    a(href:"${FormatUtil.escapeJS(pkg.name)}") { text("${pkg.name}") }
                }
                td(align: "center") {
                    text("${FormatUtil.formatTime(pkg.duration)}")
                }
                td(align: "center") {
                    text("${pkg.failCount}")
                }
                td(align: "center") {
                    text("${FormatUtil.formatLong(prevPkg == null ? 0 : pkg.failCount - prevPkg.failCount)}")
                }
                td(align: "center") {
                    text("${pkg.skipCount}")
                }
                td(align: "center") {
                    text("${FormatUtil.formatLong(prevPkg == null ? 0 : pkg.skipCount - prevPkg.skipCount)}")
                }
                td(align: "center") {
                    text("${pkg.totalCount}")
                }
                td(align: "center") {
                    text("${FormatUtil.formatLong(prevPkg == null ? 0 : pkg.totalCount - prevPkg.totalCount)}")
                }
//                td(align: "center") {
//                    text("${pkg.age}")
//                }
            }
        }
    }
}

/**
 * Prints out the tables containing information about methods executed during test
 *
 * @param type Description of the type of methods. Used as title of table
 * @param tableName unique name for the table
 * @param methodList list of methods that form the rows of the table
 * @param showMoreArrows if arrows should be shown with link to get more details about the methods
 * @return nothing
 */
def printMethods(type, tableName, methodList, showMoreArrows) {
    a(class: "testng-toggle-table", "data-toggle-table-id": "${tableName}") {
        text("hide/expand the table")
    }
    table(id:tableName, border:"1px", class:"pane sortable") {
        thead() {
            tr() {
                th(class: "pane-header") {
                    text("${type} Method")
                }
            }
        }
        tbody() {
            for (method in methodList) {
                def methodSafeId = Functions.jsStringEscape(method.id)
                def methodSafeUpUrl = Functions.jsStringEscape(method.upUrl)
                tr() {
                    td(align: "left") {
                        if (showMoreArrows) {
                            a(id: "${method.id}-showlink", class: "testng-show-stack-trace",
                                    "data-failed-test-safe-id": "${methodSafeId}", "data-failed-test-safe-up-url": "${methodSafeUpUrl}/summary") {
                                text(">>>")
                            }
                            a(style: "display:none", id: "${method.id}-hidelink", class: "testng-hide-stack-trace",
                                    "data-failed-test-safe-id": "${methodSafeId}") {
                                text("<<<")
                            }
                            text(" ")
                        }
                        a(href:"${method.upUrl}") {
                            text("${method.parent.canonicalName}.${method.name}")
                        }
                        if (showMoreArrows) {
                            div(id:"${method.id}", style: "display:none", class: "hidden") {
                                text("Loading...")
                            }
                        }
                    }
                }
            }
        }
    }
}