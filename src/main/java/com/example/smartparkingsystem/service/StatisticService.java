package com.example.smartparkingsystem.service;

import com.example.smartparkingsystem.dao.MembersDAO;
import com.example.smartparkingsystem.dao.ParkingHistoryDAO;
import com.example.smartparkingsystem.dao.PaymentHistoryDAO;
import com.example.smartparkingsystem.dao.PaymentInfoDAO;
import com.example.smartparkingsystem.dto.PaymentHistoryDTO;
import com.example.smartparkingsystem.vo.MembersVO;
import com.example.smartparkingsystem.vo.ParkingHistoryVO;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Log4j2
public enum StatisticService {
    INSTANCE;

    // DAO 객체들
    private final PaymentHistoryDAO paymentHistoryDAO;
    private final MembersDAO membersDAO;
    private final ParkingHistoryDAO parkingHistoryDAO;
    private final PaymentInfoDAO paymentInfoDAO = new PaymentInfoDAO();

    // ========== 캐싱 변수 ==========
    private Map<Integer, Map<Integer, List<PaymentHistoryDTO>>> cachedPaymentData = null;
    private long lastCacheTime = 0;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5분 캐시 유지

    // ========== 회원권 가격 상수 ==========
    private final int MEMBERSHIP_PRICE_PER_30DAYS = paymentInfoDAO.selectInfo().getMemberCharge();

    StatisticService() {
        paymentHistoryDAO = new PaymentHistoryDAO();
        membersDAO = new MembersDAO();
        parkingHistoryDAO = new ParkingHistoryDAO();
    }

    /**
     * 캐싱된 결제 데이터 조회 (5분마다 갱신)
     */
    private Map<Integer, Map<Integer, List<PaymentHistoryDTO>>> getCachedPaymentData() {
        long currentTime = System.currentTimeMillis();
        if (cachedPaymentData == null || (currentTime - lastCacheTime) > CACHE_DURATION) {
            log.info("캐시 갱신 - DB에서 데이터 조회");
            cachedPaymentData = paymentHistoryDAO.selectOrderByYearMonth();
            lastCacheTime = currentTime;
        } else {
            log.info("캐시 사용 - DB 조회 생략");
        }
        return cachedPaymentData;
    }

    /**
     * 캐시 강제 갱신
     */
    public void refreshCache() {
        log.info("캐시 강제 갱신");
        cachedPaymentData = null;
        lastCacheTime = 0;
    }

    /**
     * 회원권 매출 계산 (30일당 요금)
     */
    private int calculateMembershipRevenue(LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        int periods = (int) Math.ceil(days / 30.0);
        return periods * MEMBERSHIP_PRICE_PER_30DAYS;
    }

    // ========================================================
    // 통계 조회 메서드들
    // ========================================================

    /**
     * 통계 페이지용 전체 데이터 조회
     */
    public Map<String, Object> getAllStatisticsData() {
        log.info("전체 통계 데이터 조회 시작");

        Map<Integer, Map<Integer, List<PaymentHistoryDTO>>> paymentDataByYear = getCachedPaymentData();
        List<MembersVO> allMembers = membersDAO.selectAllMembers();

        int totalCount = 0;
        for (Map<Integer, List<PaymentHistoryDTO>> monthMap : paymentDataByYear.values()) {
            for (List<PaymentHistoryDTO> records : monthMap.values()) {
                totalCount += records.size();
            }
        }

        log.info("총 데이터: " + totalCount + "건");
        log.info("회원: " + allMembers.size() + "명");

        Map<String, Object> memberStats = calculateMemberStats(allMembers);

        Map<String, Object> result = new HashMap<>();
        result.put("paymentDataByYear", paymentDataByYear);
        result.put("memberStats", memberStats);
        result.put("totalCount", totalCount);

        log.info("전체 통계 데이터 조회 완료");
        return result;
    }

    /**
     * 회원 통계 계산
     */
    private Map<String, Object> calculateMemberStats(List<MembersVO> members) {
        log.info("회원 통계 계산 시작");

        LocalDate today = LocalDate.now();
        int totalCount = members.size();
        int activeCount = 0;

        for (MembersVO member : members) {
            LocalDate endDate = member.getEndDate();
            if (endDate.isAfter(today) || endDate.isEqual(today)) {
                activeCount++;
            }
        }

        int inactiveCount = totalCount - activeCount;

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("activeCount", activeCount);
        result.put("inactiveCount", inactiveCount);

        log.info("회원 통계: 총 " + totalCount + "명, 활성 " + activeCount + "명, 비활성 " + inactiveCount + "명");
        return result;
    }

