package hudson.plugins.testng.TestNGProjectAction

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

if (from.graphActive) {
    div(class: "test-trend-caption") {
        text("${from.graphName}")
    }
    img(lazymap: "${from.urlName}/graphMap", alt: "[Test result trend chart]", src: "${from.urlName}/graph")
}
