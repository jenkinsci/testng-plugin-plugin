function resultsGraph(id, data) {

    console.log(data);

    var transformedData = [
        ['x'].concat(data.buildNum.reverse()),
        ['duration'].concat(data.duration.reverse())
        ];

    var chart = c3.generate({
        bindto: '#' + id,
        data: {
            x: "x",
            groups:[['duration']],
            type: 'bar',
            columns: transformedData,
            colors: {
                'duration': '#2196F3',
            },
            color: function (color, d) {
                // d will be 'id' when called for legends
                if(d.id) {
                    console.log(d)
                    if(data.status[d.index] == "FAIL"){
                        return d3.rgb("#FF5252");
                    }
                }
                return color;
            },
            order: null,   // stack order by data definition.
            onclick: function (d, element) {
                var url = window.location.href;
                window.open(url.substring(0, url.length - 14) + d.x,"_self");
            }
        },
        axis: {
            y: {
                label: "Duration"
            }
        },
        bar: {
            width: {
                ratio: .75 // this makes bar width 100% of length between ticks
            }
        },
        grid: { lines: {front: true}, x: {show: true}, y: {show: true}},
        size: {
            width: 600
        }
    });
}