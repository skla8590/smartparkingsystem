<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.smartparkingsystem.*" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="com.example.smartparkingsystem.dao.MembersDAO" %>
<%@ page import="com.example.smartparkingsystem.dto.MembersDTO" %>
<%@ page import="com.example.smartparkingsystem.vo.MembersVO" %>
<%@ page import="com.example.smartparkingsystem.service.MembersService" %>
<%
    MembersDTO membersDTO = MembersDTO.builder()
            .mno(Long.parseLong(request.getParameter("mno")))
            .carNum(request.getParameter("carNum"))
            .memberName(request.getParameter("memberName"))
            .memberPhone(request.getParameter("memberPhone"))
            .build();

    MembersService membersService = MembersService.INSTANCE;
    membersService.modifyMember(membersDTO);
%>