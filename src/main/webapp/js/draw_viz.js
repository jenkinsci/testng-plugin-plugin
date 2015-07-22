function resultsGraph(id) {

    var mockdata = [
        ['pass', 3, 4, 4, 6, 4, 5, 5, 5, 5],
        ['fail', 2, 2, 2, 2, 2, 2, 2, 2, 2],
        ['skip', 5, 6, 7, 7, 3, 3, 3, 3, 3]];

    var chart = c3.generate({
        bindto: '#' + id,
        data: {
            groups:[['pass', 'fail', 'skip']],
            type: 'bar',
            columns: mockdata,
            colors: {
                'pass': '#729FCF',
                'skip': '#FCE94F',
                'fail': '#EF2929'
            }
            //groups: ["pass", "fail", "skip"]
        }
    });
}