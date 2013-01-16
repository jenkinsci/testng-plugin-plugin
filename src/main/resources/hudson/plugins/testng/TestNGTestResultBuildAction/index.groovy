package hudson.plugins.testng.TestNGTestResultBuildAction

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

l.layout(title: "TestNG Report for Build #${my.owner.number}") {
    st.include(page: "sidepanel.jelly", it: my.owner)
    l.main_panel() {

        h1("${my.displayName}")
        st.include(page: "bar.groovy")
        st.include(page: "reportDetail.groovy")
    }
}