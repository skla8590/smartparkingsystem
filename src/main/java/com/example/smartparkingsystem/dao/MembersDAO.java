package com.example.smartparkingsystem.dao;

import com.example.smartparkingsystem.util.ConnectionUtil;
import com.example.smartparkingsystem.vo.MembersVO;
import lombok.Cleanup;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MembersDAO {
    // 신규 회원 등록
    public void insertMember(MembersVO membersVO) {
        String sql = "INSERT INTO members (car_num, member_name, member_phone, start_date, end_date, member_charge) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, membersVO.getCarNum());
            preparedStatement.setString(2, membersVO.getMemberName());
            preparedStatement.setString(3, membersVO.getMemberPhone());
            preparedStatement.setDate(4, Date.valueOf(membersVO.getStartDate()));
            preparedStatement.setDate(5, Date.valueOf(membersVO.getEndDate()));
            preparedStatement.setInt(6, membersVO.getMemberCharge());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 회원 정보 수정
    public void updateMember(MembersVO membersVO) {
        String sql = "UPDATE members SET car_num = ?, member_name = ?, member_phone = ? WHERE mno = ?";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, membersVO.getCarNum());
            preparedStatement.setString(2, membersVO.getMemberName());
            preparedStatement.setString(3, membersVO.getMemberPhone());
            preparedStatement.setLong(4, membersVO.getMno());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 전체 회원 목록 조회
    public List<MembersVO> selectAllMembers() {
        List<MembersVO> membersVOList = new ArrayList<>();

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM members");
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                MembersVO membersVO = MembersVO.builder()
                        .mno(resultSet.getLong("mno"))
                        .carNum(resultSet.getString("car_num"))
                        .memberName(resultSet.getString("member_name"))
                        .memberPhone(resultSet.getString("member_phone"))
                        .startDate(resultSet.getDate("start_date").toLocalDate())
                        .endDate(resultSet.getDate("end_date").toLocalDate())
                        .memberCharge(resultSet.getInt("member_charge"))
                        .build();

                membersVOList.add(membersVO);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return membersVOList;
    }

    // 특정 회원 목록 전체 조회
    public MembersVO selectOneMember(String carNum) {
        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM members WHERE car_num = ?");
            preparedStatement.setString(1, carNum);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                MembersVO membersVO = MembersVO.builder()
                        .mno(resultSet.getLong("mno"))
                        .carNum(resultSet.getString("car_num"))
                        .memberName(resultSet.getString("member_name"))
                        .memberPhone(resultSet.getString("member_phone"))
                        .startDate(resultSet.getDate("start_date").toLocalDate())
                        .endDate(resultSet.getDate("end_date").toLocalDate())
                        .memberCharge(resultSet.getInt("member_charge"))
                        .build();

                return membersVO;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // 특정 회원 목록 중 가장 최근 등록된 1건 조회
    public MembersVO selectMemberByCarNum(String carNum) {
        String sql = "SELECT * FROM members WHERE car_num = ? ORDER BY end_date DESC LIMIT 1";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, carNum);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                MembersVO membersVO = MembersVO.builder()
                        .mno(resultSet.getLong("mno"))
                        .carNum(resultSet.getString("car_num"))
                        .memberName(resultSet.getString("member_name"))
                        .memberPhone(resultSet.getString("member_phone"))
                        .startDate(resultSet.getDate("start_date").toLocalDate())
                        .endDate(resultSet.getDate("end_date").toLocalDate())
                        .memberCharge(resultSet.getInt("member_charge"))
                        .build();

                return membersVO;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // 전체 회원 목록 조회
    public List<MembersVO> selectIsMembers() {
        List<MembersVO> membersVOList = new ArrayList<>();

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM members WHERE end_date >= CURDATE()");
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                MembersVO membersVO = MembersVO.builder()
                        .mno(resultSet.getLong("mno"))
                        .carNum(resultSet.getString("car_num"))
                        .memberName(resultSet.getString("member_name"))
                        .memberPhone(resultSet.getString("member_phone"))
                        .startDate(resultSet.getDate("start_date").toLocalDate())
                        .endDate(resultSet.getDate("end_date").toLocalDate())
                        .memberCharge(resultSet.getInt("member_charge"))
                        .build();

                membersVOList.add(membersVO);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return membersVOList;
    } // 필요 없으면 삭제해도 될 것 같음

    // 전체 만료 회원 목록 조회
    public List<MembersVO> selectIsNotMembers() {
        List<MembersVO> membersVOList = new ArrayList<>();

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM members WHERE end_date < CURDATE()");
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                MembersVO membersVO = MembersVO.builder()
                        .mno(resultSet.getLong("mno"))
                        .carNum(resultSet.getString("car_num"))
                        .memberName(resultSet.getString("member_name"))
                        .memberPhone(resultSet.getString("member_phone"))
                        .startDate(resultSet.getDate("start_date").toLocalDate())
                        .endDate(resultSet.getDate("end_date").toLocalDate())
                        .memberCharge(resultSet.getInt("member_charge"))
                        .build();

                membersVOList.add(membersVO);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return membersVOList;
    } // 필요 없으면 삭제해도 될 것 같음

    // 회원 목록 전체 개수
    public int selectMemberCount(String searchType, String keyword, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM members WHERE 1=1");

        // 검색 키워드 존재 여부
        boolean isSearch = keyword != null && !keyword.isEmpty() && searchType != null && !searchType.isEmpty();

        // 검색 키워드가 있으면 키워드 필터 추가 (회원 + 비회원 모두 조회)
        if (isSearch) {
            String column;
            switch (searchType) {
                case "carNum":
                    column = "car_num";
                    break;
                case "memberName":
                    column = "member_name";
                    break;
                case "memberPhone":
                    column = "member_phone";
                    break;
                default:
                    throw new IllegalArgumentException("잘못된 검색 타입");
            }
            sql.append(" AND ").append(column).append(" LIKE ? ");
        } else {
            // 검색 키워드가 없으면 status 필터 적용 (회원 or 비회원 하나만 조회)
            if (status != null) {
                if ("expired".equals(status)) {
                    sql.append(" AND end_date < CURDATE()");
                } else if ("active".equals(status)) {
                    sql.append(" AND end_date >= CURDATE()");
                }
            }
        }

        int count = 0;

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
            if (isSearch) {
                preparedStatement.setString(1, "%" + keyword + "%");
            }
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return count;
    }

    // 회원 목록 페이징
    public List<MembersVO> selectMemberList(String searchType, String keyword, String status, int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM members WHERE 1=1");

        boolean isSearch = keyword != null && !keyword.isEmpty() && searchType != null && !searchType.isEmpty();

        if (isSearch) {
            String column;
            switch (searchType) {
                case "carNum":
                    column = "car_num";
                    break;
                case "memberName":
                    column = "member_name";
                    break;
                case "memberPhone":
                    column = "member_phone";
                    break;
                default:
                    throw new IllegalArgumentException("잘못된 검색 타입");
            }
            sql.append(" AND ").append(column).append(" LIKE ? ");
        } else {
            if (status != null) {
                if ("expired".equals(status)) {
                    sql.append(" AND end_date < CURDATE()");
                } else {
                    sql.append(" AND end_date >= CURDATE()");
                }
            }
        }

        sql.append(" ORDER BY mno DESC LIMIT ?, ?");

        List<MembersVO> membersVOList = new ArrayList<>();

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());

            int index = 1;

            if (isSearch) {
                preparedStatement.setString(index++, "%" + keyword + "%");
            }
            preparedStatement.setInt(index++, offset);
            preparedStatement.setInt(index, limit);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                MembersVO membersVO = MembersVO.builder()
                        .mno(resultSet.getLong("mno"))
                        .carNum(resultSet.getString("car_num"))
                        .memberName(resultSet.getString("member_name"))
                        .memberPhone(resultSet.getString("member_phone"))
                        .startDate(resultSet.getDate("start_date").toLocalDate())
                        .endDate(resultSet.getDate("end_date").toLocalDate())
                        .memberCharge(resultSet.getInt("member_charge"))
                        .build();
                membersVOList.add(membersVO);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return membersVOList;
    }
}
