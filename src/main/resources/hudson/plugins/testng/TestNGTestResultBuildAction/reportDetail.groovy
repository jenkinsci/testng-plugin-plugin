package hudson.plugins.testng.TestNGTestResultBuildAction

import hudson.plugins.testng.util.FormatUtil

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

script(src: "${app.rootUrl}/plugin/testng-plugin/js/toggle_table.js")
script(src: "${app.rootUrl}/plugin/testng-plugin/js/toggle_mthd_summary.js")

h2("Failed Tests")

if (my.result.failCount != 0) {
    a(href: "javascript:toggleTable('fail-tbl')") {
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
                tr() {
                    td(align: "left") {
                        a(id: "${failedTest.id}-showlink", href:"javascript:showStackTrace('${failedTest.id}', '${failedTest.upUrl}/summary')") {
                            text(">>>")
                        }
                        a(style: "display:none", id: "${failedTest.id}-hidelink", href:"javascript:hideStackTrace('${failedTest.id}')") {
                            text("<<<")
                        }
                        text(" ")
                        a(href:"${failedTest.upUrl}") {
                            text("${failedTest.parent.name}.${failedTest.name}")
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

a(href:"javascript:toggleTable('all-tbl')") {
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
                    a(href:"${pkg.name}") { text("${pkg.name}") }
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
    a(href: "javascript:toggleTable('${tableName}')") {
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
                tr() {
                    td(align: "left") {
                        if (showMoreArrows) {
                            a(id: "${method.id}-showlink", href:"javascript:showStackTrace('${method.id}', '${method.upUrl}/summary')") {
                                text(">>>")
                            }
                            a(style: "display:none", id: "${method.id}-hidelink", href:"javascript:hideStackTrace('${method.id}')") {
                                text("<<<")
                            }
                            text(" ")
                        }
                        a(href:"${method.upUrl}") {
                            text("${method.parent.name}.${method.name}")
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