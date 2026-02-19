package com.example.smartparkingsystem.dao;

import com.example.smartparkingsystem.vo.ParkingHistoryVO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
class ParkingHistoryDAOTest {
    private ParkingHistoryDAO parkingHistoryDAO;

    @BeforeEach
    public void ready() {
        parkingHistoryDAO = new ParkingHistoryDAO();
    }

    @Test
    void insertEntryTest() {
        ParkingHistoryVO parkingHistoryVO = ParkingHistoryVO.builder()
                .parkingArea("A-7")
                .carNum("11가1190")
                .carType("일반").build();
        log.info(parkingHistoryVO);
        parkingHistoryDAO.insertEntry(parkingHistoryVO);
    }

    @Test
    void updateIsMember() {
        long park_no = 3;
        ParkingHistoryVO updateVO = parkingHistoryDAO.selectParkingHistory(park_no);
        parkingHistoryDAO.updateIsMember(updateVO);

        park_no = 5;
        ParkingHistoryVO nonUpdateVO = parkingHistoryDAO.selectParkingHistory(park_no);
        parkingHistoryDAO.updateIsMember(nonUpdateVO);
    }

    @Test
    void selectOccupiedTest() {
        for (ParkingHistoryVO vo : parkingHistoryDAO.selectOccupied()) {
            log.info(vo);
        }
    }

    @Test
    void selectParkingHistoryTest() {
        long parkNo = 1;
        log.info(parkingHistoryDAO.selectParkingHistory(parkNo));
    }

    @Test
    void selectRecentParkingTest() {
        String carNum = "11가1001";
        log.info(parkingHistoryDAO.selectRecentParking(carNum));
    }

    @Test
    void updateExitTest() {
        long parkNo = 1;
        ParkingHistoryVO parkingHistoryVO = parkingHistoryDAO.selectParkingHistory(parkNo);
        parkingHistoryDAO.updateExit(parkingHistoryVO);
    }
}