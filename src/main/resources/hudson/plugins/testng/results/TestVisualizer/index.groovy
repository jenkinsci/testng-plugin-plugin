package hudson.plugins.testng.results.TestVisualizer

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

l.layout(title: "Test Visualizer") {
    st.include(page: "sidepanel.jelly", it: my.owner)
    l.main_panel() {

        h1("Visualizer")
    }
}