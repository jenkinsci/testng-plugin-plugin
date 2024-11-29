// Loads data for all the methods
function showAllExecMthds() {
    thisPkgResult.getAllSortedTestMethodsByStartTime(function (t) {
        document.getElementById("sortedMethods").innerHTML = t.responseObject();
    });
    document.getElementById("showAllLink").style.display = "none";
}

document.addEventListener("DOMContentLoaded", () => {
    // following script loads the initial table data
    thisPkgResult.getFirstXSortedTestMethodsByStartTime(function(t) {
        document.getElementById('sortedMethods').innerHTML = t.responseObject();
    });

    const showAllButton = document.querySelector(".testng-show-all-exec-methods");
    if (showAllButton !== null) {
        showAllButton.addEventListener("click", () => {
            showAllExecMthds();
        });
    }
});
