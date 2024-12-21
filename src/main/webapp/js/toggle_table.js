function toggleTable(id) {
    var element = document.getElementById(id);
    if (document.getElementById(id).style.display == "none") {
        document.getElementById(id).style.display = "";
    } else if (document.getElementById(id).style.display == "") {
        document.getElementById(id).style.display = "none";
    }
}

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".testng-toggle-table").forEach((toggle) => {
        toggle.addEventListener("click", (event) => {
            event.preventDefault();
            const { toggleTableId } = event.target.dataset;

            toggleTable(toggleTableId);
        });
    });
});
