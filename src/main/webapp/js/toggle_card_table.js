// Toggles the collapsible tables rendered into l:card's "controls" slot
// (see collapsibleTable()/controlsHtml() in reportDetail.groovy).
(function () {
    "use strict";

    var CHEVRON_DOWN =
        '<svg class="icon-sm" xmlns="http://www.w3.org/2000/svg" aria-hidden="true" viewBox="0 0 512 512">' +
        '<path d="M112 184l144 144 144-144" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="48"/></svg>';
    var CHEVRON_UP =
        '<svg class="icon-sm" xmlns="http://www.w3.org/2000/svg" aria-hidden="true" viewBox="0 0 512 512">' +
        '<path d="M112 328l144-144 144 144" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="48"/></svg>';

    document.addEventListener("click", function (event) {
        var link = event.target.closest(".testng-card-toggle");
        if (!link) {
            return;
        }
        event.preventDefault();

        var targetId = link.getAttribute("data-toggle-target");
        var target = targetId ? document.getElementById(targetId) : null;
        if (!target) {
            return;
        }

        var collapsed = target.classList.toggle("testng-collapsed");
        var icon = link.querySelector(".testng-card-toggle-icon");
        if (icon) {
            icon.innerHTML = collapsed ? CHEVRON_DOWN : CHEVRON_UP;
        }
        link.setAttribute("tooltip", collapsed ? "Show table" : "Hide table");
    });
})();
