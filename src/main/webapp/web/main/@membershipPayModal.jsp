<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="modal fade" id="membershipPayModal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static">
  <div class="modal-dialog">
    <div class="modal-content" style="border-radius: 20px; overflow: hidden;">
      <div class="modal-header bg-primary text-white" style="padding: 1rem 1.25rem;">
        <h5 class="modal-title fw-bold fs-7">회원권 신규/연장 결제</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>

      <div class="modal-body" style="padding: 1rem 1.25rem;">
        <div id="mem-input-section">
          <div class="mb-4">
            <label class="info-label" style="font-size: 1.25rem; font-weight: 700; display: block; margin-bottom: 12px;">차량 번호</label>
            <div class="input-group">
              <input type="text" id="mem-carNum" class="form-control form-control-lg" style="border-radius: 12px 0 0 12px; border: 2px solid #eee;">
              <button class="btn btn-outline-primary fw-bold" type="button" id="btn-check-member" style="border-radius: 0 12px 12px 0; border: 2px solid #0d6efd; border-left: none;">회원 확인</button>
            </div>
          </div>

          <div class="row">
            <div class="col-6 mb-4">
              <label class="info-label" style="font-size: 1.25rem; font-weight: 700; display: block; margin-bottom: 12px;">이름</label>
              <input type="text" id="mem-name" class="form-control form-control-lg" style="border-radius: 12px; border: 2px solid #eee;">
            </div>
            <div class="col-6 mb-4">
              <label class="info-label" style="font-size: 1.25rem; font-weight: 700; display: block; margin-bottom: 12px;">연락처</label>
              <input type="text" id="mem-phone" class="form-control form-control-lg" style="border-radius: 12px; border: 2px solid #eee;">
            </div>
          </div>

          <div class="row">
            <div class="col-6 mb-4">
              <label class="info-label" style="font-size: 1.25rem; font-weight: 700; display: block; margin-bottom: 12px;">시작일</label>
              <input type="date" id="mem-startDate" class="form-control form-control-lg" readonly style="border-radius: 12px; border: 1px solid #ddd; background-color: #f8f9fa;">
            </div>
            <div class="col-6 mb-4">
              <label class="info-label" style="font-size: 1.25rem; font-weight: 700; display: block; margin-bottom: 12px;">만료일</label>
              <input type="date" id="mem-endDate" class="form-control form-control-lg" readonly style="border-radius: 12px; border: 1px solid #ddd; background-color: #f8f9fa;">
            </div>
          </div>

          <div class="mb-2" style="display: flex; justify-content: space-between; align-items: baseline; border-top: 1px solid #f2f2f2; padding-top: 20px; margin-top: 20px;">
            <label style="font-size: 1.1rem; font-weight: 700; color: #333; margin: 0; flex-shrink: 0;">총 결제 요금</label>

            <div style="flex-grow: 1; text-align: right;">
              <input type="text" id="mem-price" value="100,000원" readonly
                     style="border: none; background: transparent; font-size: 1.3rem; font-weight: 800; color: #0d6efd; text-align: right; padding: 0; width: 100%;">
            </div>
          </div>
        </div>

        <div id="mem-receipt-section" style="display: none;">
          <div class="p-4 border border-2 border-dark rounded bg-light" style="border-style: dashed !important; background-color: #fff !important;">
            <h4 class="text-center fw-bold mb-4">회원권 결제 영수증</h4>
            <div class="d-flex justify-content-between mb-3 fs-5">
              <span>차량번호:</span> <span id="res-car" class="fw-bold"></span>
            </div>
            <div class="d-flex justify-content-between mb-3">
              <span>성함/연락처:</span> <span id="res-user" class="fw-bold"></span>
            </div>
            <hr>
            <div class="d-flex justify-content-between mb-3">
              <span>권종:</span> <span>정기 정액권 (30일)</span>
            </div>
            <div class="d-flex justify-content-between mb-3">
              <span>유효기간:</span> <span id="res-period" class="text-primary fw-bold"></span>
            </div>
            <hr>
            <div class="d-flex justify-content-between fs-4 fw-bold">
              <span>결제금액:</span> <span id="res-price" class="text-danger"></span>
            </div>
          </div>
          <div class="mt-4 d-flex gap-2">
            <button class="btn btn-dark py-3 w-100 fw-bold" style="border-radius: 12px;" onclick="window.print()">영수증 출력</button>
            <button type="button" id="btn-receipt-close-final" class="btn btn-secondary py-3 w-100 fw-bold" style="border-radius: 12px;">닫기</button>
          </div>
        </div>
      </div>

      <div class="modal-footer" id="mem-footer" style="padding: 0 2rem 2.5rem 2rem; border-top: none;">
        <button type="button" class="btn btn-secondary py-3 px-4 fw-bold d-print-none" data-bs-dismiss="modal" style="border-radius: 12px;">취소</button>
        <button type="button" class="btn btn-primary py-3 px-5 fw-bold d-print-none" id="btn-membership-submit" style="border-radius: 12px;">결제하기</button>
      </div>
    </div>
  </div>
</div>