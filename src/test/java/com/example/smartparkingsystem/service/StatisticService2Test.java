package com.example.smartparkingsystem.service;

import com.example.smartparkingsystem.dto.MonthlyData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StatisticService2Test {

    private StatisticService service;

    @BeforeEach
    void setUp() {
        service = StatisticService.INSTANCE;
        // 테스트 전 캐시 초기화
        service.refreshCache();
    }

    // ========================================
    // 1. 기본 통계 조회 테스트
    // ========================================

    @Test
    @Order(1)
    @DisplayName("전체 통계 데이터 조회 테스트")
    void testGetAllStatisticsData() {
        // given & when
        Map<String, Object> result = service.getAllStatisticsData();

        // then
        assertNotNull(result, "결과가 null이면 안됨");
        assertTrue(result.containsKey("paymentDataByYear"), "paymentDataByYear 키 존재해야 함");
        assertTrue(result.containsKey("memberStats"), "memberStats 키 존재해야 함");
        assertTrue(result.containsKey("totalCount"), "totalCount 키 존재해야 함");

        // 데이터 구조 검증
        Map<Integer, List<MonthlyData>> paymentData = (Map<Integer, List<MonthlyData>>) result.get("paymentDataByYear");
        assertNotNull(paymentData, "paymentDataByYear는 null이면 안됨");

        Map<String, Object> memberStats = (Map<String, Object>) result.get("memberStats");
        assertNotNull(memberStats, "memberStats는 null이면 안됨");
        assertTrue(memberStats.containsKey("totalCount"), "회원 총 수 필요");
        assertTrue(memberStats.containsKey("activeCount"), "활성 회원 수 필요");
        assertTrue(memberStats.containsKey("inactiveCount"), "비활성 회원 수 필요");

        Integer totalCount = (Integer) result.get("totalCount");
        assertNotNull(totalCount, "totalCount는 null이면 안됨");
        assertTrue(totalCount >= 0, "totalCount는 0 이상이어야 함");

        System.out.println("=== 전체 통계 데이터 ===");
        System.out.println("총 결제 건수: " + totalCount);
        System.out.println("회원 통계: " + memberStats);
        System.out.println("년도별 데이터: " + paymentData.keySet());
    }

    @Test
    @Order(2)
    @DisplayName("회원 통계 조회 테스트")
    void testGetMemberStats() {
        // given & when
        Map<String, Object> memberStats = service.getMemberStats();

        // then
        assertNotNull(memberStats, "회원 통계는 null이면 안됨");

        int totalCount = (int) memberStats.get("totalCount");
        int activeCount = (int) memberStats.get("activeCount");
        int inactiveCount = (int) memberStats.get("inactiveCount");

        // 논리 검증
        assertEquals(totalCount, activeCount + inactiveCount, "전체 = 활성 + 비활성");
        assertTrue(totalCount >= 0, "총 회원 수는 0 이상");
        assertTrue(activeCount >= 0, "활성 회원 수는 0 이상");
        assertTrue(inactiveCount >= 0, "비활성 회원 수는 0 이상");

        System.out.println("=== 회원 통계 ===");
        System.out.println("총 회원: " + totalCount + "명");
        System.out.println("활성 회원: " + activeCount + "명");
        System.out.println("비활성 회원: " + inactiveCount + "명");
    }

    @Test
    @Order(3)
    @DisplayName("오늘 요약 통계 테스트")
    void testGetTodaySummary() {
        // given & when
        Map<String, Object> summary = service.getTodaySummary();

        // then
        assertNotNull(summary, "오늘 요약은 null이면 안됨");
        assertTrue(summary.containsKey("dailySales"), "일일 매출 필요");
        assertTrue(summary.containsKey("dailyCount"), "일일 입차 대수 필요");
        assertTrue(summary.containsKey("totalCount"), "누적 대수 필요");

        int dailySales = (int) summary.get("dailySales");
        int dailyCount = (int) summary.get("dailyCount");
        int totalCount = (int) summary.get("totalCount");

        assertTrue(dailySales >= 0, "일일 매출은 0 이상");
        assertTrue(dailyCount >= 0, "일일 입차는 0 이상");
        assertTrue(totalCount >= 0, "누적 대수는 0 이상");

        System.out.println("=== 오늘 요약 ===");
        System.out.println("일일 매출: " + dailySales + "원");
        System.out.println("일일 입차: " + dailyCount + "대");
        System.out.println("누적 대수: " + totalCount + "대");
    }

    // ========================================
    // 2. 캐시 테스트
    // ========================================

    @Test
    @Order(4)
    @DisplayName("캐시 동작 테스트")
    void testCacheFunction() {
        // given
        service.refreshCache(); // 캐시 초기화

        // when - 첫 번째 호출 (DB 조회)
        long start1 = System.currentTimeMillis();
        Map<String, Object> result1 = service.getAllStatisticsData();
        long time1 = System.currentTimeMillis() - start1;

        // when - 두 번째 호출 (캐시 사용)
        long start2 = System.currentTimeMillis();
        Map<String, Object> result2 = service.getAllStatisticsData();
        long time2 = System.currentTimeMillis() - start2;

        // then
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.get("totalCount"), result2.get("totalCount"), "같은 데이터 반환해야 함");

        System.out.println("=== 캐시 성능 ===");
        System.out.println("첫 번째 조회 (DB): " + time1 + "ms");
        System.out.println("두 번째 조회 (캐시): " + time2 + "ms");

        // 캐시가 더 빨라야 함 (일반적으로)
        assertTrue(time2 <= time1 * 2, "캐시 사용 시 성능 개선 예상");
    }

    @Test
    @Order(5)
    @DisplayName("캐시 갱신 테스트")
    void testRefreshCache() {
        // given
        service.getAllStatisticsData(); // 캐시 생성

        // when
        service.refreshCache(); // 캐시 강제 갱신

        // then
        Map<String, Object> result = service.getAllStatisticsData();
        assertNotNull(result, "캐시 갱신 후에도 데이터 조회 가능해야 함");

        System.out.println("캐시 갱신 테스트 완료");
    }

    // ========================================
    // 3. 월별 매출 통계 테스트
    // ========================================

    @Test
    @Order(6)
    @DisplayName("년도별 월간 매출 통계 테스트 (회원권 포함)")
    void testGetMonthlySales_YearlyWithMembership() {
        // given
        int year = 2025;

        // when
        Map<String, Object> result = service.getMonthlySales(year, null, true);

        // then
        assertNotNull(result, "결과는 null이면 안됨");

        if (!result.isEmpty()) {
            assertTrue(result.containsKey("categories"), "카테고리 필요");
            assertTrue(result.containsKey("normalSales"), "일반 매출 필요");
            assertTrue(result.containsKey("memberSales"), "회원권 매출 필요");
            assertTrue(result.containsKey("includeMembership"), "회원권 포함 여부 필요");

            List<String> categories = (List<String>) result.get("categories");
            List<Integer> normalSales = (List<Integer>) result.get("normalSales");
            List<Integer> memberSales = (List<Integer>) result.get("memberSales");

            assertEquals(categories.size(), normalSales.size(), "카테고리와 일반매출 크기 동일");
            assertEquals(categories.size(), memberSales.size(), "카테고리와 회원매출 크기 동일");

            System.out.println("=== " + year + "년 월별 매출 (회원권 포함) ===");
            for (int i = 0; i < categories.size(); i++) {
                System.out.printf("%s - 일반: %d원, 회원: %d원%n",
                        categories.get(i), normalSales.get(i), memberSales.get(i));
            }
        } else {
            System.out.println(year + "년 데이터 없음");
        }
    }

    @Test
    @Order(7)
    @DisplayName("년도별 월간 매출 통계 테스트 (회원권 미포함)")
    void testGetMonthlySales_YearlyWithoutMembership() {
        // given
        int year = LocalDate.now().getYear();

        // when
        Map<String, Object> result = service.getMonthlySales(year, null, false);

        // then
        if (!result.isEmpty()) {
            assertFalse((boolean) result.get("includeMembership"), "회원권 미포함");

            System.out.println("=== " + year + "년 월별 매출 (회원권 미포함) ===");
            List<String> categories = (List<String>) result.get("categories");
            List<Integer> normalSales = (List<Integer>) result.get("normalSales");

            for (int i = 0; i < categories.size(); i++) {
                System.out.printf("%s - 일반: %d원%n", categories.get(i), normalSales.get(i));
            }
        }
    }

    @Test
    @Order(8)
    @DisplayName("특정 월의 일별 매출 통계 테스트")
    void testGetMonthlySales_Daily() {
        // given
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        // when
        Map<String, Object> result = service.getMonthlySales(year, month, true);

        // then
        if (!result.isEmpty()) {
            List<String> categories = (List<String>) result.get("categories");
            List<Integer> normalSales = (List<Integer>) result.get("normalSales");
            List<Integer> memberSales = (List<Integer>) result.get("memberSales");

            assertNotNull(categories, "카테고리 필요");
            assertTrue(categories.size() >= 28 && categories.size() <= 31, "일별 데이터는 28~31개");

            System.out.println("=== " + year + "년 " + month + "월 일별 매출 ===");
            for (int i = 0; i < Math.min(5, categories.size()); i++) {
                System.out.printf("%s - 일반: %d원, 회원: %d원%n",
                        categories.get(i), normalSales.get(i), memberSales.get(i));
            }
            System.out.println("... (총 " + categories.size() + "일)");
        }
    }

    // ========================================
    // 4. 누적 매출 통계 테스트
    // ========================================

    @Test
    @Order(9)
    @DisplayName("년도별 월간 누적 매출 테스트")
    void testGetCumulativeSales_Yearly() {
        // given
        int year = LocalDate.now().getYear();

        // when
        Map<String, Object> result = service.getCumulativeSales(year, null, true);

        // then
        if (!result.isEmpty()) {
            assertTrue(result.containsKey("title"), "타이틀 필요");
            assertTrue(result.containsKey("cumulativeNormal"), "일반 누적 매출 필요");
            assertTrue(result.containsKey("cumulativeMember"), "회원 누적 매출 필요");

            List<Integer> cumNormal = (List<Integer>) result.get("cumulativeNormal");
            List<Integer> cumMember = (List<Integer>) result.get("cumulativeMember");

            // 누적 값은 증가해야 함
            for (int i = 1; i < cumNormal.size(); i++) {
                assertTrue(cumNormal.get(i) >= cumNormal.get(i - 1), "누적 매출은 증가해야 함");
            }

            System.out.println("=== " + result.get("title") + " ===");
            List<String> categories = (List<String>) result.get("categories");
            for (int i = 0; i < categories.size(); i++) {
                System.out.printf("%s - 일반 누적: %d원, 회원 누적: %d원%n",
                        categories.get(i), cumNormal.get(i), cumMember.get(i));
            }
        }
    }

    @Test
    @Order(10)
    @DisplayName("특정 월의 일별 누적 매출 테스트")
    void testGetCumulativeSales_Daily() {
        // given
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        // when
        Map<String, Object> result = service.getCumulativeSales(year, month, true);

        // then
        if (!result.isEmpty()) {
            List<Integer> cumNormal = (List<Integer>) result.get("cumulativeNormal");

            // 누적 값 검증
            for (int i = 1; i < cumNormal.size(); i++) {
                assertTrue(cumNormal.get(i) >= cumNormal.get(i - 1), "누적 매출은 감소하면 안됨");
            }

            System.out.println("=== " + result.get("title") + " ===");
            System.out.println("일별 누적 매출 데이터 수: " + cumNormal.size());
        }
    }

    // ========================================
    // 5. 차종별 통계 테스트
    // ========================================

    @Test
    @Order(11)
    @DisplayName("년도별 차종 통계 테스트")
    void testGetCarTypeStats_Yearly() {
        // given
        int year = LocalDate.now().getYear();

        // when
        Map<String, Object> result = service.getCarTypeStats(year, null);

        // then
        assertNotNull(result, "결과는 null이면 안됨");
        assertTrue(result.containsKey("data"), "데이터 필요");
        assertTrue(result.containsKey("total"), "총 대수 필요");

        List<Map<String, Object>> pieData = (List<Map<String, Object>>) result.get("data");
        int total = (int) result.get("total");

        // 파이 차트 데이터 검증
        int sum = 0;
        for (Map<String, Object> data : pieData) {
            assertTrue(data.containsKey("name"), "차종명 필요");
            assertTrue(data.containsKey("y"), "값 필요");
            assertTrue(data.containsKey("percentage"), "비율 필요");
            sum += (int) data.get("y");
        }

        assertEquals(total, sum, "전체 합계가 일치해야 함");

        System.out.println("=== " + year + "년 차종별 통계 ===");
        System.out.println("총 대수: " + total);
        for (Map<String, Object> data : pieData) {
            System.out.printf("%s: %d대 (%s%%)%n",
                    data.get("name"), data.get("y"), data.get("percentage"));
        }
    }

    @Test
    @Order(12)
    @DisplayName("특정 월 차종 통계 테스트")
    void testGetCarTypeStats_Monthly() {
        // given
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        // when
        Map<String, Object> result = service.getCarTypeStats(year, month);

        // then
        assertNotNull(result);

        if ((int) result.get("total") > 0) {
            System.out.println("=== " + year + "년 " + month + "월 차종별 통계 ===");
            System.out.println("총 대수: " + result.get("total"));
        } else {
            System.out.println(year + "년 " + month + "월 데이터 없음");
        }
    }

    // ========================================
    // 6. 피크 시간대 분석 테스트
    // ========================================

    @Test
    @Order(13)
    @DisplayName("피크 시간대 분석 테스트")
    void testGetPeakTimeStats() {
        // given & when
        Map<String, Object> result = service.getPeakTimeStats();

        // then
        assertNotNull(result, "결과는 null이면 안됨");
        assertTrue(result.containsKey("categories"), "시간대 카테고리 필요");
        assertTrue(result.containsKey("hourlyCount"), "시간별 카운트 필요");

        List<String> categories = (List<String>) result.get("categories");
        List<Integer> hourlyCount = (List<Integer>) result.get("hourlyCount");

        assertEquals(24, categories.size(), "24시간 데이터 필요");
        assertEquals(24, hourlyCount.size(), "24시간 카운트 필요");

        // 피크 시간대 찾기
        int maxCount = Collections.max(hourlyCount);
        int peakHour = hourlyCount.indexOf(maxCount);

        System.out.println("=== 피크 시간대 분석 ===");
        System.out.println("피크 시간: " + peakHour + "시 (" + maxCount + "대)");
        System.out.println("시간대별 입차 현황:");
        for (int i = 0; i < 24; i++) {
            if (hourlyCount.get(i) > 0) {
                System.out.printf("%s: %d대%n", categories.get(i), hourlyCount.get(i));
            }
        }
    }

    // ========================================
    // 7. 엣지 케이스 테스트
    // ========================================

    @Test
    @Order(14)
    @DisplayName("존재하지 않는 년도 조회 테스트")
    void testGetMonthlySales_InvalidYear() {
        // given
        int invalidYear = 1999; // 데이터 없을 것으로 예상되는 년도

        // when
        Map<String, Object> result = service.getMonthlySales(invalidYear, null, true);

        // then
        assertTrue(result.isEmpty() || result.get("categories") == null,
                "존재하지 않는 년도는 빈 결과 또는 null");

        System.out.println("존재하지 않는 년도 테스트 통과");
    }

    @Test
    @Order(15)
    @DisplayName("존재하지 않는 월 조회 테스트")
    void testGetMonthlySales_InvalidMonth() {
        // given
        int year = LocalDate.now().getYear();
        int invalidMonth = 13; // 잘못된 월

        // when & then
        // 잘못된 월 입력 시 예외 발생 또는 빈 결과 반환
        assertDoesNotThrow(() -> {
            Map<String, Object> result = service.getMonthlySales(year, invalidMonth, true);
            System.out.println("잘못된 월 처리: " + result);
        });
    }

    // ========================================
    // 8. 통합 테스트
    // ========================================

    @Test
    @Order(16)
    @DisplayName("전체 API 통합 테스트")
    void testAllAPIs() {
        System.out.println("\n========== 전체 API 통합 테스트 시작 ==========\n");

        // 1. 전체 통계
        assertDoesNotThrow(() -> {
            Map<String, Object> result = service.getAllStatisticsData();
            System.out.println("✓ 전체 통계 조회 성공");
        });

        // 2. 회원 통계
        assertDoesNotThrow(() -> {
            Map<String, Object> result = service.getMemberStats();
            System.out.println("✓ 회원 통계 조회 성공");
        });

        // 3. 오늘 요약
        assertDoesNotThrow(() -> {
            Map<String, Object> result = service.getTodaySummary();
            System.out.println("✓ 오늘 요약 조회 성공");
        });

        // 4. 월별 매출
        int year = LocalDate.now().getYear();
        assertDoesNotThrow(() -> {
            Map<String, Object> result = service.getMonthlySales(year, null, true);
            System.out.println("✓ 월별 매출 조회 성공");
        });

        // 5. 누적 매출
        assertDoesNotThrow(() -> {
            Map<String, Object> result = service.getCumulativeSales(year, null, true);
            System.out.println("✓ 누적 매출 조회 성공");
        });

        // 6. 차종별 통계
        assertDoesNotThrow(() -> {
            Map<String, Object> result = service.getCarTypeStats(year, null);
            System.out.println("✓ 차종별 통계 조회 성공");
        });

        // 7. 피크 시간대
        assertDoesNotThrow(() -> {
            Map<String, Object> result = service.getPeakTimeStats();
            System.out.println("✓ 피크 시간대 조회 성공");
        });

        System.out.println("\n========== 전체 API 통합 테스트 완료 ==========\n");
    }
}