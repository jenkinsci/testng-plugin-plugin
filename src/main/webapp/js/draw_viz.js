function resultsGraph(id) {

    var mockdata = [
        ['fail', 3, 4, 4, 6, 4, 5, 5, 5, 5, 2, 1],
        ['pass', 2, 2, 2, 2, 2, 2, 2, 2, 2, 7, 5],
        ['skip', 5, 6, 7, 7, 3, 3, 3, 3, 3, 0, 4]];

    var chart = c3.generate({
        bindto: '#' + id,
        data: {
            groups:[['fail', 'pass', 'skip']],
            type: 'bar',
            columns: mockdata,
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