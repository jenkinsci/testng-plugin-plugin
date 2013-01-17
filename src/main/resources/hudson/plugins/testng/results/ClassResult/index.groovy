package hudson.plugins.testng.results.ClassResult

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

l.layout(title: "Test Class - ${my.name}") {
    st.include(page: "sidepanel.jelly", it: my.owner)
    l.main_panel() {
        h1("Class ${my.name}")
        st.include(page: "reportDetail.groovy")
    }
}