    /**
     * 오늘 날짜 요약 통계
     */
    public Map<String, Object> getTodaySummary() {
        log.info("오늘 날짜 요약 통계 조회 시작");

        LocalDate today = LocalDate.now();
        List<ParkingHistoryVO> todayRecords = parkingHistoryDAO.selectByDate(today);
        int dailyCount = todayRecords.size();
        int totalCount = parkingHistoryDAO.getTotalCount();
        int dailySales = calculateDailySales(today);

        Map<String, Object> summary = new HashMap<>();
        summary.put("dailySales", dailySales);
        summary.put("dailyCount", dailyCount);
        summary.put("totalCount", totalCount);

        log.info("오늘 요약: 입차 " + dailyCount + "대, 매출 " + dailySales + "원, 누적 " + totalCount + "대");
        return summary;
    }

    /**
     * 오늘 매출 계산
     */
    private int calculateDailySales(LocalDate date) {
        Map<Integer, Map<Integer, List<PaymentHistoryDTO>>> paymentDataByYear = getCachedPaymentData();
        int year = date.getYear();
        int month = date.getMonthValue();

        Map<Integer, List<PaymentHistoryDTO>> monthMap = paymentDataByYear.get(year);
        if (monthMap == null) return 0;

        List<PaymentHistoryDTO> records = monthMap.get(month);
        if (records == null) return 0;

        int dailySales = 0;
        for (PaymentHistoryDTO record : records) {
            if (record.getEntryTime().toLocalDate().equals(date)) {
                dailySales += record.getFinalCharge();
            }
        }
        return dailySales;
    }

    /**
     * 회원 통계만 조회 (AJAX용)
     */
    public Map<String, Object> getMemberStats() {
        log.info("회원 통계 조회");
        List<MembersVO> allMembers = membersDAO.selectAllMembers();
        return calculateMemberStats(allMembers);
    }

    // ========================================================
    // 차트용 통계 메서드들
    // ========================================================

    /**
     * 1. 월별 매출 통계
     */
    public Map<String, Object> getMonthlySales(int year, Integer month, boolean includeMembership) {
        log.info("월별 매출 통계 조회: year={}, month={}, includeMembership={}", year, month, includeMembership);

        Map<Integer, Map<Integer, List<PaymentHistoryDTO>>> paymentDataByYear = getCachedPaymentData();
        Map<Integer, List<PaymentHistoryDTO>> yearMap = paymentDataByYear.get(year);

        if (yearMap == null) {
            log.warn("해당 년도 데이터 없음: {}", year);
            return new HashMap<>();
        }

        Map<String, Object> response = new HashMap<>();

        if (month == null) {
            calculateMonthlyAggregation(year, yearMap, includeMembership, response);
        } else {
            calculateDailyAggregation(year, month, yearMap, includeMembership, response);
        }

        return response;
    }

    /**
     * 월별 집계 (1월~12월)
     */
    private void calculateMonthlyAggregation(int year, Map<Integer, List<PaymentHistoryDTO>> yearMap,
                                             boolean includeMembership, Map<String, Object> response) {

        List<String> categories = new ArrayList<>();
        List<Integer> normalSales = new ArrayList<>();
        List<Integer> memberSales = new ArrayList<>();

        // 회원권 매출 계산
        Map<Integer, Integer> monthlyMembershipRevenue = new HashMap<>();
        if (includeMembership) {
            List<MembersVO> allMembers = membersDAO.selectAllMembers();
            for (MembersVO member : allMembers) {
                if (member.getStartDate().getYear() == year) {
                    int m = member.getStartDate().getMonthValue();
                    int revenue = calculateMembershipRevenue(member.getStartDate(), member.getEndDate());
                    monthlyMembershipRevenue.put(m, monthlyMembershipRevenue.getOrDefault(m, 0) + revenue);
                }
            }
        }

        // 월 오름차순 정렬
        List<Integer> sortedMonths = new ArrayList<>(yearMap.keySet());
        Collections.sort(sortedMonths);

        for (int m : sortedMonths) {
            categories.add(m + "월");
            List<PaymentHistoryDTO> records = yearMap.get(m);

            int normalSum = 0;
            for (PaymentHistoryDTO record : records) {
                if (record.getMno() == null) {
                    normalSum += record.getFinalCharge();
                }
            }

            normalSales.add(normalSum);
            memberSales.add(monthlyMembershipRevenue.getOrDefault(m, 0));
        }

        response.put("categories", categories);
        response.put("normalSales", normalSales);
        response.put("memberSales", memberSales);
        response.put("includeMembership", includeMembership);
    }

