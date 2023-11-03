$(document).ready(function () {
    $.getJSON("api/ddb-number-objects",
            function (data) {
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