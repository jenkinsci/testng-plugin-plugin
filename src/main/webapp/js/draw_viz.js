function resultsGraph(id, data) {

    var transformedData = [
        ['pass'].concat(data[0]),
        ['fail'].concat(data[1]),
        ['skip'].concat(data[2])];

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
            }
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