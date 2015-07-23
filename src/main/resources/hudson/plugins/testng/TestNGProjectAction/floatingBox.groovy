package hudson.plugins.testng.TestNGProjectAction

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

link(rel: "stylesheet", href:"${app.rootUrl}/plugin/testng-plugin/css/c3.min.css")

script(src:"${app.rootUrl}/plugin/testng-plugin/js/d3.min.js")
script(src:"${app.rootUrl}/plugin/testng-plugin/js/c3.min.js")
script(src:"${app.rootUrl}/plugin/testng-plugin/js/draw_results.js")

if (from.graphActive) {
    div(class: "test-trend-caption") {
        text("${from.graphName}")
    }

    div(id: "chart")
}

script() {
    text("var data = ${from.chartJson};")
    text("\nresultsGraph('chart', data);")
}
