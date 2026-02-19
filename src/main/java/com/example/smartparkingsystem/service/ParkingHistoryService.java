package com.example.smartparkingsystem.service;

import com.example.smartparkingsystem.dao.ParkingHistoryDAO;
import com.example.smartparkingsystem.dto.ParkingHistoryDTO;
import com.example.smartparkingsystem.util.MapperUtil;
import com.example.smartparkingsystem.vo.ParkingHistoryVO;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public enum ParkingHistoryService {
    INSTANCE;

    ParkingHistoryDAO parkingHistoryDAO;
    ModelMapper modelMapper;

    ParkingHistoryService() {
        parkingHistoryDAO = new ParkingHistoryDAO();
        modelMapper = MapperUtil.INSTANCE.getInstance();
    }

    /* 입차 등록 */
    public void registerEntry(ParkingHistoryDTO parkingHistoryDTO) {
        ParkingHistoryVO parkingHistoryVO = modelMapper.map(parkingHistoryDTO, ParkingHistoryVO.class);
        log.info(parkingHistoryVO);
        parkingHistoryDAO.insertEntry(parkingHistoryVO);
    }

    /* 기록 조회 */
    public ParkingHistoryDTO getParkingHistory(long parkNo) {
        ParkingHistoryVO parkingHistoryVO = parkingHistoryDAO.selectParkingHistory(parkNo);
        if (parkingHistoryVO == null) {
            throw new IllegalArgumentException(parkNo + " 주차 기록 없음");
        }
        return modelMapper.map(parkingHistoryVO, ParkingHistoryDTO.class);
    }

    /* 현재 주차 중인 기록 조회 */
    public List<ParkingHistoryDTO> getOccupied() {
        List<ParkingHistoryDTO> occupiedList = new ArrayList<>();
        for (ParkingHistoryVO vo : parkingHistoryDAO.selectOccupied()) {
            occupiedList.add(modelMapper.map(vo, ParkingHistoryDTO.class));
        }
        return occupiedList;
    }

    /* 차량의 최근 주차 조회 */
    public ParkingHistoryDTO getRecentParking(String carNum) {
        ParkingHistoryVO vo = parkingHistoryDAO.selectRecentParking(carNum);
        if (vo == null) return null;
        return modelMapper.map(vo, ParkingHistoryDTO.class);
    }

    /* 회원권 상태 변경 */
    public void changeIsMemberState(ParkingHistoryDTO parkingHistoryDTO) {
        parkingHistoryDAO.updateIsMember(modelMapper.map(parkingHistoryDTO, ParkingHistoryVO.class));
    }

    /* 출차 처리 */
    public void registerExit(ParkingHistoryDTO parkingHistoryDTO) {
        ParkingHistoryVO parkingHistoryVO = modelMapper.map(parkingHistoryDTO, ParkingHistoryVO.class);

        // DTO->VO 과정에서 null값 들어가는 오류 발생 시 DB에 저장된 값 불러와 다시 저장
        if (parkingHistoryVO.getEntryTime() == null) {
            ParkingHistoryVO dbVO = parkingHistoryDAO.selectParkingHistory(parkingHistoryVO.getParkNo());
            parkingHistoryVO = ParkingHistoryVO.builder()
                    .parkNo(dbVO.getParkNo())
                    .entryTime(dbVO.getEntryTime())
                    .build();
        }
        parkingHistoryDAO.updateExit(parkingHistoryVO);
    }

}
