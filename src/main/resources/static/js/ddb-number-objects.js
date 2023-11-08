$(document).ready(function () {
    $.ajax({
        url: "api/ddb-number-objects",
        timeout: 0,
        dataType: "json"
    }).done(function (data) {
        $('#ddb-number-objects').each(function () {
            $(this).prop('Counter', 0).animate({
                Counter: data
            }, {
                duration: 1000,
                easing: 'swing',
                step: function (now) {
                    $(this).text(Math.ceil(now).toLocaleString());
                }
            });
        });
    });
});