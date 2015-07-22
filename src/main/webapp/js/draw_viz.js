function resultsGraph(id, data) {

    var mockdata = [
        ['fail', 3, 4, 4, 6, 4, 5, 5, 5, 5],
        ['pass', 2, 2, 2, 2, 2, 2, 2, 2, 2],
        ['skip', 5, 6, 7, 7, 3, 3, 3, 3, 3]];

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
        size: {
          width: 600
        }
    });
}