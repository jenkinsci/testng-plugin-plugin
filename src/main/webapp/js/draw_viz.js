function resultsGraph(id) {

    var mockdata = {
        pass: [3, 4, 4, 6, 4, 5, 5, 5, 5],
        skip: [2, 2, 2, 2, 2, 2, 2, 2, 2],
        fail: [5, 6, 7, 7, 3, 3, 3, 3, 3]};

    var chart = c3.generate({
        bindto: '#' + id,
        data: {
            type: 'bar',
            json: mockdata,
            colors: {
                'pass': '#729FCF',
                'skip': '#FCE94F',
                'fail': '#EF2929'
            }
        }
    });
}