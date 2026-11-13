// Recolors the server-rendered trend PNGs (class "testng-theme-graph") for
// dark mode via GraphHelper.java's "graphBg"/"plotBg"/"fg"/"grid" params.
(function () {
    "use strict";

    var DARK_BG = "1b1b1b";
    var DARK_FG = "c9c9c9";

    function isDarkTheme() {
        var theme = document.documentElement.getAttribute("data-theme") || "";
        if (theme.indexOf("dark") !== -1) {
            return true;
        }
        if (theme.indexOf("light") !== -1) {
            return false;
        }
        return !!(window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches);
    }

    function applyThemeColors() {
        var dark = isDarkTheme();
        document.querySelectorAll("img.testng-theme-graph").forEach(function (img) {
            var url;
            try {
                url = new URL(img.src, window.location.href);
            } catch (e) {
                return;
            }
            if (dark) {
                url.searchParams.set("graphBg", DARK_BG);
                url.searchParams.set("plotBg", DARK_BG);
                url.searchParams.set("fg", DARK_FG);
                url.searchParams.set("grid", DARK_FG);
            } else {
                url.searchParams.delete("graphBg");
                url.searchParams.delete("plotBg");
                url.searchParams.delete("fg");
                url.searchParams.delete("grid");
            }
            var next = url.toString();
            if (img.src !== next) {
                img.src = next;
            }
        });
    }

    document.addEventListener("DOMContentLoaded", applyThemeColors);
    if (window.matchMedia) {
        window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", applyThemeColors);
    }
    new MutationObserver(applyThemeColors).observe(document.documentElement, {
        attributes: true,
        attributeFilter: ["data-theme"],
    });
})();
