//Loads data for all the methods
function showAllExecutedMethods() {
    var foo = <st:bind value="${it}"/>

    foo.getAllSortedTestMethodsByStartTime(function(t) {
        document.getElementById('sortedMethods').innerHTML = t.responseObject();
    })

    document.getElementById("showAllLink").style.display = "none";
}