function showMore(id) {
    document.getElementById(id + "_1").style.display = "none";
    document.getElementById(id + "_2").style.display = "";
}

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".testng-show-more").forEach((button) => {
        button.addEventListener("click", (event) => {
            event.preventDefault();
            const { methodName } = event.target.dataset;

            showMore(methodName);
        })
    });
});
