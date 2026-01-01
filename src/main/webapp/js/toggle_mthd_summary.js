function showStackTrace(id, query) {
    var element = document.getElementById(id)
    element.style.display = "";
    document.getElementById(id + "-showlink").style.display = "none";
    document.getElementById(id + "-hidelink").style.display = "";

    var rqo = new XMLHttpRequest();
    rqo.open('GET', query, true);
    rqo.onreadystatechange = function() { element.innerHTML = rqo.responseText; }
    rqo.send(null);
}

function hideStackTrace(id) {
    document.getElementById(id).style.display = "none";
    document.getElementById(id + "-showlink").style.display = "";
    document.getElementById(id + "-hidelink").style.display = "none";
}

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".testng-show-stack-trace").forEach((button) => {
        button.addEventListener("click", (event) => {
            event.preventDefault();
            const { failedTestSafeId, failedTestSafeUpUrl } = event.target.closest(".testng-show-stack-trace").dataset;

            showStackTrace(failedTestSafeId, failedTestSafeUpUrl);
        });
    });

    document.querySelectorAll(".testng-hide-stack-trace").forEach((button) => {
        button.addEventListener("click", (event) => {
            event.preventDefault();
            const { failedTestSafeId } = event.target.closest(".testng-hide-stack-trace").dataset;

            hideStackTrace(failedTestSafeId);
        });
    });
});
