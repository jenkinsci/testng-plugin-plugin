function resultsGraph(id, data) {
    //convert duration to readable format
    for(var i=data.duration.length-1; i>=0; i--) {
        data.duration[i] = msToTime(data.duration[i]);
        data.duration[i] = trimTime(data.duration[i]);
    }

    //convert status color to status string
    for(var i=data.buildStatus.length-1; i>=0; i--) {
        if(data.buildStatus[i]=="RED"){
            data.buildStatus[i] = "FAIL";
        } else if(data.buildStatus[i]=="YELLOW") {
            data.buildStatus[i] = "UNSTABLE";
        } else if(data.buildStatus[i]=="BLUE"){
            data.buildStatus[i] = "SUCCESS";
        }
    }

    function msToTime(duration) {
        var milliseconds = parseInt((duration%1000)/100)
            , seconds = parseInt((duration/1000)%60)
            , minutes = parseInt((duration/(1000*60))%60)
            , hours = parseInt((duration/(1000*60*60))%24);

        hours = (hours < 10) ? "0" + hours : hours;
        minutes = (minutes < 10) ? "0" + minutes : minutes;
        seconds = (seconds < 10) ? "0" + seconds : seconds;

        return hours + "h" + minutes + "m" + seconds + "." + milliseconds + "s";
    }

    function trimTime(timeString) {
        while(timeString.substring(0,3) == "00h" || timeString.substring(0,3) == "00m" || timeString.substring(0,3) == "00s"){
            timeString = timeString.substring(3);
        }
        while(timeString.substring(0,1) == "0"){
                    timeString = timeString.substring(1);
                }
        return timeString;
    }

    data.buildNum.reverse();
    data.buildStatus.reverse();
    data.duration.reverse();

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
            order: null,
            selection: { grouped: true },
            onclick: function (d, element) {
                var url = window.location.href.replace('/testngreports/','');
                window.open(url + '/' + data.buildNum[d.index],"_self");
            }
        },
        axis: { x: { type: 'category', categories: data.buildNum}},
        grid: { lines: {front: true}, x: {show: true}, y: {show: true}},
        size: {
            width: 800
        },
        tooltip: {
            format: {
                title: function (d) {
                    return 'Build ' + data.buildNum[d] + ": " + data.buildStatus[d];
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
                for (i = 0; i < d.length; i++) {
                    if (! (d[i] && (d[i].value || d[i].value === 0))) { continue; }
                        if (! text) {
                            title = titleFormat ? titleFormat(d[i].x) : d[i].x;
                            text = "<table class='" + $$.CLASS.tooltip + "'>" + (title || title === 0 ? "<tr><th colspan='2'>" + title + "</th></tr>" : "");
                        }
                        name = nameFormat(d[i].name);
                        value = valueFormat(d[i].value, d[i].ratio, d[i].id, d[i].index);
                        bgcolor = $$.levelColor ? $$.levelColor(d[i].value) : color(d[i].id);

                        text += "<tr class='" + $$.CLASS.tooltipName + "-" + d[i].id + "'>";
                        text += "<td class='name'><span style='background-color:" + bgcolor + "'></span>" + name + "</td>";
                        text += "<td class='value'>" + value + "</td>";
                        text += "</tr>";
                }
                text += "<tr class='" + $$.CLASS.tooltipName + "-" + "final" + "'>";
                text += "<td class='name' colspan=2>" + "Duration: " + data.duration[d[0].index] + "</td>";
                text += "</tr>";
                return text + "</table>";
            }
        }
    });
}

