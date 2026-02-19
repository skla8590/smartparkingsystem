// 타이머
let timeLeft = 240; // 4분
let timerInterval = null;

function startTimer() {

    if (timerInterval) {
        clearInterval(timerInterval);
    }
    timeLeft = 240;

    const timerEl = document.getElementById("timer");

    timerInterval = setInterval(function () {

        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;

        timerEl.innerText =
            "남은 시간: " + minutes + ":" +
            (seconds < 10 ? "0" : "") + seconds;

        // 1분 이하 빨간색으로 변경
        if (timeLeft <= 60) {
            document.getElementById('timer').classList.remove('bg-info');
            document.getElementById('timer').classList.add('bg-danger');
        }

        if (timeLeft <= 0) {
            clearInterval(timerInterval);
            alert("인증 시간이 만료되었습니다.");
            document.getElementById('otpCode').disabled = true;
            document.getElementById('verifyBtn').disabled = true;
            document.getElementById('rtOTP').disabled = false;

            return;
        }
        timeLeft--;
    }, 1000);
}
// 팝업이라 그냥 밖에 빼놓음 전단계가 없기 때문
window.onload = startTimer;


// 이메일 팝업창
function verifyOTP(event) {
    event.preventDefault();

    const otpCode = document.getElementById("otpCode").value;

    if (otpCode.length !== 6) {
        alert('6자리 인증번호를 입력해주세요.')
        return;
    }

    fetch("/main/mypage", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: 'box=1&otpCode=' + otpCode
    })
        .then(res => {
            if (res.status === 200) {
                alert("[OTP Success] 인증 완료")
                // 인증이 완료되었을시 콜백 함수 실행
                if (window.opener && window.opener.onEmailVerified) {
                    window.opener.onEmailVerified();
                }
                window.close();
            } else if (res.status === 401) {
                alert("[OTP Fail] 인증번호가 일치 하지 않습니다.")
            } else if (res.status === 403) {
                alert("[OTP Expired] 이전페이지로 돌아가 재발송해주세요.")
            } else {
                alert("[ERROR] 알 수 없는 오류")
            }
        })
}

// 인증번호 재전송
function returnOTP() {
    if (confirm('인증번호 재전송하시겠습니까?')) {

        showLoading()
        fetch('/main/mypage', {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: 'box=returnOTP'
        })
            .then(res => {
                if (res.status === 200) {
                    alert("[OTP Resend] 인증번호가 재발송되었습니다.")
                    startTimer()
                    document.getElementById('rtOTP').disabled = true;
                    document.getElementById('otpCode').disabled = false;
                    document.getElementById('verifyBtn').disabled = false;
                    document.getElementById('timer').classList.remove('bg-danger');
                    document.getElementById('timer').classList.add('bg-info');
                } else {
                    alert('[ERROR] 재발송 실패')
                }
            })
            .finally(() => {
                hideLoading()
            })
    }
}