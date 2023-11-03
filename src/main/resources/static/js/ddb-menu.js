$(document).ready(function () {
    var link = window.location.href.substring(window.location.href.lastIndexOf('/') + 1);
    if (link.indexOf('?') > 0) {
        link = link.substring(0, link.indexOf('?'));
    }
    const aLink = $('a[href="' + link + '"]');
    aLink.addClass("active");

    if (link.indexOf('-') > -1) {
        abschnitt = link.substring(0, link.indexOf('-'));
        $('#' + abschnitt + '-abschnitt').addClass("show");
    } else if (link === 'index.html') {
        $('#ddb-abschnitt').addClass("show");
    }
});
