$(document).ready(function () {
    $.getJSON("api/ddb-number-objects-digitalisat",
            function (data) {
                $('#ddb-number-objects-digitalisat').each(function () {
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