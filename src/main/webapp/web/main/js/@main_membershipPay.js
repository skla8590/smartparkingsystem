const membershipPayModalElement = document.getElementById('membershipPayModal');
const membershipPayModal = new bootstrap.Modal(membershipPayModalElement);
const btnMembershipPayRemote = document.getElementById('btnMembershipPay');
const btnMembershipSubmit = document.getElementById('btn-membership-submit');

// // 목업데이터 추후 삭제
// const mockMembers = [
//     {carNum: "12가3456", name: "홍길동", phone: "010-1111-2222"},
//     {carNum: "99나9999", name: "김철수", phone: "010-9999-8888"}
// ];

// '회원권 결제 버튼' 클릭
btnMembershipPayRemote.addEventListener('click', () => {
    if (typeof modal !== 'undefined') modal.hide();

    if (!window.currentCard) {
        alert("선택된 차량 정보가 없습니다.");
        return;
    }

    document.getElementById('mem-carNum').value = window.currentCard.dataset.carNum;
    document.getElementById('mem-name').value = "";
    document.getElementById('mem-phone').value = "";

    const now = new Date();
    const future = new Date();
    future.setDate(now.getDate() + 30);
    document.getElementById('mem-startDate').value = now.toISOString().split('T')[0];
    document.getElementById('mem-endDate').value = future.toISOString().split('T')[0];

    membershipPayModal.show();
});

// 기존 회원 여부 확인 버튼 클릭
document.getElementById('btn-check-member').addEventListener('click', () => {
    const carNum = document.getElementById('mem-carNum').value.trim();

    // let member;
    // for (let i = 0; i < mockMembers.length; i++) {
    //     if (mockMembers[i].carNum === carNum) {
    //         member = mockMembers[i];
    //         break;
    //     }
    // }
    // if (member) {
    //     alert('등록된 회원 정보가 있습니다. 기존 정보로 입력을 진행합니다.')
    //     document.getElementById('mem-name').value = member.name;
    //     document.getElementById('mem-phone').value = member.phone;
    // } else {
    //     alert("등록된 정보가 없습니다. 신규 회원 정보를 입력해주세요.");
    //     document.getElementById('mem-name').focus();
    // }

    axios.get('/member_check', {
        params: { carNum: carNum }
    })
        .then(response => {
            const data = response.data;

            if (!data.success) {
                alert(data.message || "조회 중 오류가 발생했습니다.");
                return;
            }

            if (data.exists) {
                // 등록된 회원
                alert('등록된 회원 정보가 있습니다. 기존 정보로 입력을 진행합니다.');
                document.getElementById('mem-name').value = data.name;
                document.getElementById('mem-phone').value = data.phone;
            } else {
                // 신규 회원
                alert("등록된 정보가 없습니다. 신규 회원 정보를 입력해주세요.");
                document.getElementById('mem-name').value = "";
                document.getElementById('mem-phone').value = "";
                document.getElementById('mem-name').focus();
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('회원 조회 중 오류가 발생했습니다.');
        });
});

// 결제하기 버튼 로직
btnMembershipSubmit.addEventListener('click', () => {
    const name = document.getElementById('mem-name').value;
    const phone = document.getElementById('mem-phone').value;
    const carNum = document.getElementById('mem-carNum').value;
    const start = document.getElementById('mem-startDate').value;
    const end = document.getElementById('mem-endDate').value;
    const price = document.getElementById('mem-price').value;

    if (!name || !phone) {
        alert("회원 정보를 모두 입력해 주세요.");
        return;
    }

    const parkNo = window.currentCard.dataset.parkNo;

    fetch('/parking/exit', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'}, // 데이터 형식 지정
        body: `parkNo=${parkNo}`
    })
        .then(res => res.json()) // 서버의 응답을 JSON으로 변환
        .then(data => { // 서버가 보낸 응답 객체
            if (!data.success) {
                alert('출차 처리 실패' + data.message);
                return;
            }
            // 1. 데이터 매핑
            document.getElementById('res-car').innerText = carNum;
            document.getElementById('res-user').innerText = name + " / " + phone;
            document.getElementById('res-period').innerText = start + " ~ " + end;
            document.getElementById('res-price').innerText = price;

            // 2. 화면 전환
            document.getElementById('mem-input-section').style.display = 'none';
            document.getElementById('mem-receipt-section').style.display = 'block';
            document.getElementById('mem-footer').style.display = 'none';

            alert("결제가 완료되었습니다!");
        })
});

// 페이지 로드 후 이벤트 등록 (맨 아래 추가)
membershipPayModalElement.addEventListener('click', function (e) {
    if (e.target?.id === 'btn-receipt-close-final') {

        const card = window.currentCard;

        if (card) {
            card.dataset.status = 'available';
            card.dataset.carNum = "";
            card.dataset.inFullTime = "";
            card.classList.replace('occupied', 'available');
            card.querySelector('.box-car').innerText = "사용 가능";
            card.querySelector('.box-time').innerText = "";

            if (typeof updateParkingCount === 'function') updateParkingCount();

            membershipPayModal.hide();
            alert('회원권 결제 및 출차가 완료되었습니다.');
        }

    }
});

// 모달 리셋 로직
membershipPayModalElement.addEventListener('hidden.bs.modal', () => {
    document.getElementById('mem-input-section').style.display = 'block';
    document.getElementById('mem-receipt-section').style.display = 'none';
    document.getElementById('mem-footer').style.display = 'flex';
});