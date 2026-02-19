package com.example.smartparkingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyData { // 월별로 데이터 나누기
    private int month;
    private List<PaymentHistoryDTO> records;
}
