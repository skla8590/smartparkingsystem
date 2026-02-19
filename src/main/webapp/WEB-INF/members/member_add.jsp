<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.smartparkingsystem.*" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="com.example.smartparkingsystem.dao.MembersDAO" %>
<%@ page import="com.example.smartparkingsystem.dto.MembersDTO" %>
<%@ page import="com.example.smartparkingsystem.vo.MembersVO" %>
<%@ page import="com.example.smartparkingsystem.service.MembersService" %>
<%-- 신규 회원 등록 --%>
<%
    MembersService membersService = MembersService.INSTANCE;

    String carNum = request.getParameter("carNum");
    String memberName = request.getParameter("memberName");
    String memberPhone = request.getParameter("memberPhone");
    String startDate = request.getParameter("startDate");
    String endDate = request.getParameter("endDate");

    // 시작일
    LocalDate start = (startDate != null && !startDate.isEmpty())
            ? LocalDate.parse(startDate) : LocalDate.now();
    // 만료일 (시작일 기준 +30일 자동 설정)
    LocalDate end = (endDate != null && !endDate.isEmpty())
            ? LocalDate.parse(endDate) : start.plusDays(30);

    MembersDTO member = MembersDTO.builder()
            .carNum(carNum)
            .memberName(memberName)
            .memberPhone(memberPhone)
            .startDate(LocalDate.parse(startDate))
            .endDate(LocalDate.parse(endDate))
            .build();

    membersService.addMember(member);
    return;
%>