package com.example.smartparkingsystem.service;

import com.example.smartparkingsystem.dao.PaymentInfoDAO;

import com.example.smartparkingsystem.dto.PaymentInfoDTO;
import com.example.smartparkingsystem.util.MapperUtil;
import com.example.smartparkingsystem.vo.PaymentInfoVO;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;

@Log4j2
public class PaymentInfoService {
    private final PaymentInfoDAO paymentInfoDAO = new PaymentInfoDAO();
    private final ModelMapper modelMapper = MapperUtil.INSTANCE.getInstance();

    private static PaymentInfoService instance;

    private PaymentInfoService () {}

    public static PaymentInfoService getInstance() {
        if (instance == null) {
            instance = new PaymentInfoService();
        }
        return instance;
    }

    // setting 등록
    public void addInfo(PaymentInfoDTO paymentInfoDTO) {
        log.info(paymentInfoDTO);
        PaymentInfoVO paymentInfoVO = modelMapper.map(paymentInfoDTO, PaymentInfoVO.class);
        paymentInfoDAO.insertInfo(paymentInfoVO);
    }

    // setting 조회
    public PaymentInfoDTO getInfo() {
        return modelMapper.map(paymentInfoDAO.selectInfo(), PaymentInfoDTO.class);
    }
}
