function resultsGraph(id, passCount, skipCount, failCount) {

    var chart = c3.generate({
        bindto: '#' + id,
        data: {
            type: 'pie',
            columns: [['pass', passCount],['fail', failCount],['skip', skipCount]],
            colors: {
                'fail': '#EF2929',
                'pass': '#729FCF',
                'skip': '#FCE94F'
            }
        },
        size: { width: 250, height: 250 }
    });
}

