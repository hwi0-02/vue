<template>
  <div class="reservation-monitoring">
    <div class="page-header">
      <h1>예약 관리</h1>
      <p>호텔 예약 및 결제 상태를 모니터링하고 관리합니다.</p>
    </div>

    <!-- 검색 및 필터 -->
    <div class="filters">
      <div class="filter-row">
        <input 
          v-model="filters.hotelName" 
          type="text" 
          placeholder="호텔명 검색"
          class="filter-input"
        />
        <input 
          v-model="filters.userName" 
          type="text" 
          placeholder="예약자명 검색"
          class="filter-input"
        />
        <select v-model="filters.reservationStatus" class="filter-select">
          <option value="">예약 상태 전체</option>
          <option value="PENDING">대기중</option>
          <option value="CONFIRMED">확정</option>
          <option value="CHECKED_IN">체크인</option>
          <option value="CHECKED_OUT">체크아웃</option>
          <option value="CANCELLED">취소</option>
          <option value="NO_SHOW">노쇼</option>
        </select>
        <select v-model="filters.paymentStatus" class="filter-select">
          <option value="">결제 상태 전체</option>
          <option value="PENDING">결제대기</option>
          <option value="COMPLETED">결제완료</option>
          <option value="FAILED">결제실패</option>
          <option value="CANCELLED">결제취소</option>
          <option value="REFUNDED">환불완료</option>
        </select>
        <button @click="searchReservations" class="search-btn">검색</button>
        <button @click="resetFilters" class="reset-btn">초기화</button>
      </div>
    </div>

    <!-- 통계 카드 -->
    <div class="stats-grid">
      <div class="stat-card">
        <h3>전체 예약</h3>
        <p class="stat-number">{{ totalReservations }}</p>
      </div>
      <div class="stat-card">
        <h3>확정 예약</h3>
        <p class="stat-number confirmed">{{ confirmedReservations }}</p>
      </div>
      <div class="stat-card">
        <h3>취소 예약</h3>
        <p class="stat-number cancelled">{{ cancelledReservations }}</p>
      </div>
      <div class="stat-card">
        <h3>결제 완료</h3>
        <p class="stat-number paid">{{ paidReservations }}</p>
      </div>
    </div>

    <!-- 예약 목록 테이블 -->
    <div class="table-container">
      <table class="reservation-table">
        <thead>
          <tr>
            <th>예약번호</th>
            <th>호텔명</th>
            <th>객실</th>
            <th>예약자</th>
            <th>체크인</th>
            <th>체크아웃</th>
            <th>예약금액</th>
            <th>예약상태</th>
            <th>결제상태</th>
            <th>예약일시</th>
            <th>액션</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="reservation in reservations" :key="reservation.reservationId">
            <td>{{ reservation.reservationId }}</td>
            <td>{{ reservation.hotelName }}</td>
            <td>{{ reservation.roomName }} ({{ reservation.roomType }})</td>
            <td>
              <div>
                <div>{{ reservation.userName }}</div>
                <small>{{ reservation.userEmail }}</small>
              </div>
            </td>
            <td>{{ formatDate(reservation.checkInDate) }}</td>
            <td>{{ formatDate(reservation.checkOutDate) }}</td>
            <td>{{ formatCurrency(reservation.totalAmount) }}</td>
            <td>
              <span :class="['status-badge', getReservationStatusClass(reservation.reservationStatus)]">
                {{ getReservationStatusText(reservation.reservationStatus) }}
              </span>
            </td>
            <td>
              <span :class="['status-badge', getPaymentStatusClass(reservation.paymentStatus)]">
                {{ getPaymentStatusText(reservation.paymentStatus) }}
              </span>
            </td>
            <td>{{ formatDateTime(reservation.reservationCreatedAt) }}</td>
            <td>
              <button @click="viewReservationDetail(reservation)" class="action-btn view-btn">
                상세보기
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 빈 데이터 메시지 -->
      <div v-if="reservations.length === 0" class="empty-message">
        <p>조건에 맞는 예약이 없습니다.</p>
      </div>
    </div>

    <!-- 페이지네이션 -->
    <div class="pagination" v-if="pagination.totalPages > 1">
      <button 
        @click="changePage(pagination.currentPage - 1)"
        :disabled="pagination.currentPage === 0"
        class="page-btn"
      >
        이전
      </button>
      
      <span class="page-info">
        {{ pagination.currentPage + 1 }} / {{ pagination.totalPages }}
      </span>
      
      <button 
        @click="changePage(pagination.currentPage + 1)"
        :disabled="pagination.currentPage >= pagination.totalPages - 1"
        class="page-btn"
      >
        다음
      </button>
    </div>

    <!-- 예약 상세 모달 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>예약 상세 정보</h2>
          <button @click="closeDetailModal" class="close-btn">×</button>
        </div>
        
        <div class="modal-body" v-if="selectedReservation">
          <div class="detail-grid">
            <!-- 예약 정보 -->
            <div class="detail-section">
              <h3>예약 정보</h3>
              <div class="detail-item">
                <label>예약번호:</label>
                <span>{{ selectedReservation.reservationId }}</span>
              </div>
              <div class="detail-item">
                <label>예약상태:</label>
                <span :class="['status-badge', getReservationStatusClass(selectedReservation.reservationStatus)]">
                  {{ getReservationStatusText(selectedReservation.reservationStatus) }}
                </span>
              </div>
              <div class="detail-item">
                <label>체크인:</label>
                <span>{{ formatDate(selectedReservation.checkInDate) }}</span>
              </div>
              <div class="detail-item">
                <label>체크아웃:</label>
                <span>{{ formatDate(selectedReservation.checkOutDate) }}</span>
              </div>
              <div class="detail-item">
                <label>투숙인원:</label>
                <span>{{ selectedReservation.guestCount }}명</span>
              </div>
              <div class="detail-item">
                <label>총 금액:</label>
                <span>{{ formatCurrency(selectedReservation.totalAmount) }}</span>
              </div>
              <div class="detail-item" v-if="selectedReservation.specialRequests">
                <label>특별요청:</label>
                <span>{{ selectedReservation.specialRequests }}</span>
              </div>
              <div class="detail-item">
                <label>예약일시:</label>
                <span>{{ formatDateTime(selectedReservation.reservationCreatedAt) }}</span>
              </div>
            </div>

            <!-- 결제 정보 -->
            <div class="detail-section">
              <h3>결제 정보</h3>
              <div class="detail-item" v-if="selectedReservation.paymentId">
                <label>결제번호:</label>
                <span>{{ selectedReservation.paymentId }}</span>
              </div>
              <div class="detail-item">
                <label>결제상태:</label>
                <span :class="['status-badge', getPaymentStatusClass(selectedReservation.paymentStatus)]">
                  {{ getPaymentStatusText(selectedReservation.paymentStatus) }}
                </span>
              </div>
              <div class="detail-item" v-if="selectedReservation.paymentMethod">
                <label>결제수단:</label>
                <span>{{ getPaymentMethodText(selectedReservation.paymentMethod) }}</span>
              </div>
              <div class="detail-item" v-if="selectedReservation.paidAmount">
                <label>결제금액:</label>
                <span>{{ formatCurrency(selectedReservation.paidAmount) }}</span>
              </div>
              <div class="detail-item" v-if="selectedReservation.paymentCreatedAt">
                <label>결제일시:</label>
                <span>{{ formatDateTime(selectedReservation.paymentCreatedAt) }}</span>
              </div>
            </div>

            <!-- 고객 정보 -->
            <div class="detail-section">
              <h3>고객 정보</h3>
              <div class="detail-item">
                <label>고객번호:</label>
                <span>{{ selectedReservation.userId }}</span>
              </div>
              <div class="detail-item">
                <label>고객명:</label>
                <span>{{ selectedReservation.userName }}</span>
              </div>
              <div class="detail-item">
                <label>이메일:</label>
                <span>{{ selectedReservation.userEmail }}</span>
              </div>
            </div>

            <!-- 호텔/객실 정보 -->
            <div class="detail-section">
              <h3>호텔/객실 정보</h3>
              <div class="detail-item">
                <label>호텔번호:</label>
                <span>{{ selectedReservation.hotelId }}</span>
              </div>
              <div class="detail-item">
                <label>호텔명:</label>
                <span>{{ selectedReservation.hotelName }}</span>
              </div>
              <div class="detail-item">
                <label>객실번호:</label>
                <span>{{ selectedReservation.roomId }}</span>
              </div>
              <div class="detail-item">
                <label>객실명:</label>
                <span>{{ selectedReservation.roomName }}</span>
              </div>
              <div class="detail-item">
                <label>객실타입:</label>
                <span>{{ selectedReservation.roomType }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'

export default {
  name: 'ReservationMonitoring',
  setup() {
    const reservations = ref([])
    const selectedReservation = ref(null)
    const showDetailModal = ref(false)
    
    // 필터 상태
    const filters = reactive({
      hotelName: '',
      userName: '',
      reservationStatus: '',
      paymentStatus: ''
    })

    // 페이지네이션 상태
    const pagination = reactive({
      currentPage: 0,
      totalPages: 0,
      totalElements: 0,
      size: 20
    })

    // 통계 데이터
    const totalReservations = ref(0)
    const confirmedReservations = ref(0)
    const cancelledReservations = ref(0)
    const paidReservations = ref(0)

    // 예약 목록 조회
    const searchReservations = async () => {
      try {
        const params = new URLSearchParams({
          page: pagination.currentPage,
          size: pagination.size
        })

        if (filters.hotelName) params.append('hotelName', filters.hotelName)
        if (filters.userName) params.append('userName', filters.userName)
        if (filters.reservationStatus) params.append('reservationStatus', filters.reservationStatus)
        if (filters.paymentStatus) params.append('paymentStatus', filters.paymentStatus)

        const response = await fetch(`/api/admin/reservations?${params}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })

        if (!response.ok) throw new Error('예약 목록 조회 실패')

        const data = await response.json()
        reservations.value = data.content || []
        
        pagination.totalPages = data.totalPages || 0
        pagination.totalElements = data.totalElements || 0

        // 통계 업데이트 (실제로는 별도 API 호출)
        updateStatistics()
        
      } catch (error) {
        console.error('예약 목록 조회 오류:', error)
        alert('예약 목록을 불러오는데 실패했습니다.')
      }
    }

    // 통계 업데이트
    const updateStatistics = () => {
      totalReservations.value = reservations.value.length
      confirmedReservations.value = reservations.value.filter(r => r.reservationStatus === 'CONFIRMED').length
      cancelledReservations.value = reservations.value.filter(r => r.reservationStatus === 'CANCELLED').length
      paidReservations.value = reservations.value.filter(r => r.paymentStatus === 'COMPLETED').length
    }

    // 페이지 변경
    const changePage = (page) => {
      pagination.currentPage = page
      searchReservations()
    }

    // 필터 초기화
    const resetFilters = () => {
      filters.hotelName = ''
      filters.userName = ''
      filters.reservationStatus = ''
      filters.paymentStatus = ''
      pagination.currentPage = 0
      searchReservations()
    }

    // 예약 상세 보기
    const viewReservationDetail = async (reservation) => {
      try {
        const response = await fetch(`/api/admin/reservations/${reservation.reservationId}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })

        if (!response.ok) throw new Error('예약 상세 조회 실패')

        selectedReservation.value = await response.json()
        showDetailModal.value = true
        
      } catch (error) {
        console.error('예약 상세 조회 오류:', error)
        alert('예약 상세 정보를 불러오는데 실패했습니다.')
      }
    }

    // 상세 모달 닫기
    const closeDetailModal = () => {
      showDetailModal.value = false
      selectedReservation.value = null
    }

    // 예약 상태 텍스트
    const getReservationStatusText = (status) => {
      const statusMap = {
        'PENDING': '대기중',
        'CONFIRMED': '확정',
        'CHECKED_IN': '체크인',
        'CHECKED_OUT': '체크아웃',
        'CANCELLED': '취소',
        'NO_SHOW': '노쇼'
      }
      return statusMap[status] || status
    }

    // 예약 상태 클래스
    const getReservationStatusClass = (status) => {
      const classMap = {
        'PENDING': 'status-pending',
        'CONFIRMED': 'status-confirmed',
        'CHECKED_IN': 'status-checked-in',
        'CHECKED_OUT': 'status-checked-out',
        'CANCELLED': 'status-cancelled',
        'NO_SHOW': 'status-no-show'
      }
      return classMap[status] || ''
    }

    // 결제 상태 텍스트
    const getPaymentStatusText = (status) => {
      if (!status) return '결제정보없음'
      const statusMap = {
        'PENDING': '결제대기',
        'COMPLETED': '결제완료',
        'FAILED': '결제실패',
        'CANCELLED': '결제취소',
        'REFUNDED': '환불완료'
      }
      return statusMap[status] || status
    }

    // 결제 상태 클래스
    const getPaymentStatusClass = (status) => {
      if (!status) return 'status-no-payment'
      const classMap = {
        'PENDING': 'status-pending',
        'COMPLETED': 'status-completed',
        'FAILED': 'status-failed',
        'CANCELLED': 'status-cancelled',
        'REFUNDED': 'status-refunded'
      }
      return classMap[status] || ''
    }

    // 결제 수단 텍스트
    const getPaymentMethodText = (method) => {
      if (!method) return '미지정'
      const methodMap = {
        'CREDIT_CARD': '신용카드',
        'BANK_TRANSFER': '계좌이체',
        'VIRTUAL_ACCOUNT': '가상계좌',
        'MOBILE': '휴대폰결제',
        'KAKAO_PAY': '카카오페이',
        'NAVER_PAY': '네이버페이'
      }
      return methodMap[method] || method
    }

    // 날짜 포맷팅
    const formatDate = (dateString) => {
      if (!dateString) return '-'
      return new Date(dateString).toLocaleDateString('ko-KR')
    }

    // 날짜시간 포맷팅
    const formatDateTime = (dateString) => {
      if (!dateString) return '-'
      return new Date(dateString).toLocaleString('ko-KR')
    }

    // 통화 포맷팅
    const formatCurrency = (amount) => {
      if (!amount) return '-'
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW'
      }).format(amount)
    }

    // 컴포넌트 마운트 시 데이터 로드
    onMounted(() => {
      searchReservations()
    })

    return {
      reservations,
      selectedReservation,
      showDetailModal,
      filters,
      pagination,
      totalReservations,
      confirmedReservations,
      cancelledReservations,
      paidReservations,
      searchReservations,
      changePage,
      resetFilters,
      viewReservationDetail,
      closeDetailModal,
      getReservationStatusText,
      getReservationStatusClass,
      getPaymentStatusText,
      getPaymentStatusClass,
      getPaymentMethodText,
      formatDate,
      formatDateTime,
      formatCurrency
    }
  }
}
</script>

