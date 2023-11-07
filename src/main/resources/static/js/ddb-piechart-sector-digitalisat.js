$(document).ready(function () {
    Chart.defaults.font.family = 'Nunito', '-apple-system,system-ui,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif';
    Chart.defaults.font.family.color = '#858796';

    var ddbPiechartSectorDigitalisat = new Chart($('#ddb-piechart-sector-digitalisat'), {
        type: 'pie',
        data: {
            labels: [],
            datasets: [{
                    data: [],
                    label: 'DDB-Objekte',
                    backgroundColor: ['#4e73df', '#858796', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#f8f9fc', '#5a5c69'],
                    hoverBorderColor: "rgba(234, 236, 244, 1)"
                }]
        },
        options: {
            maintainAspectRatio: false,
            responsive: true,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });

    $.ajax({
        url: "api/ddb-piechart-sector-digitalisat",
        timeout: 0
    }).done(function (data) {
        keys = [];
        values = [];
        for (var property in data) {
            keys.push(property);
            values.push(data[property]);

        }
        ddbPiechartSectorDigitalisat.data.labels = keys;
        ddbPiechartSectorDigitalisat.data.datasets[0].data = values;
        ddbPiechartSectorDigitalisat.update();
    });
});