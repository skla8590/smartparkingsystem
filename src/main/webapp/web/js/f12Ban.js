// 개발자도구 금지
(function () {
    // 우클릭 방지
    document.addEventListener('contextmenu', e => e.preventDefault());

    // F12, Ctrl+Shift+I, Ctrl+Shift+J, Ctrl+U 차단
    document.addEventListener('keydown', function (e) {
        if (e.key === 'F12' ||
            (e.ctrlKey && e.shiftKey && e.key === 'I') ||
            (e.ctrlKey && e.shiftKey && e.key === 'J') ||
            (e.ctrlKey && e.key === 'U')) {
            e.preventDefault();
            return false;
        }
    });
})();