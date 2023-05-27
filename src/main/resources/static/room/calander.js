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

$(document).ready(function() {
    $('form').on('submit', function(e) {
        e.preventDefault(); // 폼 제출의 기본 동작을 방지합니다.

        let  startDate = $('#startDate').val();
        let  endDate = $('#endDate').val();
        let  name = $('#name').val();
        let  description = $('#description').val();
        let  place = $('#place').val();
        let  duration = $('#duration').val();
        let  startTime = $('#startTime').val();
        let  endTime = $('#endTime').val();
        let  deadLine = $('#deadLine').val();

        validateTimes(startDate, endDate, name, description, place, duration, startTime, endTime, deadLine);

        // console.log('Start Date: ', startDate);
        // console.log('End Date: ', endDate);
        // console.log('Name: ', name);
        // console.log('Description: ', description);
        // console.log('Place: ', place);
        // console.log('Duration: ', duration);
        // console.log('Start Time: ', startTime);
        // console.log('End Time: ', endTime);
        // console.log('Dead Line: ', deadLine);
    });
});

function validateTimes(startDate, endDate, name, description, place, duration, startTime, endTime, deadLine) {
    startDate = new Date(startDate);
    endDate = new Date(endDate);
    startTime = new Date(`1970-01-01T${startTime}:00`);
    endTime = new Date(`1970-01-01T${endTime}:00`);
    deadLine = new Date(deadLine);

    if (endDate < startDate) {
        alert("가능날짜가 잘못되었습니다.");
        return false;
    }
    if (endTime < startTime) {
        alert("가능시간이 잘못되었습니다.");
        return false;
    }
    if (new Date() > deadLine) {
        alert("마감시간이 잘못되었습니다.");
        return false;
    }
    if (startDate < deadLine) {
        alert("마감시간은 가능한 날짜보다 이전이어야 합니다.");
        return false;
    }
    return true;
}
