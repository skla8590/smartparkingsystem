CREATE TABLE IF NOT EXISTS `validation`
(
    `no`           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '인덱스',
    `admin_id`     VARCHAR(20) NOT NULL COMMENT 'FK 관리자 아이디',
    `otp_code`     CHAR(6)     NOT NULL COMMENT 'OTP',
    `admin_email`  VARCHAR(50) NOT NULL COMMENT '관리자 이메일 (OTP보낸 이메일)',
    `expired_time` DATETIME    NOT NULL COMMENT '만료시간',
    CONSTRAINT fk_admin_id_otp
        FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`)
)
    COMMENT 'OTP 로그';