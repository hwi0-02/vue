<template>
  <div class="payment-management">
    <div class="page-header">
      <h1>결제 관리</h1>
      <p>결제 내역을 모니터링하고 환불을 처리합니다.</p>
    </div>
    <div class="placeholder">
      <p>Phase 5에서 구현 예정입니다.</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PaymentManagement'
}
</script>

<style scoped>
.payment-management {
  padding: 20px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  color: #333;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.placeholder {
  margin-top: 40px;
  text-align: center;
  color: #666;
}
</style>