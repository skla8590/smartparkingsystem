<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
    <div class="container-fluid">
        <a class="navbar-brand" href="../main/main.jsp"><b>스마트 주차 관리 시스템</b></a>

        <div class="collapse navbar-collapse">
            <ul class="navbar-nav me-auto">
                <li class="nav-item"><a class="nav-link active" href="../main/main.jsp">대시보드</a></li>
                <li class="nav-item"><a class="nav-link" href="../member/member.jsp">회원 관리</a></li>
                <li class="nav-item"><a class="nav-link" href="../setting/setting.jsp">설정 관리</a></li>
                <li class="nav-item"><a class="nav-link" href="../statistic/statistic.jsp">통계</a></li>
            </ul>
        </div>

        <span class="navbar-text">
        <ul class="navbar-nav d-flex flex-row gap-3 p-0 m-0" style="align-items: center;">
            <li class="nav-item">
                <a href="/main/mypage" class="text-white text-decoration-none">MyPage</a>
            </li>
            <li class="nav-item">
                <a href="/logout" class="text-white text-decoration-none" onclick="return logout()">로그아웃</a>
            </li>
        </ul>
    </span>
    </div>
</nav>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        // 1. 브라우저 전체 경로 들고오기
        const currentPath = window.location.pathname;

        // 2. 네비게이션 링크 들고오기
        const navLinks = document.querySelectorAll('.navbar-nav .nav-link');

        navLinks.forEach(link => {
            // 3. 링크의 href 속성값이 현재 경로에 포함되어 있는지 확인
            if (currentPath.includes(link.getAttribute('href').split('/').pop())) {
                // 4. 기존 active 클래스 제거 후 현재 메뉴에 추가
                navLinks.forEach(item => item.classList.remove('active'));
                link.classList.add('active');
            }
        });
    });

    function logout() {
        return confirm('로그아웃 하시겠습니까?');
    }
</script>
