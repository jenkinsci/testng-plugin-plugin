function resultsGraph(id, data) {

    console.log(data);

    var transformedData = [
        ['fail'].concat(data.fail.reverse()),
        ['pass'].concat(data.pass.reverse()),
        ['skip'].concat(data.skip.reverse())
        ];

    var chart = c3.generate({
        bindto: '#' + id,
        data: {
            groups:[['fail', 'pass', 'skip']],
            type: 'bar',
            columns: transformedData,
            colors: {
                'fail': '#EF2929',
                'pass': '#729FCF',
                'skip': '#FCE94F'
            },
            order: null,   // stack order by data definition.
            onclick: function (d, element) {
                var url = window.location.href;
                window.open(url.substring(0, url.length - 14) + d.x,"_self");
            }
        },
        bar: {
            width: {
                ratio: 1 // this makes bar width 100% of length between ticks
            }
        },
        size: {
            width: 600
        }
    });
}