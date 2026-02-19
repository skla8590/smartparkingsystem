<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String status = (String) request.getAttribute("status");
    String id = (String) request.getAttribute("id");
    String car = (String) request.getAttribute("car");
    Object timeObj = request.getAttribute("time");
    String carType = (String) request.getAttribute("type");
    Object parkNo = request.getAttribute("parkNo");
    Object isMember = request.getAttribute("isMember");

    boolean isOccupied = "occupied".equals(status);
    String carNum = isOccupied ? car : "사용\n가능";

    String timer = "";
    if (isOccupied && timeObj != null) {
        timer = timeObj.toString().replace(" ", "T");
    }
%>
<div class="parking-card <%=status%>"
     data-status="<%=status%>"
     data-id="<%=id%>"
     data-car-num="<%=isOccupied ? car : ""%>"
     data-in-full-time="<%=timer%>"
     data-car-type="<%=carType != null ? carType : ""%>"
     data-park-no="<%=parkNo != null ? parkNo : ""%>"
     data-is-member="<%=isMember != null ? isMember : "false"%>">


    <!-- 출력용 -->
    <div class="box-id"><%=id%>
    </div>
    <div class="box-car"><%=carNum%>
    </div>
    <div class="box-time"><%=isOccupied ? "00:00" : ""%>
    </div>
</div>