    /**
     * 일별 집계 (특정 월의 1일~말일)
     */
    private void calculateDailyAggregation(int year, int month, Map<Integer, List<PaymentHistoryDTO>> yearMap,
                                           boolean includeMembership, Map<String, Object> response) {

        List<PaymentHistoryDTO> records = yearMap.get(month);
        if (records == null) {
            log.warn("해당 월 데이터 없음: {}월", month);
            return;
        }

        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
        List<String> categories = new ArrayList<>();
        int[] dailyNormal = new int[daysInMonth];
        int[] dailyMember = new int[daysInMonth];

        for (int i = 1; i <= daysInMonth; i++) {
            categories.add(i + "일");
        }

        if (includeMembership) {
            List<MembersVO> allMembers = membersDAO.selectAllMembers();
            for (MembersVO member : allMembers) {
                LocalDate startDate = member.getStartDate();
                if (startDate.getYear() == year && startDate.getMonthValue() == month) {
                    int day = startDate.getDayOfMonth();
                    if (day >= 1 && day <= daysInMonth) {
                        int revenue = calculateMembershipRevenue(member.getStartDate(), member.getEndDate());
                        dailyMember[day - 1] += revenue;
                    }
                }
            }
        }

        for (PaymentHistoryDTO record : records) {
            int day = record.getEntryTime().getDayOfMonth();
            if (day >= 1 && day <= daysInMonth && record.getMno() == null) {
                dailyNormal[day - 1] += record.getFinalCharge();
            }
        }

        response.put("categories", categories);
        response.put("normalSales", toList(dailyNormal));
        response.put("memberSales", toList(dailyMember));
        response.put("includeMembership", includeMembership);
    }

    /**
     * 2. 누적 매출 통계
     */
    public Map<String, Object> getCumulativeSales(int year, Integer month, boolean includeMembership) {
        log.info("누적 매출 통계 조회: year={}, month={}, includeMembership={}", year, month, includeMembership);

        Map<Integer, Map<Integer, List<PaymentHistoryDTO>>> paymentDataByYear = getCachedPaymentData();
        Map<Integer, List<PaymentHistoryDTO>> yearMap = paymentDataByYear.get(year);

        if (yearMap == null) {
            log.warn("해당 년도 데이터 없음: {}", year);
            return new HashMap<>();
        }

        Map<String, Object> response = new HashMap<>();

        if (month == null) {
            calculateMonthlyCumulative(year, yearMap, includeMembership, response);
            response.put("title", year + "년 누적 매출 현황");
        } else {
            calculateDailyCumulative(year, month, yearMap, includeMembership, response);
            response.put("title", month + "월 일별 누적 매출 현황");
        }

        return response;
    }

    /**
     * 월별 누적 계산
     */
    private void calculateMonthlyCumulative(int year, Map<Integer, List<PaymentHistoryDTO>> yearMap,
                                            boolean includeMembership, Map<String, Object> response) {

        List<String> categories = new ArrayList<>();
        List<Integer> cumNormal = new ArrayList<>();
        List<Integer> cumMember = new ArrayList<>();

        int runningNormal = 0;
        int runningMember = 0;

        // 회원권 매출 계산
        Map<Integer, Integer> monthlyMembershipRevenue = new HashMap<>();
        if (includeMembership) {
            List<MembersVO> allMembers = membersDAO.selectAllMembers();
            for (MembersVO member : allMembers) {
                if (member.getStartDate().getYear() == year) {
                    int m = member.getStartDate().getMonthValue();
                    int revenue = calculateMembershipRevenue(member.getStartDate(), member.getEndDate());
                    monthlyMembershipRevenue.put(m, monthlyMembershipRevenue.getOrDefault(m, 0) + revenue);
                }
            }
        }

        // 월 오름차순 정렬
        List<Integer> sortedMonths = new ArrayList<>(yearMap.keySet());
        Collections.sort(sortedMonths);

        for (int m : sortedMonths) {
            categories.add(m + "월");
            List<PaymentHistoryDTO> records = yearMap.get(m);

            int normalSum = 0;
            for (PaymentHistoryDTO record : records) {
                if (record.getMno() == null) {
                    normalSum += record.getFinalCharge();
                }
            }

            runningNormal += normalSum;
            runningMember += monthlyMembershipRevenue.getOrDefault(m, 0);

            cumNormal.add(runningNormal);
            cumMember.add(runningMember);
        }

        response.put("categories", categories);
        response.put("cumulativeNormal", cumNormal);
        response.put("cumulativeMember", cumMember);
        response.put("includeMembership", includeMembership);
    }

