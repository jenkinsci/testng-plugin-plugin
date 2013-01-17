package hudson.plugins.testng.results.MethodResult

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

l.layout(title: "Test method - ${my.name}") {
    st.include(page: "sidepanel.jelly", it: my.owner)
    l.main_panel() {
        st.include(page: "reportDetail.groovy")
    }
}