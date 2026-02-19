    // ========================================
    // 전역
    // ========================================

    let currentChartType = 'monthly_sales';

    // 상세 조건 선택시 바로 그래프 바뀌게
    let chartLoadTimer = null;

    function debouncedLoadChart() {
        clearTimeout(chartLoadTimer);
        chartLoadTimer = setTimeout(loadChart, 300); // 300ms 후 실행
    }

    // 그래프 링크 삭제
    Highcharts.setOptions({
        credits: { enabled: false }
    });


    // ========================================
    // 필터 초기화
    // ========================================
    function initFilters() {
        const now = new Date();
        const currentYear = now.getFullYear();
        const currentMonth = now.getMonth() + 1;

        const yearSelect = document.getElementById('year');
        const monthSelect = document.getElementById('month'); // ← 추가

        yearSelect.innerHTML = '';
        for (let y = 2024; y <= currentYear; y++) {
            const opt = document.createElement('option');
            opt.value = y;
            opt.textContent = y + '년';
            if (y === currentYear) opt.selected = true;
            yearSelect.appendChild(opt);
        }

        buildMonthOptions(currentYear, currentMonth);

        yearSelect.addEventListener('change', function () {
            const selectedYear = parseInt(this.value);
            const limitMonth = selectedYear === currentYear ? currentMonth : 12;
            buildMonthOptions(selectedYear, limitMonth);
            debouncedLoadChart();
        });

        monthSelect.addEventListener('change', debouncedLoadChart);

        document.getElementById('includeMembership').addEventListener('change', debouncedLoadChart);

        document.getElementById('chartType').addEventListener('change', function () {
            updateFilterVisibility(this.value);
            loadChart();
        });

        updateFilterVisibility(document.getElementById('chartType').value);
    }

    function buildMonthOptions(selectedYear, limitMonth) {
        const now = new Date();
        const currentYear = now.getFullYear();
        const currentMonth = now.getMonth() + 1;

        const monthSelect = document.getElementById('month');
        const prevMonth = parseInt(monthSelect.value) || currentMonth;

        monthSelect.innerHTML = '';

        const allOpt = document.createElement('option');
        allOpt.value = 'all';
        allOpt.textContent = '전체';
        // 과거 연도면 '전체' 기본 선택
        if (selectedYear < currentYear) allOpt.selected = true;
        monthSelect.appendChild(allOpt);

        for (let m = 1; m <= limitMonth; m++) {
            const opt = document.createElement('option');
            opt.value = m;
            opt.textContent = m + '월';
            // 현재 연도일 때만 이전 선택 월 유지 (없으면 현재 월)
            if (selectedYear === currentYear && m === (prevMonth <= limitMonth ? prevMonth : limitMonth)) {
                opt.selected = true;
            }
            monthSelect.appendChild(opt);
        }
    }

    // 통계 유형별 필터 표시/숨김
    function updateFilterVisibility(chartType) {
        const dateFilterRow = document.getElementById('dateFilterRow');
        const membershipLabel = document.getElementById('membershipLabel');
        const searchBtn = document.getElementById('searchBtn');
        const yearSelect = document.getElementById('year');
        const monthSelect = document.getElementById('month');

        // 회원 통계: 필터 전체 숨김 (조회 버튼 포함)
        if (chartType === 'member_stats') {
            dateFilterRow.style.display = 'none';
            return;
        }

        dateFilterRow.style.display = '';
        yearSelect.style.display = '';
        monthSelect.style.display = '';
        searchBtn.style.display = '';

        // 회원권 매출 포함: 매출 유형에만 표시
        const showMembership = chartType === 'monthly_sales' || chartType === 'cumulative_sales';
        membershipLabel.style.display = showMembership ? '' : 'none';
    }

    // ========================================
    // 페이지 로드 시 초기화
    // ========================================
    window.onload = function () {
        initFilters();
        loadChart();
    };

    // ========================================
    // 차트 로드 메인 함수
    // ========================================
    function loadChart() {
    const chartType = document.getElementById('chartType').value;
    const year = parseInt(document.getElementById('year').value);
    const month = document.getElementById('month').value;
    const includeMembership = document.getElementById('includeMembership').checked;

    console.log('차트 로드:', {chartType, year, month, includeMembership});

    // 로딩 표시
    showLoading();

    // 차트 타입별 API 호출
    switch(chartType) {
    case 'monthly_sales':
    loadMonthlySales(year, month, includeMembership);
    break;
    case 'cumulative_sales':
    loadCumulativeSales(year, month, includeMembership);
    break;
    case 'car_type_pie':
    loadCarTypePie(year, month);
    break;
    case 'peak_time':
    loadPeakTime(year, month);
    break;
    break;
    case 'member_stats':
    loadMemberStats();
    break;
}
}
    document.getElementById('year')
    document.getElementById('month')
    document.getElementById('includeMembership')




    // ========================================
    // 1. 월별 매출 통계
    // ========================================
    function loadMonthlySales(year, month, includeMembership) {
    const monthParam = month === 'all' ? '' : month;
    const url = (CONTEXT_PATH === '' ? '' : CONTEXT_PATH) +
    '/statistic/api/monthly-sales?year=' + year +
    '&month=' + monthParam +
    '&includeMembership=' + includeMembership;

    console.log('API 호출:', url);
    console.log('파라미터:', {year, month, monthParam, includeMembership});

    fetch(url)
    .then(response => {
    console.log('응답 상태:', response.status);
    if (!response.ok) {
    throw new Error('HTTP error! status: ' + response.status);
}
    return response.json();
})
    .then(data => {
    console.log('월별 매출 데이터:', data);
    drawMonthlySalesChart(data);
})
    .catch(error => {
    console.error('월별 매출 로드 실패:', error);
    showError('월별 매출 데이터를 불러오는데 실패했습니다. ' + error.message);
});
}
    function drawMonthlySalesChart(data) {
    console.log("받은 데이터:", data);
    if (data.error) {
    showError('서버 오류: ' + data.message + ' (' + data.cause + ')');
    return;
}
    // 데이터 없음 체크
    if (!data.categories || data.categories.length === 0) {
    showError('해당 기간의 데이터가 없습니다.');
    return;
}
    // normalSales가 없거나 빈 배열인 경우
    if (!data.normalSales || data.normalSales.length === 0) {
    showError('매출 데이터가 없습니다.');
    return;
}

    const series = [];

    // 일반 매출
    series.push({
    name: '일반 매출',
    data: data.normalSales,
    color: '#4472C4'
});

    // 회원권 매출 (포함된 경우)
    if (data.includeMembership) {
    series.push({
    name: '회원권 매출',
    data: data.memberSales,
    color: '#70AD47'
});
}

    Highcharts.chart('chart_container', {
    chart: {
    type: 'column'
},
    title: {
    text: data.categories.length > 12 ? '일별 매출 현황' : '월별 매출 현황'
},
    xAxis: {
    categories: data.categories
},
    yAxis: {
    min: 0,
    title: {
    text: '매출액 (원)'
}
},
    tooltip: {
    pointFormat: '<b>{point.y:,.0f}원</b>'
},
    plotOptions: {
    column: {
    stacking: 'normal'
}
},
    series: series
});
        showMonthlySalesSummary(data);
}

    // ========================================
    // 2. 누적 매출 통계
    // ========================================
    function loadCumulativeSales(year, month, includeMembership) {
        const monthParam = month === 'all' ? '' : month;
        const url = CONTEXT_PATH + '/statistic/api/cumulative-sales?year=' + year +
            '&month=' + monthParam + '&includeMembership=' + includeMembership;

        // 이전 기간 계산
        let prevYear = year, prevMonth = '';
        if (month === 'all' || !month) {
            prevYear = year - 1;
            prevMonth = '';
        } else {
            const m = parseInt(month);
            if (m === 1) { prevYear = year - 1; prevMonth = 12; }
            else { prevYear = year; prevMonth = m - 1; }
        }
        const prevUrl = CONTEXT_PATH + '/statistic/api/cumulative-sales?year=' + prevYear +
            '&month=' + prevMonth + '&includeMembership=' + includeMembership;

        showLoading();

        Promise.all([
            fetch(url).then(r => { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); }),
            fetch(prevUrl).then(r => r.ok ? r.json() : null).catch(() => null)
        ])
            .then(([currentData, prevData]) => {
                drawCumulativeSalesChart(currentData, prevData);
            })
            .catch(error => {
                showError('누적 매출 데이터를 불러오는데 실패했습니다. ' + error.message);
            });
    }

    function drawCumulativeSalesChart(data, prevData) {
    if (!data.categories || data.categories.length === 0) {
    showError('해당 기간의 데이터가 없습니다.');
    return;
}

    const series = [];

    // 일반 누적 매출
    series.push({
    name: '일반 누적',
    data: data.cumulativeNormal,
    color: '#4472C4'
});

    // 회원 누적 매출 (포함된 경우)
    if (data.includeMembership) {
    series.push({
    name: '회원 누적',
    data: data.cumulativeMember,
    color: '#70AD47'
});
}

    Highcharts.chart('chart_container', {
    chart: {
    type: 'column'
},
    title: {
    text: data.title || '누적 매출 현황'
},
    xAxis: {
    categories: data.categories
},
    yAxis: {
    min: 0,
    title: {
    text: '누적 매출액 (원)'
}
},
    tooltip: {
    pointFormat: '<b>{point.y:,.0f}원</b>'
},
    series: series
});
        showCumulativeSalesSummary(data, prevData);

}

    // ========================================
    // 3. 차종별 통계 (파이 차트)
    // ========================================
    function loadCarTypePie(year, month) {
    const monthParam = month === 'all' ? '' : month;
    const url = CONTEXT_PATH + '/statistic/api/car-type-stats?year=' + year +
    '&month=' + monthParam;

    console.log('API 호출:', url);

    fetch(url)
    .then(response => {
    if (!response.ok) throw new Error('HTTP error! status: ' + response.status);
    return response.json();
})
    .then(data => {
    console.log('차종별 통계 데이터:', data);
    drawCarTypePie(data);
})
    .catch(error => {
    console.error('차종별 통계 로드 실패:', error);
    showError('차종별 통계 데이터를 불러오는데 실패했습니다. ' + error.message);
});
}

    function drawCarTypePie(data) {
    if (!data.data || data.data.length === 0) {
    showError('해당 기간의 데이터가 없습니다.');
    return;
}
        hideSummary();

    Highcharts.chart('chart_container', {
    chart: {
    type: 'pie'
},
    title: {
    text: '차종별 통계'
},
    tooltip: {
    pointFormat: '<b>{point.y}대 ({point.percentage:.1f}%)</b>'
},
    subtitle: { text: '총 ' + data.total + '대' },
    plotOptions: {
    pie: {
    allowPointSelect: true,
    cursor: 'pointer',
    dataLabels: {
    enabled: true,
        format: '<b>{point.name}</b><br>{point.y}대 ({point.percentage:.1f}%)'
}
}
},
    series: [{
    name: '차종',
    colorByPoint: true,
    data: data.data
}]
});
}

    // ========================================
    // 4. 피크 시간대 분석
    // ========================================
    function loadPeakTime(year, month) {
        const monthParam = month === 'all' ? '' : month;
        const url = CONTEXT_PATH + '/statistic/api/peak-time?year=' + year +
            '&month=' + monthParam;

        fetch(url)
            .then(response => {
                if (!response.ok) throw new Error('HTTP error! status: ' + response.status);
                return response.json();
            })
            .then(data => {
                console.log('피크 시간대 데이터:', data);
                drawPeakTimeChart(data);
            })
            .catch(error => {
                console.error('피크 시간대 로드 실패:', error);
                showError('피크 시간대 데이터를 불러오는데 실패했습니다. ' + error.message);
            });
    }

    function drawPeakTimeChart(data) {
    Highcharts.chart('chart_container', {
        chart: {
            type: 'column'
        },
        title: {
            text: '시간대별 입차 현황'
        },
        xAxis: {
            categories: data.categories
        },
        yAxis: {
            min: 0,
            title: {
                text: '입차 대수'
            }
        },
        tooltip: {
            pointFormat: '<b>{point.y}대</b>'
        },
        series: [{
            name: '입차 대수',
            data: data.hourlyCount,
            color: '#ED7D31'
        }]
    });
        showPeakTimeSummary(data);
}

    // ========================================
    // 5. 회원 통계
    // ========================================
    function loadMemberStats() {
    const url = CONTEXT_PATH + '/statistic/api/member-stats';

    console.log('API 호출:', url);

    fetch(url)
    .then(response => {
    if (!response.ok) throw new Error('HTTP error! status: ' + response.status);
    return response.json();
})
    .then(data => {
    console.log('회원 통계 데이터:', data);
    drawMemberStatsChart(data);
})
    .catch(error => {
    console.error('회원 통계 로드 실패:', error);
    showError('회원 통계 데이터를 불러오는데 실패했습니다. ' + error.message);
});
}

    function drawMemberStatsChart(data) {
    hideSummary();
    const pieData = [
{
    name: '활성 회원',
    y: data.activeCount,
    color: '#70AD47'
},
{
    name: '비활성 회원',
    y: data.inactiveCount,
    color: '#FFC000'
}
    ];

    Highcharts.chart('chart_container', {
    chart: {
    type: 'pie'
},
    title: {
    text: '회원 현황 (총 ' + data.totalCount + '명)'
},
    tooltip: {
    pointFormat: '<b>{point.y}명 ({point.percentage:.1f}%)</b>'
},
    plotOptions: {
    pie: {
    allowPointSelect: true,
    cursor: 'pointer',
    dataLabels: {
    enabled: true,
    format: '<b>{point.name}</b>: {point.y}명 ({point.percentage:.1f}%)'
}
}
},
    series: [{
    name: '회원',
    colorByPoint: true,
    data: pieData
}]
});
}

    // ========================================
    // 통계 요약 함수들
    // ========================================
    function showMonthlySalesSummary(data) {
        const isDaily = data.categories.length > 12;
        const unit = isDaily ? '일' : '월';

        const totals = data.normalSales.map((v, i) =>
            v + (data.memberSales && data.includeMembership ? (data.memberSales[i] || 0) : 0)
        );

        const maxIdx = totals.indexOf(Math.max(...totals));
        const minNonZeroIdx = totals.reduce((best, v, i) =>
            v > 0 && (best === -1 || v < totals[best]) ? i : best, -1);
        const avg = Math.round(totals.reduce((a, b) => a + b, 0) / totals.filter(v => v > 0).length) || 0;

        renderSummaryCards([
            {
                label: '최고 매출 ' + unit,
                value: data.categories[maxIdx],
                sub: numberFormat(totals[maxIdx]) + '원',
                color: '#4472C4'
            },
            {
                label: '최저 매출 ' + unit,
                value: minNonZeroIdx >= 0 ? data.categories[minNonZeroIdx] : '-',
                sub: minNonZeroIdx >= 0 ? numberFormat(totals[minNonZeroIdx]) + '원' : '-',
                color: '#ED7D31'
            },
            {
                label: '평균 매출액',
                value: numberFormat(avg) + '원',
                sub: '매출 발생 ' + unit + ' 기준',
                color: '#70AD47'
            }
        ]);
    }

    function showCumulativeSalesSummary(data, prevData) {
        const isMonthly = data.categories && data.categories[0] && data.categories[0].includes('월');
        const periodLabel = isMonthly ? '전년' : '전월';

        const currentFinal = (data.cumulativeNormal || []).slice(-1)[0] || 0;
        const currentMemberFinal = (data.cumulativeMember && data.includeMembership)
            ? (data.cumulativeMember.slice(-1)[0] || 0) : 0;
        const currentTotal = currentFinal + currentMemberFinal;

        let prevTotal = 0;
        if (prevData && prevData.cumulativeNormal) {
            const prevFinal = prevData.cumulativeNormal.slice(-1)[0] || 0;
            const prevMemberFinal = (prevData.cumulativeMember && prevData.includeMembership)
                ? (prevData.cumulativeMember.slice(-1)[0] || 0) : 0;
            prevTotal = prevFinal + prevMemberFinal;
        }

        const diff = currentTotal - prevTotal;
        const rate = prevTotal > 0 ? ((diff / prevTotal) * 100).toFixed(1) : null;

        renderSummaryCards([
            {
                label: '최종 누적 매출',
                value: numberFormat(currentTotal) + '원',
                sub: data.title || '',
                color: '#4472C4'
            },
            {
                label: periodLabel + ' 대비 증감',
                value: rate !== null ? (diff >= 0 ? '+' : '-') + numberFormat(diff) + '원' : '비교 데이터 없음', // ← 수정
                sub: rate !== null ? (diff >= 0 ? '+' : '') + rate + '%' : '',  // ← 수치 없을 때 sub 빈 문자열
                color: rate !== null ? (diff >= 0 ? '#70AD47' : '#ED7D31') : '#8a93aa' // ← 회색으로
            }
        ]);
    }

    function showPeakTimeSummary(data) {
        const counts = data.hourlyCount;
        const total = counts.reduce((a, b) => a + b, 0);

        const sorted = counts
            .map((v, i) => ({ hour: i, count: v }))
            .sort((a, b) => b.count - a.count);

        const top3Text = sorted.slice(0, 3).map(h => h.hour + '시 (' + h.count + '대)').join(', ');

        // 최솟값 구하고 그 값과 같은 시간대 전부 수집
        const minCount = Math.min(...counts);
        const quietestList = counts
            .map((v, i) => ({ hour: i, count: v }))
            .filter(h => h.count === minCount)
            .map(h => h.hour + '시')
            .join(', ');

        renderSummaryCards([
            {
                label: '혼잡 TOP 3',
                value: top3Text,
                sub: '입차가 가장 많은 시간대',
                color: '#ED7D31'
            },
            {
                label: '가장 한산한 시간대',
                value: quietestList,
                sub: minCount + '대',
                color: '#4472C4'
            },
            {
                label: '전체 입차 대수',
                value: numberFormat(total) + '대',
                sub: '조회 기간 합계',
                color: '#70AD47'
            }
        ]);
    }

    function renderSummaryCards(cards) {
        const section = document.getElementById('summarySection');
        const container = document.getElementById('summaryCards');
        if (!section || !container) return;

        container.innerHTML = cards.map(card => `
        <div class="summary-card" style="border-top: 4px solid ${card.color}">
            <div class="summary-label">${card.label}</div>
            <div class="summary-value" style="color:${card.color}">${card.value}</div>
            <div class="summary-sub">${card.sub}</div>
        </div>
    `).join('');

        section.style.display = 'block';
    }

    function hideSummary() {
        const section = document.getElementById('summarySection');
        if (section) section.style.display = 'none';
    }

    function numberFormat(n) {
        return Math.abs(n).toLocaleString('ko-KR');
    }



    // ========================================
    // 유틸리티 함수
    // ========================================
    function showLoading() {
    document.getElementById('chart_container').innerHTML = `
        <div class="text-center p-5">
            <div class="spinner-border" role="status">
                <span class="visually-hidden">로딩중...</span>
            </div>
            <p class="mt-3">데이터를 불러오는 중입니다...</p>
        </div>
    `;
}

    function showError(message) {
    document.getElementById('chart_container').innerHTML = `
        <div class="alert alert-warning text-center p-5" role="alert">
            ${message}
        </div>
    `;
}

    // 오늘 요약 갱신 (옵션)
    function refreshTodaySummary() {
    fetch(CONTEXT_PATH + '/statistic/api/today-summary')
        .then(response => response.json())
        .then(data => {
            document.getElementById('dailySales').textContent = data.dailySales.toLocaleString();
            document.getElementById('dailyCount').textContent = data.dailyCount;
            document.getElementById('totalCount').textContent = data.totalCount;
        })
        .catch(error => {
            console.error('오늘 요약 갱신 실패:', error);
        });
}
