$(document).ready(function () {
    $.getJSON("api/dzp-number-pages",
            function (data) {
                $('#dzp-number-pages').each(function () {
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