<style scoped>
.reservation-monitoring {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
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

/* 필터 섹션 */
.filters {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-input,
.filter-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 150px;
}

.search-btn,
.reset-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.search-btn {
  background-color: #007bff;
  color: white;
}

.reset-btn {
  background-color: #6c757d;
  color: white;
}

/* 통계 카드 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  text-align: center;
}

.stat-card h3 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #666;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
  color: #333;
}

.stat-number.confirmed { color: #28a745; }
.stat-number.cancelled { color: #dc3545; }
.stat-number.paid { color: #007bff; }

/* 테이블 */
.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow-x: auto;
  margin-bottom: 20px;
}

.reservation-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.reservation-table th {
  background-color: #f8f9fa;
  padding: 12px 8px;
  text-align: left;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #dee2e6;
  white-space: nowrap;
}

.reservation-table td {
  padding: 12px 8px;
  border-bottom: 1px solid #dee2e6;
  vertical-align: top;
}

.reservation-table tr:hover {
  background-color: #f8f9fa;
}

/* 상태 배지 */
.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  white-space: nowrap;
}

.status-pending { background-color: #fff3cd; color: #856404; }
.status-confirmed { background-color: #d4edda; color: #155724; }
.status-checked-in { background-color: #cce5ff; color: #004085; }
.status-checked-out { background-color: #e2e3e5; color: #383d41; }
.status-cancelled { background-color: #f8d7da; color: #721c24; }
.status-no-show { background-color: #ffeaa7; color: #6c5ce7; }
.status-completed { background-color: #d4edda; color: #155724; }
.status-failed { background-color: #f8d7da; color: #721c24; }
.status-refunded { background-color: #fff3cd; color: #856404; }
.status-no-payment { background-color: #e2e3e5; color: #6c757d; }

/* 액션 버튼 */
.action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-right: 5px;
}

.view-btn {
  background-color: #007bff;
  color: white;
}

.view-btn:hover {
  background-color: #0056b3;
}

/* 빈 메시지 */
.empty-message {
  text-align: center;
  padding: 40px;
  color: #666;
}

/* 페이지네이션 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}

/* 모달 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #dee2e6;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body {
  padding: 20px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
}

.detail-section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #333;
  border-bottom: 2px solid #007bff;
  padding-bottom: 5px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f1f1f1;
}

.detail-item label {
  font-weight: 500;
  color: #666;
  min-width: 100px;
}

.detail-item span {
  text-align: right;
  word-break: break-all;
}

@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-input,
  .filter-select {
    min-width: auto;
    width: 100%;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .reservation-table {
    font-size: 12px;
  }

  .reservation-table th,
  .reservation-table td {
    padding: 8px 4px;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .detail-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-item span {
    text-align: left;
    margin-top: 4px;
  }
}
</style>