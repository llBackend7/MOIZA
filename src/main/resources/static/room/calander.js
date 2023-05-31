$(function() {
    $('#dialog').dialog({
        autoOpen: false, // 초기에 자동으로 열리지 않도록 설정
        modal: true, // 모달 다이얼로그로 설정
        resizable: true, // 크기 조정 허용
        open: function(event, ui) {
            // 다이얼로그 열릴 때 중앙 정렬
            $(this).dialog('widget').position({
                my: 'center',
                at: 'center',
                of: window
            });
        }
    });
});
function setDurationValue() {
    var hour = $(".hourSelect").val();
    var minute = $(".minuteSelect").val();

    if (hour !== "00" && hour !== "01" && hour !== "02" && hour !== "03" && hour !== "04" && hour !== "05" && hour !== "06" && hour !== "07" && hour !== "08" && hour !== "09" && hour !== "10" && hour !== "11" && hour !== "12") {
        alert("시간을 올바르게 선택해주세요.");
        return false;
    }

    if (minute !== "00" && minute !== "30") {
        alert("분을 올바르게 선택해주세요.");
        return false;
    }

    if (hour && minute) {
        $("#duration").val(hour + ":" + minute);
    }
}

$(document).ready(function () {
    setDurationValue();

    $(".hourSelect, .minuteSelect").change(function () {
        setDurationValue();
    });
});

$(document).ready(function () {
    $('form').on('submit', function (e) {
        e.preventDefault(); // 폼 제출의 기본 동작을 방지합니다.
        if (setDurationValue()) {
            return false;
        }

        let startDate = $('#startDate').val();
        let endDate = $('#endDate').val();
        let name = $('#name').val();
        let description = $('#description').val();
        let duration = $('#duration').val();
        let startTime = $('#availableStartTime').val();
        let endTime = $('#availableEndTime').val();
        let deadLine = $('#deadLine').val();

        if (validateTimes(startDate, endDate, name, description, duration, startTime, endTime, deadLine)) {
            $.ajax({
                type: $(this).attr('method'),
                url: $(this).attr('action'),
                data: $(this).serialize(),
                success: function (resp) {
                    $('form input, select, textarea').prop('disabled', true);
                    $('form button[type="submit"]').prop('disabled', true);

                    $('#response').attr('href',resp.link);
                    $('#dialog').dialog('open');
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.error(textStatus, errorThrown);
                }
            });
        }
    });
});

function validateTimes(startDate, endDate, name, description, duration, startTime, endTime, deadLine) {
    if (!validateTime(startTime) || !validateTime(endTime)) {
        alert("30분단위로 시각을 입력해주세요.");
        return false;
    }
    startDate = new Date(startDate);
    endDate = new Date(endDate);
    startTime = new Date(`1970-01-01T${startTime}:00`);
    endTime = new Date(`1970-01-01T${endTime}:00`);
    deadLine = new Date(deadLine);

    if (duration === '00:00' || !duration) {
        alert("모임 진행 시간을 올바르게 입력해 주세요.");
        return false;
    }
    if (endDate < startDate) {
        alert("시작날짜는 끝날짜보다 앞서야합니다.");
        return false;
    }
    if (endTime <= startTime) {
        alert("시작시간은 끝시간보다 앞서야 합니다.");
        return false;
    }
    if (new Date() > deadLine) {
        alert("마감시간은 현재보다 나중이어야 합니다.");
        return false;
    }
    if (startDate < deadLine) {
        alert("마감시간은 가능한 날짜보다 이전이어야 합니다.");
        return false;
    }
    return true;
}

$(document).ready(function () {
    for (var i = 0; i < 24; i++) {
        for (var j = 0; j <= 30; j += 30) {
            var hour = i < 10 ? '0' + i : i;
            var minute = j < 10 ? '0' + j : j;
            $('.time-30m-intervals').append(new Option(hour + ':' + minute, hour + ':' + minute));
        }
    }
});

function validateTime(time) {
    // 시간과 분을 추출
    var parts = time.split(":");
    var hour = parseInt(parts[0]);
    var minute = parseInt(parts[1]);

    // 유효성 검사
    if (hour < 0 || hour > 23 || minute % 30 !== 0) {
        return false;
    }

    return true;
}


