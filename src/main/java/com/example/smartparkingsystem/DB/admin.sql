CREATE TABLE IF NOT EXISTS `admin`
(
    `admin_id`            VARCHAR(20) PRIMARY KEY COMMENT '관리자 아이디',
    `password`            VARCHAR(60) NOT NULL COMMENT '관리자 비밀번호', -- BCrypt적용하니 글자수가 60개가 나옴; 널널하게 100으로 변경(상의예정)
    `admin_name`          VARCHAR(20)  NOT NULL COMMENT '관리자 이름',
#     `birth`         VARCHAR(6)  NOT NULL COMMENT '관리자 생년월일 6자리',
    `admin_email`         VARCHAR(50)  NOT NULL UNIQUE COMMENT '이메일',
    `is_active`           BOOLEAN  DEFAULT TRUE COMMENT '사용여부 True 사용중, False 사용중지',
    `last_login`          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 로그인 날짜',
    `last_login_ip`       VARCHAR(45)  NULL COMMENT '마지막 로그인 IP (보안용)',
    `is_password_reset` BOOLEAN  DEFAULT FALSE COMMENT '재설정후 최초 로그인 여부 True 최초, False 일반',
    `created_at`          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '계정 생성일'
) COMMENT '관리자';


-- admin: 1234, test: 1111, test1: test1
INSERT INTO admin
(admin_id, password, admin_name, admin_email, is_active, last_login, last_login_ip, created_at)
VALUES ('admin', '$2a$12$hvk0XVGUYQk2BwV4SZ9Sz.xCrkCOCgQ3KGCv.QI77JGdJZ9Ri2usW', '관리자', 'admin@naver.com', true, NULL,
        NULL, NOW()),
       ('test', '$2a$12$D0Tcf..4G2y8woY2HgB9veF.yjdoUeiI2yMymm8xVIOLB8yv7mjmO', 'Test', 'test@naver.com', false, NULL,
        NULL, NOW()),
       ('test1', '$2a$12$teiNyPb2nUP6vMA9aoQL/OeXC01FtycxknhHtm10CSj4SU/VqQIG6', 'Test1', 'test1@naver.com', true, NULL,
        NULL, NOW());

