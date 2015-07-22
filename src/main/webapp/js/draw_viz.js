function resultsGraph(id, data) {

//    var data = {"mockdata" : [
//                    ['fail', 3, 4, 4, 6, 4, 5, 5, 5, 5, 2],
//                    ['pass', 2, 2, 2, 2, 2, 2, 2, 2, 2, 7],
//                    ['skip', 5, 6, 7, 7, 3, 3, 3, 3, 3, 0]
//                ],
//                "buildNumbers" : [7,8,9,10,11,12,13,14,15,16],
//                "buildTimes" : ["time stamp here"], //display on mousover
//                "urlBase" : "http://localhost:8080/jenkins/job/test_job_freestyle_project/"
//    };
    var transformedData = [
        ['pass'].concat(data[0]),
        ['fail'].concat(data[1]),
        ['skip'].concat(data[2])
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
                window.open(data.urlBase + d.x,"_self")
                alert("Go to new page here!!!");
                console.log(d);
                console.log(element);
            },
            onmouseover: function (d) {

            }
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