package com.example.smartparkingsystem.initDBData;

import com.example.smartparkingsystem.dao.ParkingHistoryDAO;
import com.example.smartparkingsystem.vo.ParkingHistoryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class initParkingHistoryData {
    private ParkingHistoryDAO parkingHistoryDAO;

    @BeforeEach
    public void ready() {
        parkingHistoryDAO = new ParkingHistoryDAO();
    }

    @Test
    void insertDummyData() {
        Random random = new Random();

        for (int year = 2024; year <= 2025; year++) {
            int monthlyCount = (year == 2024) ? 10 : 20;
            for (int month = 1; month <= 12; month++) {
                for (int i = 0; i < monthlyCount; i++) {
                    // 주차 시간 랜덤 설정
                    LocalDateTime entryTime = LocalDateTime.of(year, month,
                            random.nextInt(28) + 1, random.nextInt(24), random.nextInt(60));
                    int totalMinutes = random.nextInt(2850) + 30;
                    LocalDateTime exitTime = entryTime.plusMinutes(totalMinutes);

                    ParkingHistoryVO parkingHistoryVO = ParkingHistoryVO.builder()
                            .parkingArea("A-" + (random.nextInt(20) + 1))
                            .carNum((random.nextInt(90) + 10) + "가" + (1000 + random.nextInt(9000)))
                            .carType(new String[]{"일반", "경차", "장애인"}[random.nextInt(3)])
                            .entryTime(entryTime).exitTime(exitTime).totalMinutes(totalMinutes).build();

                    parkingHistoryDAO.insertTestData(parkingHistoryVO);
                }
            }
        }

        int monthlyCount = 20;
        for (int i = 0; i < monthlyCount; i++) {
            // 주차 시간 랜덤 설정
            LocalDateTime entryTime = LocalDateTime.of(2026, 1,
                    random.nextInt(28) + 1, random.nextInt(24), random.nextInt(60));
            int totalMinutes = random.nextInt(2850) + 30;
            LocalDateTime exitTime = entryTime.plusMinutes(totalMinutes);

            ParkingHistoryVO parkingHistoryVO = ParkingHistoryVO.builder()
                    .parkingArea("A-" + (random.nextInt(20) + 1))
                    .carNum((random.nextInt(90) + 10) + "가" + (1000 + random.nextInt(9000)))
                    .carType(new String[]{"일반", "경차", "장애인"}[random.nextInt(3)])
                    .entryTime(entryTime).exitTime(exitTime).totalMinutes(totalMinutes).build();

            parkingHistoryDAO.insertTestData(parkingHistoryVO);
        }
    }
}