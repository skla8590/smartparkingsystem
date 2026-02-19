<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>통계 | 스마트 주차 관리 시스템</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/web/statistic/statistic.css">

  <!-- Highcharts 라이브러리 -->
  <script src="https://code.highcharts.com/highcharts.js"></script>
  <script src="https://code.highcharts.com/modules/exporting.js"></script>

</head>
<body>
<!-- 헤더 불러오기 -->
<%@ include file="../common/header_main.jsp" %>


  <div class="content">

      <!-- 필터 폼 (AJAX 방식으로 변경) -->
      <div class="filter-container">
        <div class="filter-row">
          <label>통계 유형</label>
          <select id="chartType">
            <option value="monthly_sales">년도 / 월별 매출</option>
            <option value="cumulative_sales">누적 매출</option>
            <option value="car_type_pie">차종별 통계</option>
            <option value="peak_time">피크 시간대</option>
            <option value="member_stats">회원 통계</option>
          </select>
        </div>

        <div class="filter-row" id="dateFilterRow">
          <label>상세 조건</label>
          <select id="year"></select>
          <select id="month"></select>
          <label id="membershipLabel">
            <input type="checkbox" id="includeMembership">
            회원권 매출 포함
          </label>
          <button class="btn btn-primary" id="searchBtn" onclick="loadChart()">조회</button>
        </div>
      </div>

      <!-- 차트 컨테이너 -->
      <div id="chart_container" style="min-height: 400px;">
        <div class="text-center p-5">
          <p>조회 버튼을 클릭하여 통계를 확인하세요.</p>
        </div>
      </div>

      <div id="summarySection" style="display:none;">
        <div class="section-title">통계 요약</div>
        <div id="summaryCards" class="summary-cards"></div>
      </div>






    <div class="info-box2">


      <div class="section-title">일일 현황</div>
      <div class="summary">
        <div class="summary-box">
          일일 총 매출액: <span id="dailySales">${todaySummary.dailySales}</span>원
        </div>
        <div class="summary-box">
          일일 입차 대수: <span id="dailyCount">${todaySummary.dailyCount}</span>대
        </div>
        <div class="summary-box">
          누적 차량 대수: <span id="totalCount">${todaySummary.totalCount}</span>대
        </div>
      </div>
    </div>

  <script>
    const CONTEXT_PATH = '<%= request.getContextPath() %>';
  </script>

  <!-- js 불러오기 -->
  <script src="${pageContext.request.contextPath}/web/statistic/statistic.js"></script>

</div>
</body>


</html>
