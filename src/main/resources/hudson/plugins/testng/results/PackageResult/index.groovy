package hudson.plugins.testng.results.PackageResult

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

link(rel: "stylesheet", href:"${app.rootUrl}plugin/testng-plugin/css/c3.min.css")

script(src:"${app.rootUrl}plugin/testng-plugin/js/d3.min.js")
script(src:"${app.rootUrl}plugin/testng-plugin/js/c3.min.js")
script(src:"${app.rootUrl}plugin/testng-plugin/js/draw_pie.js")

l.layout(title: "Test Packages") {
    st.include(page: "sidepanel.jelly", it: my.owner)
    l.main_panel() {

        h1("Package ${my.displayName}")
        st.include(page: "pie.groovy")
        st.include(page: "reportDetail.groovy")
    }
}