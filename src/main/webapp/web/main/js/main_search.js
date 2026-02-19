// 검색 함수
function searchCar() {
    const inputCarNum = document.getElementById('inputCarNum');
    const searchCarNum = inputCarNum.value.trim();

    if (searchCarNum.length !== 4) { // 차량번호 4자리가 입력되지 않았다면
        alert('차량번호 뒤 4자리를 정확히 입력하세요.')
        inputCarNum.focus();
        return;
    }

    // 주차 구역(A1 ~ A20) 전체 다 가져오기
    const parkingSpots = document.querySelectorAll('.parking-card');
    let isFound = false;

    parkingSpots.forEach(spot => {
        const parkedCarNum = spot.dataset.carNum;
        if (parkedCarNum && parkedCarNum.endsWith(searchCarNum)) {
            spot.click();
            isFound = true;
        }
    });

    if (!isFound) {
        alert('해당 번호의 차량이 주차되어있지 않습니다.')
    }
}

// 검색 버튼 클릭하면 searchCar 함수 실행
document.getElementById('btnCarSearch').addEventListener('click', searchCar);

// 차량 번호 입력 후 엔터 쳐도 searchCar 함수 실행
document.getElementById('inputCarNum').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        e.preventDefault();
        document.getElementById('btnCarSearch').click();
    }
})