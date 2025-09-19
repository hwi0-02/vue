<template>
  <div class="coupon-management">
    <div class="page-header">
      <h1>쿠폰 관리</h1>
      <p class="page-description">쿠폰을 생성, 수정, 삭제합니다.</p>
    </div>
    <!-- 상단 카드: 2열 레이아웃 -->
    <div class="summary-cards">
      <div class="card">
        <div class="card-content">
          <h3>전체 쿠폰</h3>
          <p class="card-number">—</p>
        </div>
      </div>
      <div class="card">
        <div class="card-content">
          <h3>사용 가능</h3>
          <p class="card-number">—</p>
        </div>
      </div>
    </div>
    <div class="placeholder">
      <p>Phase 7에서 구현 예정입니다.</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CouponManagement'
}
</script>

<style scoped>
.coupon-management {
  padding: 20px;
}

.placeholder {
  margin-top: 40px;
  text-align: center;
  color: #666;
}

/* 2열 카드 레이아웃 */
.summary-cards { margin-top: 20px; }

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.08);
}

.card-content h3 {
  margin: 0 0 6px 0;
  font-size: 14px;
  color: #7f8c8d;
}

.card-number {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
}

@media (max-width: 768px) {}
</style>