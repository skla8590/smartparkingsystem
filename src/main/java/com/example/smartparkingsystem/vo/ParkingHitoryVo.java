    package com.example.smartparkingsystem.vo;


    import lombok.*;

    @Getter
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public class ParkingHitoryVo {
        private int park_no;
        private String parking_area;
        private String car_num;
        private String car_type;
        private boolean is_member;
        private String entry_time;
        private String exit_time;
        private int total_minutes;
    }
