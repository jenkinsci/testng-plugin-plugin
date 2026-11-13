package hudson.plugins.testng.TestNGTestResultBuildAction

import hudson.Functions
import hudson.plugins.testng.util.FormatUtil

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

script(src: "${resURL}/plugin/testng-plugin/js/toggle_mthd_summary.js")
// Collapsible tables render into l:card's own "controls" slot.
script(src: "${resURL}/plugin/testng-plugin/js/toggle_card_table.js?v=${hudson.plugins.testng.PluginImpl.RESOURCE_VERSION}")

def chevronIcon(symbol, tooltip = "") {
    l.icon(class: "icon-sm", src: symbol, tooltip: tooltip)
}

// wrapperId must be unique per call site.
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

def stackTraceToggle(id, safeId, safeUpUrl) {
    a(href: "", id: "${id}-showlink",
            class: "testng-show-stack-trace jenkins-button jenkins-button--tertiary jenkins-button--icon jenkins-button--small",
            title: "Show stack trace",
            "data-failed-test-safe-id": "${safeId}", "data-failed-test-safe-up-url": "${safeUpUrl}/summary") {
        chevronIcon("symbol-chevron-down", "Show stack trace")
    }
    a(href: "", style: "display:none", id: "${id}-hidelink",
            class: "testng-hide-stack-trace jenkins-button jenkins-button--tertiary jenkins-button--icon jenkins-button--small",
            title: "Hide stack trace",
            "data-failed-test-safe-id": "${safeId}") {
        chevronIcon("symbol-chevron-up", "Hide stack trace")
    }
}

if (my.result.failCount != 0) {
    collapsibleTable("fail-tbl-wrap", "Failed Tests", "testng-card-failed-tests") {
            table(id: "fail-tbl", class: "sortable jenkins-table jenkins-!-margin-bottom-0") {
                thead() {
                    tr() {
                        th() {
                            text("Test Method")
                        }
                        th(align: "right") {
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
                                a(href: "${failedTest.upUrl}") {
                                    text("${failedTest.parent.canonicalName}.${failedTest.name}")
                                }
                                text(" ")
                                stackTraceToggle(failedTest.id, failedTestSafeId, failedTestSafeUpUrl)
                                div(id: "${failedTest.id}", style: "display:none", class: "hidden") {
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
    }
} else {
    l.card(title: "Failed Tests", id: "testng-card-failed-tests") {
        text("No Test method failed")
    }
}

if (my.result.failedConfigCount != 0) {
    printMethods("Configuration", "Failed Configuration Methods", "fail-config-tbl", my.result.failedConfigs, true)
}

if (my.result.skipCount != 0) {
    printMethods("Test", "Skipped Tests", "skip-tbl", my.result.skippedTests, false)
}

if (my.result.skippedConfigCount != 0) {
    printMethods("Configuration", "Skipped Configuration Methods", "skip-config-tbl", my.result.skippedConfigs, false)
}

collapsibleTable("all-tbl-wrap", "All Tests (grouped by their packages)", "testng-card-all-tests") {
    table(id: "all-tbl", class: "sortable jenkins-table jenkins-!-margin-bottom-0") {
        thead() {
            tr() {
                th() {
                    text("Package")
                }
                th(align: "center", style: "width:5em", title: "Duration") {
                    text("Duration")
                }
                th(align: "center", style: "width:5em", title: "Failed tests count") {
                    text("Fail")
                }
                th(align: "center", style: "width:5em", title: "Failed tests count diff") {
                    text("(diff)")
                }
                th(align: "center", style: "width:5em", title: "Skipped tests count") {
                    text("Skip")
                }
                th(align: "center", style: "width:5em", title: "Skipped tests count diff") {
                    text("(diff)")
                }
                th(align: "center", style: "width:5em", title: "Total tests count") {
                    text("Total")
                }
                th(align: "center", style: "width:5em", title: "Total tests count diff") {
                    text("(diff)")
                }
            }
        }
        tbody() {
            for (pkg in my.result.packageMap.values()) {
                def prevPkg = pkg.previousResult
                tr() {
                    td(align: "left") {
                        a(href: "${FormatUtil.escapeJS(pkg.name)}") { text("${pkg.name}") }
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
                }
            }
        }
    }
}

// Renders a card with a table of methods executed during test.
def printMethods(type, cardTitle, tableName, methodList, showMoreArrows) {
    collapsibleTable("${tableName}-wrap", cardTitle, "testng-card-${tableName}") {
            table(id: tableName, class: "sortable jenkins-table jenkins-!-margin-bottom-0") {
                thead() {
                    tr() {
                        th() {
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
                                a(href: "${method.upUrl}") {
                                    text("${method.parent.canonicalName}.${method.name}")
                                }
                                if (showMoreArrows) {
                                    text(" ")
                                    stackTraceToggle(method.id, methodSafeId, methodSafeUpUrl)
                                    div(id: "${method.id}", style: "display:none", class: "hidden") {
                                        text("Loading...")
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }
}
