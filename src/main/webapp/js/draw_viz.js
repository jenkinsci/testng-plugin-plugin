function resultsGraph(id, data) {

    console.log(data);
    console.log(data.buildNum[data.buildNum.length-1]);
    console.log(data.buildNum[0]);

    var transformedData = [
        ['buildNum'].concat(data.buildNum.reverse()),
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
            },
            x: "buildNum"
        },
        bar: {
            width: {
                ratio: 1 // this makes bar width 100% of length between ticks
            }
        },
        grid: { lines: {front: true}, x: {show: true}, y: {show: true}},
        size: {
            width: 600
        },
        tooltip: {
            format: {
                title: function (d) { return 'Build ' + d; },
                value: function (name, id, index) {
                    //num -- pass/fail/skip -- buildNum
                    var percent = data.
                    return name + " (" + percent + "%)";
                }
            }
        }
    });
}