function resultsGraph(id, data) {

    console.log(data);

    data.buildNum.reverse();
    data.status.reverse();
    var transformedData = [
        ['duration'].concat(data.duration.reverse())
        ];

    var currentBuildIndex = 0;
    for(var i=0; i<data.buildNum.length; i++){
        if(data.buildNum[i] == data.currentBuild){
            currentBuildIndex = i - 0.5;
            console.log("setting current build index to " + i);
            break;
        }
    }

    var chart = c3.generate({
        bindto: '#' + id,
        data: {
            groups:[['duration']],
            type: 'bar',
            columns: transformedData,
            colors: {
                'duration': '#2196F3',
            },
            color: function (color, d) {
                // d will be 'id' when called for legends
                if(d.id) {
                    if(data.status[d.index] == "FAIL"){
                        return d3.rgb("#FF5252");
                    } else if(data.status[d.index] == "SKIP"){
                        return d3.rgb("#FFC107");
                    }
                }
                return color;
            },
            selection: { grouped: true },
            onclick: function (d, element) {
                var url = window.location.href;
                var newUrl = url.replace(/\/[^\/]*\/testngreports/gi,'/' + data.buildNum[d.index] + '/testngreports');
                window.open(newUrl,"_self");
            }
        },
        axis: {
            y: { label: "Duration (Seconds)" },
            x: { type: 'category', categories: data.buildNum }
        },
        bar: {
            width: {
                ratio: .75 // this makes bar width 100% of length between ticks
            }
        },
        grid: { x: {show: true}, y: {show: true}},
        size: {
            width: 800
        },
        legend: {
            show: false
        },
        tooltip: {
            format: {
                title: function (d) {
                    return 'Build ' + data.buildNum[d];
                },
                value: function (name, id, index, value) {
                    return name;
                }
            },
            contents: function (d, defaultTitleFormat, defaultValueFormat, color) {
                var $$ = this, config = $$.config,
                      titleFormat = config.tooltip_format_title || defaultTitleFormat,
                      nameFormat = config.tooltip_format_name || function (name) { return name; },
                      valueFormat = config.tooltip_format_value || defaultValueFormat,
                      text, i, title, value, name, bgcolor;
                title = "Build " + data.buildNum[d[0].index];
                text = "<table class='" + $$.CLASS.tooltip + "'>" + (title || title === 0 ? "<tr><th colspan='2'>" + title + "</th></tr>" : "");
                text += "<tr class='" + $$.CLASS.tooltipName + "-" + "final" + "'>";
                text += "<td class='name' colspan=2>" + "Duration: " + data.duration[d[0].index] + "s</td>";
                text += "</tr>";
                return text + "</table>";
            }
        },
        regions: [
          {axis: 'x', start: currentBuildIndex, end: currentBuildIndex+1, class: 'chartHighlight'}
        ]
    });
}