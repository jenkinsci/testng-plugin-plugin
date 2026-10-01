package hudson.plugins.testng.results.PackageResult

st = namespace("jelly:stapler")

// Routes into fullPage.jelly, which wraps this page in core's l:run-subpage.
st.include(page: "fullPage.jelly", it: my)
