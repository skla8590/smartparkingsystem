// 로딩창 표시
window.showLoading = function () {
    const loadingHTML = `
        <div id="loading" style="
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 9999;
        ">
            <div class="spinner-border text-light" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
        </div>
    `;
    // 'beforeend' = 요소 안 제일 마지막 자식 뒤에 로딩을 넣음(footer 아래에 추가)
    document.body.insertAdjacentHTML('beforeend', loadingHTML);
}

// 로딩창 숨기기
window.hideLoading = function () {
    const loading = document.getElementById('loading');
    if (loading) {
        loading.remove();
    }
}