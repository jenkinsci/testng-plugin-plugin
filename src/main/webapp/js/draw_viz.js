function resultsGraph(id, data) {

    var transformedData = [
        ['fail'].concat(data.fail.reverse()),
        ['pass'].concat(data.pass.reverse()),
        ['skip'].concat(data.skip.reverse())
        ];

    var chart = c3.generate({
        bindto: '#' + id,
        data: {
            groups:[['fail', 'pass', 'skip']],
            type: 'area',
            columns: transformedData,
            colors: {
                'fail': '#EF2929',
                'pass': '#729FCF',
                'skip': '#FCE94F'
            },
            onclick: function (d, element) {
                var url = window.location.href;
                window.open(url.substring(0, url.length - 14) + data.buildNum[d.x],"_self");
            }
        },
        axis: { x: { type: 'category', categories: data.buildNum.reverse()}},
        grid: { lines: {front: true}, x: {show: true}, y: {show: true}},
        size: {
            width: 600
        },
        tooltip: {
            format: {
                title: function (d) {
                    return 'Build ' + data.buildNum[d];
                },
                value: function (name, id, index,value) {
                    return name;
                }
            }
        }
    });
}