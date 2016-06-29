package hudson.plugins.testng.results.PackageResult

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

l.layout(title: "Test Packages") {
    st.include(page: "sidepanel.jelly", it: my.run)
    l.main_panel() {

        h1("Package ${my.displayName}")
        st.include(page: "bar.groovy")
        st.include(page: "reportDetail.groovy")
    }
}