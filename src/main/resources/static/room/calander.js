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
        toastWarning("시간을 올바르게 선택해주세요.");
        return false;
    }

    if (minute !== "00" && minute !== "30") {
        toastWarning("분을 올바르게 선택해주세요.");
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

        return validateTimes(startDate, endDate, name, description, duration, startTime, endTime, deadLine);
    });
});

function validateTimes(startDate, endDate, name, description, duration, startTime, endTime, deadLine) {
    if (!validateTime(startTime) || !validateTime(endTime)) {
        toastWarning("30분단위로 시각을 입력해주세요.");
        return false;
    }
    startDate = new Date(startDate);
    endDate = new Date(endDate);
    startTime = new Date(`1970-01-01T${startTime}:00`);
    endTime = new Date(`1970-01-01T${endTime}:00`);
    deadLine = new Date(deadLine);

    const [durationHours, durationMinutes] = duration.split(':');
    const durationInMinutes = parseInt(durationHours) * 60 + parseInt(durationMinutes);
    const differenceInMinutes = (endTime - startTime) / (1000 * 60); // 시간 차이를 분 단위로 계산
    const startDateTime = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate(), startTime.getHours(), startTime.getMinutes());

    console.log(startDateTime)
    console.log(startDateTime < deadLine)

    if (duration === '00:00' || !duration) {
        toastWarning("모임 진행 시간을 올바르게 입력해 주세요.");
        return false;
    }
    if (differenceInMinutes < durationInMinutes) {
        toastWarning("시작시간과 끝시간의 간격이 모임 진행 시간보다 커야 합니다.");
        return false;
    }
    if (endDate < startDate) {
        toastWarning("시작날짜는 끝날짜보다 앞서야합니다.");
        return false;
    }
    if (endTime <= startTime) {
        toastWarning("시작시간은 끝시간보다 앞서야 합니다.");
        return false;
    }
    if (new Date() > deadLine) {
        toastWarning("마감시간은 현재보다 나중이어야 합니다.");
        return false;
    }
    if (startDateTime < deadLine) {
        toastWarning("마감시간은 가능한 날짜보다 이전이어야 합니다.");
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

function increaseVoteCount(button) {
    var voteCountElement = button.parentElement.previousElementSibling.querySelector('.vote-count');
    var currentCount = parseInt(voteCountElement.textContent);
    voteCountElement.textContent = currentCount + 1;
}

toastr.options = {
    closeButton: true,
    debug: false,
    newestOnTop: true,
    progressBar: true,
    positionClass: "toast-top-right",
    preventDuplicates: false,
    onclick: null,
    showDuration: "300",
    hideDuration: "1000",
    timeOut: "5000",
    extendedTimeOut: "1000",
    showEasing: "swing",
    hideEasing: "linear",
    showMethod: "fadeIn",
    hideMethod: "fadeOut"
};

function parseMsg(msg) {
    const [pureMsg, ttl] = msg.split(";ttl=");

    const currentJsUnixTimestamp = new Date().getTime();

    if (ttl && parseInt(ttl) + 5000 < currentJsUnixTimestamp) {
        return [pureMsg, false];
    }

    return [pureMsg, true];
}

function toastWarning(msg) {
    const [pureMsg, needToShow] = parseMsg(msg);

    if (needToShow) {
        toastr["warning"](pureMsg, "경고");
    }
}
