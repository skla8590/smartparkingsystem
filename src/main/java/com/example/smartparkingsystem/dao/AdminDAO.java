package com.example.smartparkingsystem.dao;

import com.example.smartparkingsystem.util.ConnectionUtil;
import com.example.smartparkingsystem.vo.AdminVO;
import lombok.Cleanup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {

    // TODO 필요한가?
    // 관리자 계정 조회
    public List<AdminVO> selectAllAdmin() {
        List<AdminVO> adminVOList = new ArrayList<>();

        String sql = "SELECT * FROM admin";
        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                AdminVO adminVO = AdminVO.builder()
                        .adminId(resultSet.getString("admin_id"))
                        .password(resultSet.getString("password"))
                        .adminName(resultSet.getString("admin_name"))
                        .adminEmail(resultSet.getString("admin_email"))
                        .isActive(resultSet.getBoolean("is_active"))
                        .lastLogin(resultSet.getObject("last_login", LocalDateTime.class))
                        .lastLoginIp(resultSet.getString("last_login_ip"))
                        .isPasswordReset(resultSet.getBoolean("is_password_reset"))
                        .createdAt(resultSet.getObject("created_at", LocalDateTime.class))
                        .build();
                adminVOList.add(adminVO);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return adminVOList;
    }

    // 아이디로 조회
    public AdminVO selectAdminById(String adminId) {
        String sql = "SELECT * FROM admin WHERE admin_id = ?";
        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, adminId);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return AdminVO.builder()
                        .adminId(resultSet.getString("admin_id"))
                        .password(resultSet.getString("password"))
                        .adminName(resultSet.getString("admin_name"))
                        .adminEmail(resultSet.getString("admin_email"))
                        .isActive(resultSet.getBoolean("is_active"))
                        .lastLogin(resultSet.getObject("last_login", LocalDateTime.class))
                        .lastLoginIp(resultSet.getString("last_login_ip"))
                        .isPasswordReset(resultSet.getBoolean("is_password_reset"))
                        .createdAt(resultSet.getObject("created_at", LocalDateTime.class))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // 정보 수정
    public void updateAdmin(AdminVO adminVO) {
        String sql = "UPDATE admin SET password = ?, admin_email = ?, is_password_reset = ? WHERE admin_id = ?";
        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, adminVO.getPassword());
            preparedStatement.setString(2, adminVO.getAdminEmail());
            preparedStatement.setBoolean(3, adminVO.isPasswordReset());
            preparedStatement.setString(4, adminVO.getAdminId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO 필요없을거같아서 주석 (제거?)
//    // 비밀번호 변경
//    public void updatePassword(String password, String adminId) {
//        String sql = "UPDATE admin SET password = ? WHERE admin_id = ?";
//        try {
//            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
//            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
//            preparedStatement.setString(1, password);
//            preparedStatement.setString(2, adminId);
//            preparedStatement.executeUpdate();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }

    // 로그 추가
    public void updateLog(String adminId, String lastLoginIp) {
        String sql = "UPDATE admin SET last_login = ?, last_login_ip = ? WHERE admin_id = ?";
        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, LocalDateTime.now());
            preparedStatement.setString(2, lastLoginIp);
            preparedStatement.setString(3, adminId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO 필요없을거같아서 주석 (제거?)
    // 계정 삭제
//    public void deleteAdmin(String admin_id) {
//        String sql = "DELETE FROM admin WHERE admin_id = ?";
//        try {
//            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
//            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
//            preparedStatement.setString(1, admin_id);
//            preparedStatement.executeUpdate();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
