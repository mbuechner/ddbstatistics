$(document).ready(function () {
    $.getJSON("api/dzp-number-issues",
            function (data) {
                $('#dzp-number-issues').each(function () {
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