package hudson.plugins.testng.TestNGProjectAction

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

if (from.graphActive) {
    link(rel: "stylesheet", href: "${resURL}/plugin/testng-plugin/css/testng.css?v=${hudson.plugins.testng.PluginImpl.RESOURCE_VERSION}")
    // Recolors the PNG to match the active theme; see theme_graph.js.
    script(src: "${resURL}/plugin/testng-plugin/js/theme_graph.js?v=${hudson.plugins.testng.PluginImpl.RESOURCE_VERSION}")
    div(class: "test-trend-caption") {
        text("${from.graphName}")
    }
    div(class: "testng-graph-frame") {
        // scale=2 for a retina-sharp PNG; width/height keep the on-screen size.
        img(class: "testng-theme-graph", lazymap: "${from.urlName}/graphMap", alt: "[Test result trend chart]",
                src: "${from.urlName}/graph?scale=2&v=${hudson.plugins.testng.PluginImpl.RESOURCE_VERSION}",
                width: "500", height: "200")
    }
}
