function resultsGraph(id, data) {

    //convert duration to readable format
    for(var i=data.duration.length-1; i>=0; i--) {
        data.duration[i] = msToTime(data.duration[i]);
        data.duration[i] = trimTime(data.duration[i]);
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

    var transformedData = [
        ['fail'].concat(data.fail.concat().reverse()),
        ['pass'].concat(data.pass.concat().reverse()),
        ['skip'].concat(data.skip.concat().reverse())
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
            width: 600
        },
        tooltip: {
            format: {
                title: function (d) {
                    return 'Build ' + data.buildNum[d] + ": " + data.duration.concat().reverse()[d];
                },
                value: function (name, id, index,value) {
                    return name;
                }
            }
        }
    });
}