    /**
     * 일별 누적 계산
     */
    private void calculateDailyCumulative(int year, int month, Map<Integer, List<PaymentHistoryDTO>> yearMap,
                                          boolean includeMembership, Map<String, Object> response) {

        List<PaymentHistoryDTO> records = yearMap.get(month);
        if (records == null) {
            log.warn("해당 월 데이터 없음: {}월", month);
            return;
        }

        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
        List<String> categories = new ArrayList<>();
        int[] dailyNormal = new int[daysInMonth];
        int[] dailyMember = new int[daysInMonth];

        for (int i = 1; i <= daysInMonth; i++) {
            categories.add(i + "일");
        }

        if (includeMembership) {
            List<MembersVO> allMembers = membersDAO.selectAllMembers();
            for (MembersVO member : allMembers) {
                LocalDate startDate = member.getStartDate();
                if (startDate.getYear() == year && startDate.getMonthValue() == month) {
                    int day = startDate.getDayOfMonth();
                    if (day >= 1 && day <= daysInMonth) {
                        int revenue = calculateMembershipRevenue(member.getStartDate(), member.getEndDate());
                        dailyMember[day - 1] += revenue;
                    }
                }
            }
        }

        for (PaymentHistoryDTO record : records) {
            int day = record.getEntryTime().getDayOfMonth();
            if (day >= 1 && day <= daysInMonth && record.getMno() == null) {
                dailyNormal[day - 1] += record.getFinalCharge();
            }
        }

        // 누적 계산
        List<Integer> cumNormal = new ArrayList<>();
        List<Integer> cumMember = new ArrayList<>();
        int runningNormal = 0;
        int runningMember = 0;

        for (int i = 0; i < daysInMonth; i++) {
            runningNormal += dailyNormal[i];
            runningMember += dailyMember[i];
            cumNormal.add(runningNormal);
            cumMember.add(runningMember);
        }

        response.put("categories", categories);
        response.put("cumulativeNormal", cumNormal);
        response.put("cumulativeMember", cumMember);
        response.put("includeMembership", includeMembership);
    }

    /**
     * 3. 차종별 통계 (파이 차트)
     */
    public Map<String, Object> getCarTypeStats(int year, Integer month) {
        log.info("차종별 통계 조회: year={}, month={}", year, month);

        LocalDate startDate = LocalDate.of(year, month != null ? month : 1, 1);
        LocalDate endDate = month != null ?
                LocalDate.of(year, month, startDate.lengthOfMonth()) :
                LocalDate.of(year, 12, 31);

        Map<String, Integer> countMap = new HashMap<>();
        int totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

        for (int i = 0; i < totalDays; i++) {
            LocalDate date = startDate.plusDays(i);
            List<ParkingHistoryVO> records = parkingHistoryDAO.selectByDate(date);
            for (ParkingHistoryVO record : records) {
                String carType = record.getCarType();
                countMap.put(carType, countMap.getOrDefault(carType, 0) + 1);
            }
        }

        int total = countMap.values().stream().mapToInt(Integer::intValue).sum();
        List<Map<String, Object>> pieData = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", entry.getKey());
            data.put("y", entry.getValue());
            data.put("percentage", total > 0 ? String.format("%.1f", (entry.getValue() * 100.0 / total)) : "0.0");
            pieData.add(data);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", pieData);
        response.put("total", total);

        log.info("차종별 통계: 총 {}대", total);
        return response;
    }

    /**
     * 4. 피크 시간대 분석
     */
    public Map<String, Object> getPeakTimeStats(int year, Integer month) {
        log.info("피크 시간대 분석 조회: year={}, month={}", year, month);

        Map<Integer, Map<Integer, List<PaymentHistoryDTO>>> paymentDataByYear = getCachedPaymentData();
        int[] hourlyCount = new int[24];

        Map<Integer, List<PaymentHistoryDTO>> yearMap = paymentDataByYear.get(year);
        if (yearMap != null) {
            for (Map.Entry<Integer, List<PaymentHistoryDTO>> entry : yearMap.entrySet()) {
                if (month != null && !entry.getKey().equals(month)) continue;
                for (PaymentHistoryDTO record : entry.getValue()) {
                    int hour = record.getEntryTime().getHour();
                    if (hour >= 0 && hour < 24) hourlyCount[hour]++;
                }
            }
        }

        List<String> categories = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            categories.add(i + "시");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("categories", categories);
        response.put("hourlyCount", toList(hourlyCount));

        log.info("피크 시간대 분석 완료");
        return response;
    }

    // ===============
    // 유틸리티 메서드
    // ===============

    private List<Integer> toList(int[] array) {
        List<Integer> list = new ArrayList<>();
        for (int value : array) {
            list.add(value);
        }
        return list;
    }
}