function resultsGraph(id, data) {

    var transformedData = [
        ['fail'].concat(data[1]),
        ['pass'].concat(data[2]),
        ['skip'].concat(data[0])
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
            order: null   // stack order by data definition.
            //groups: ["pass", "fail", "skip"]
        },
        bar: {
            width: {
                ratio: 1 // this makes bar width 100% of length between ticks
            }
        },
        grid: { lines: {front: true}, x: {show: true}, y: {show: true}},
        size: {
          width: 600
        }
    });
}