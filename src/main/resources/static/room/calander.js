$(document).ready(function () {

    $('.input-daterange').datepicker({
        format: 'yyyy-mm-dd',
        autoclose: true
    });

});


$(document).ready(function() {
    for (var i = 0; i < 24; i++) {
        for (var j = 0; j <= 30; j+=30) {
            var hour = i < 10 ? '0' + i : i;
            var minute = j < 10 ? '0' + j : j;
            $('.time-30m-intervals').append(new Option(hour + ':' + minute, hour + ':' + minute));
        }
    }
});
