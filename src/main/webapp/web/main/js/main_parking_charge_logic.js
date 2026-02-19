/* 주차 시간(분) 통한 요금 계산 함수 */
function calculateBaseFeeOnly(minutes, policy) {
    if (minutes <= policy.freeTime) return 0;
    if (minutes <= policy.basicTime) return policy.basicCharge;
    const extraMinutes = minutes - policy.basicTime;
    const extraCharge = Math.ceil(extraMinutes / policy.extraTime) * policy.extraCharge;
    return policy.basicCharge + extraCharge;
}

/* 입차 날짜, 출차 날짜, 차종 통한 최종 요금 계산 */
function calculateParkingCharge(inDateStr, outDateStr, carType) {
    // 1. 요금 정책
    // *** 추후 DB(payment_info)에서 끌고 와야 함!!! ***
    // *** freeTime, maxCharge는 테이블에 없는데 있는 게 좋을지?
    // *** 설정 관리에 없는 옵션이라 테이블에서 뺀 건데 추후 생길 걸 예상하고 테이블에 넣어두어야 할지
    const policy = {
        freeTime: 10, // ***
        basicTime: 60,
        extraTime: 30,
        basicCharge: 2000,
        extraCharge: 1000,
        smallCarDiscount: 0.3,
        disabledDiscount: 0.5,
        maxCharge: 15000 // ***
    };

    // 2. 날짜 객체 생성
    const inDate = new Date(inDateStr);
    const outDate = new Date(outDateStr)

    // 날짜 계산용 순수 날짜 변수
    const startDate = new Date(inDate.getFullYear(), inDate.getMonth(), inDate.getDate());
    const endDate = new Date(outDate.getFullYear(), outDate.getMonth(), outDate.getDate());

    // 날짜 차이 계산 (0 당일, 1 하루, 2 이틀, ...)
    const diffDays = Math.floor((endDate - startDate) / 86400000);

    let preTotal = 0; // 총 요금
    let base = 0;  // 기본 요금
    let extra = 0; // 추가 요금

    // 3. 상황별 요금 계산
    if (diffDays === 0) { // 주차 기간이 하루 이내
        const diffMis = Math.floor((outDate - inDate) / 60000);
        preTotal = calculateBaseFeeOnly(diffMis, policy);
        if (preTotal > policy.maxCharge) {
            preTotal = policy.maxCharge;
        }
    } else { // 자정 넘긴 장기 주차
        // 1) 첫날 요금
        const day1Time = 1440 - (inDate.getHours() * 60 + inDate.getMinutes()); // 전날 주차한 시간
        let day1Charge = Math.min(calculateBaseFeeOnly(day1Time, policy), policy.maxCharge);
        preTotal += day1Charge;

        // 2) 중간 요금
        if (diffDays > 1) {
            preTotal += (diffDays - 1) * policy.maxCharge;
        }

        // 3) 마지막날 요금
        const lastDayTime = outDate.getHours() * 60 + outDate.getMinutes();
        let lastDayCharge = Math.min(calculateBaseFeeOnly(lastDayTime, policy), policy.maxCharge)
        preTotal += lastDayCharge;
    }

    // 영수증 표시용
    base = preTotal === 0 ? 0 : policy.basicCharge;
    extra = Math.max(0, preTotal - base);

    // 3. 할인 혜택 적용
    let discount = 0;
    let discountName = "";
    if (carType === "월정액") {
        discount = preTotal; // 100% 할인
        discountName = "월정액 회원 할인 (100%)"
    } else if (carType === "장애인") {
        discount = preTotal * policy.disabledDiscount;
        discountName = "장애인 할인 (" + policy.disabledDiscount * 100 + "%)";
    } else if (carType === "경차") {
        discount = preTotal * policy.smallCarDiscount;
        discountName = "경차 할인 (" + policy.smallCarDiscount * 100 + "%)";
    }
    return {
        total: preTotal - discount,
        base: base,
        extra: extra,
        discount: discount,
        discountName: discountName,
        duration: Math.floor((outDate - inDate) / 60000)};
}