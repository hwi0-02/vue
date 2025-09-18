<template>
  <div class="sales-management">
    <div class="page-header">
      <h1>매출·수수료 관리</h1>
      <p>지정된 기간의 매출과 수수료를 요약하여 확인합니다.</p>
    </div>

    <!-- 날짜 범위 선택 -->
    <div class="date-filter">
      <div class="date-inputs">
        <div class="date-group">
          <label for="fromDate">시작일:</label>
          <input 
            id="fromDate"
            v-model="dateRange.from" 
            type="date" 
            class="date-input"
          />
        </div>
        <div class="date-group">
          <label for="toDate">종료일:</label>
          <input 
            id="toDate"
            v-model="dateRange.to" 
            type="date" 
            class="date-input"
          />
        </div>
        <button @click="searchSales" class="search-btn" :disabled="loading">
          {{ loading ? '조회중...' : '조회' }}
        </button>
        <button @click="setQuickDate(7)" class="quick-btn">최근 7일</button>
        <button @click="setQuickDate(30)" class="quick-btn">최근 30일</button>
        <button @click="setQuickDate(90)" class="quick-btn">최근 3개월</button>
      </div>
    </div>

    <!-- 매출 요약 카드 -->
    <div class="summary-cards" v-if="salesData">
      <div class="card total-revenue">
        <div class="card-header">
          <h3>총 매출</h3>
          <i class="icon">💰</i>
        </div>
        <div class="card-content">
          <p class="amount">{{ formatCurrency(salesData.totalRevenue) }}</p>
          <small>{{ dateRange.from }} ~ {{ dateRange.to }}</small>
        </div>
      </div>

      <div class="card platform-fee">
        <div class="card-header">
          <h3>플랫폼 수익</h3>
          <i class="icon">📊</i>
        </div>
        <div class="card-content">
          <p class="amount">{{ formatCurrency(salesData.platformFeeAmount) }}</p>
          <small>수수료율 10%</small>
        </div>
      </div>

      <div class="card hotels-count">
        <div class="card-header">
          <h3>참여 호텔</h3>
          <i class="icon">🏨</i>
        </div>
        <div class="card-content">
          <p class="amount">{{ salesData.hotelSettlements.length }}</p>
          <small>개 호텔</small>
        </div>
      </div>

      <div class="card avg-settlement">
        <div class="card-header">
          <h3>평균 정산액</h3>
          <i class="icon">📈</i>
        </div>
        <div class="card-content">
          <p class="amount">{{ formatCurrency(averageSettlement) }}</p>
          <small>호텔당 평균</small>
        </div>
      </div>
    </div>

    <!-- 호텔별 정산 테이블 -->
    <div class="settlements-section" v-if="salesData && salesData.hotelSettlements.length > 0">
      <div class="section-header">
        <h2>호텔별 정산 내역</h2>
        <div class="table-controls">
          <input 
            v-model="searchHotel" 
            type="text" 
            placeholder="호텔명 검색"
            class="search-input"
          />
          <select v-model="sortBy" class="sort-select">
            <option value="revenue">매출액순</option>
            <option value="settlement">정산액순</option>
            <option value="reservations">예약건수순</option>
            <option value="name">호텔명순</option>
          </select>
        </div>
      </div>

      <div class="table-container">
        <table class="settlements-table">
          <thead>
            <tr>
              <th>순위</th>
              <th>호텔명</th>
              <th>총 매출</th>
              <th>정산 금액 (90%)</th>
              <th>플랫폼 수수료 (10%)</th>
              <th>예약 건수</th>
              <th>건당 평균 매출</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(settlement, index) in filteredSettlements" :key="settlement.hotelId">
              <td>
                <span class="rank" :class="getRankClass(index)">{{ index + 1 }}</span>
              </td>
              <td>
                <div class="hotel-info">
                  <strong>{{ settlement.hotelName }}</strong>
                  <small>ID: {{ settlement.hotelId }}</small>
                </div>
              </td>
              <td class="amount-cell">{{ formatCurrency(settlement.totalRevenue) }}</td>
              <td class="amount-cell settlement-amount">{{ formatCurrency(settlement.settlementAmount) }}</td>
              <td class="amount-cell platform-fee">{{ formatCurrency(getPlatformFee(settlement.totalRevenue)) }}</td>
              <td class="text-center">{{ settlement.reservationCount }}</td>
              <td class="amount-cell">{{ formatCurrency(getAveragePerReservation(settlement)) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 정산 요약 -->
      <div class="settlement-summary">
        <div class="summary-item">
          <span>총 정산 호텔:</span>
          <strong>{{ salesData.hotelSettlements.length }}개</strong>
        </div>
        <div class="summary-item">
          <span>총 호텔 정산액:</span>
          <strong>{{ formatCurrency(totalHotelSettlement) }}</strong>
        </div>
        <div class="summary-item">
          <span>총 플랫폼 수수료:</span>
          <strong>{{ formatCurrency(salesData.platformFeeAmount) }}</strong>
        </div>
      </div>
    </div>

    <!-- 빈 데이터 메시지 -->
    <div v-else-if="salesData && salesData.hotelSettlements.length === 0" class="empty-message">
      <div class="empty-icon">📊</div>
      <h3>선택한 기간에 매출 데이터가 없습니다</h3>
      <p>다른 날짜 범위를 선택해보세요.</p>
    </div>

    <!-- 로딩 상태 -->
    <div v-else-if="loading" class="loading-message">
      <div class="loading-spinner"></div>
      <p>매출 데이터를 조회하고 있습니다...</p>
    </div>

    <!-- 초기 상태 -->
    <div v-else class="initial-message">
      <div class="initial-icon">📅</div>
      <h3>날짜 범위를 선택하고 조회 버튼을 클릭하세요</h3>
      <p>매출·수수료 관리를 위한 데이터를 조회할 수 있습니다.</p>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'

export default {
  name: 'SalesManagement',
  setup() {
    const salesData = ref(null)
    const loading = ref(false)
    const searchHotel = ref('')
    const sortBy = ref('revenue')

    // 날짜 범위 상태
    const dateRange = reactive({
      from: '',
      to: ''
    })

    // 초기 날짜 설정 (최근 30일)
    const initializeDates = () => {
      const today = new Date()
      const thirtyDaysAgo = new Date(today)
      thirtyDaysAgo.setDate(today.getDate() - 30)

      dateRange.to = today.toISOString().split('T')[0]
      dateRange.from = thirtyDaysAgo.toISOString().split('T')[0]
    }

    // 빠른 날짜 설정
    const setQuickDate = (days) => {
      const today = new Date()
      const pastDate = new Date(today)
      pastDate.setDate(today.getDate() - days)

      dateRange.to = today.toISOString().split('T')[0]
      dateRange.from = pastDate.toISOString().split('T')[0]
    }

    // 매출 데이터 조회
    const searchSales = async () => {
      if (!dateRange.from || !dateRange.to) {
        alert('시작일과 종료일을 모두 선택해주세요.')
        return
      }

      if (new Date(dateRange.from) > new Date(dateRange.to)) {
        alert('시작일은 종료일보다 이전이어야 합니다.')
        return
      }

      loading.value = true
      try {
        const params = new URLSearchParams({
          from: dateRange.from,
          to: dateRange.to
        })

        const response = await fetch(`/api/admin/sales?${params}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })

        if (!response.ok) throw new Error('매출 데이터 조회 실패')

        salesData.value = await response.json()
        
      } catch (error) {
        console.error('매출 데이터 조회 오류:', error)
        alert('매출 데이터를 불러오는데 실패했습니다.')
      } finally {
        loading.value = false
      }
    }

    // 필터링된 정산 데이터
    const filteredSettlements = computed(() => {
      if (!salesData.value) return []

      let filtered = salesData.value.hotelSettlements

      // 호텔명 검색
      if (searchHotel.value) {
        filtered = filtered.filter(settlement => 
          settlement.hotelName.toLowerCase().includes(searchHotel.value.toLowerCase())
        )
      }

      // 정렬
      return filtered.sort((a, b) => {
        switch (sortBy.value) {
          case 'revenue':
            return b.totalRevenue - a.totalRevenue
          case 'settlement':
            return b.settlementAmount - a.settlementAmount
          case 'reservations':
            return b.reservationCount - a.reservationCount
          case 'name':
            return a.hotelName.localeCompare(b.hotelName, 'ko')
          default:
            return 0
        }
      })
    })

    // 평균 정산액 계산
    const averageSettlement = computed(() => {
      if (!salesData.value || salesData.value.hotelSettlements.length === 0) return 0
      
      const total = salesData.value.hotelSettlements.reduce((sum, settlement) => 
        sum + settlement.settlementAmount, 0)
      
      return total / salesData.value.hotelSettlements.length
    })

    // 총 호텔 정산액 계산
    const totalHotelSettlement = computed(() => {
      if (!salesData.value) return 0
      
      return salesData.value.hotelSettlements.reduce((sum, settlement) => 
        sum + settlement.settlementAmount, 0)
    })

    // 순위 클래스 반환
    const getRankClass = (index) => {
      if (index === 0) return 'rank-gold'
      if (index === 1) return 'rank-silver'
      if (index === 2) return 'rank-bronze'
      return ''
    }

    // 플랫폼 수수료 계산
    const getPlatformFee = (totalRevenue) => {
      return totalRevenue * 0.10
    }

    // 건당 평균 매출 계산
    const getAveragePerReservation = (settlement) => {
      if (settlement.reservationCount === 0) return 0
      return settlement.totalRevenue / settlement.reservationCount
    }

    // 통화 포맷팅
    const formatCurrency = (amount) => {
      if (!amount) return '₩0'
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW'
      }).format(amount)
    }

    // 컴포넌트 마운트 시 초기화
    onMounted(() => {
      initializeDates()
    })

    return {
      salesData,
      loading,
      searchHotel,
      sortBy,
      dateRange,
      filteredSettlements,
      averageSettlement,
      totalHotelSettlement,
      setQuickDate,
      searchSales,
      getRankClass,
      getPlatformFee,
      getAveragePerReservation,
      formatCurrency
    }
  }
}
</script>

<style scoped>
.sales-management {
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

/* 날짜 필터 */
.date-filter {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.date-inputs {
  display: flex;
  gap: 15px;
  align-items: end;
  flex-wrap: wrap;
}

.date-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.date-group label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.date-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 150px;
}

.search-btn,
.quick-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  height: 36px;
}

.search-btn {
  background-color: #007bff;
  color: white;
}

.search-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.quick-btn {
  background-color: #f8f9fa;
  color: #333;
  border: 1px solid #ddd;
}

.quick-btn:hover {
  background-color: #e9ecef;
}

/* 요약 카드 */
.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #666;
}

.card-header .icon {
  font-size: 24px;
}

.card-content .amount {
  font-size: 24px;
  font-weight: bold;
  margin: 0 0 5px 0;
  color: #333;
}

.card-content small {
  color: #666;
  font-size: 12px;
}

.total-revenue .amount { color: #28a745; }
.platform-fee .amount { color: #007bff; }
.hotels-count .amount { color: #6f42c1; }
.avg-settlement .amount { color: #fd7e14; }

/* 정산 섹션 */
.settlements-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #dee2e6;
}

.section-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.table-controls {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-input,
.sort-select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.search-input {
  min-width: 200px;
}

/* 테이블 */
.table-container {
  overflow-x: auto;
}

.settlements-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.settlements-table th {
  background-color: #f8f9fa;
  padding: 12px 8px;
  text-align: left;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #dee2e6;
  white-space: nowrap;
}

.settlements-table td {
  padding: 12px 8px;
  border-bottom: 1px solid #dee2e6;
  vertical-align: middle;
}

.settlements-table tr:hover {
  background-color: #f8f9fa;
}

.rank {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-weight: bold;
  color: white;
  min-width: 24px;
  text-align: center;
}

.rank-gold { background-color: #ffd700; color: #333; }
.rank-silver { background-color: #c0c0c0; color: #333; }
.rank-bronze { background-color: #cd7f32; color: white; }

.hotel-info strong {
  display: block;
  margin-bottom: 2px;
}

.hotel-info small {
  color: #666;
  font-size: 12px;
}

.amount-cell {
  text-align: right;
  font-weight: 500;
}

.settlement-amount { color: #28a745; }
.platform-fee { color: #007bff; }

.text-center {
  text-align: center;
}

/* 정산 요약 */
.settlement-summary {
  display: flex;
  justify-content: space-around;
  padding: 20px;
  background-color: #f8f9fa;
  border-top: 1px solid #dee2e6;
}

.summary-item {
  text-align: center;
}

.summary-item span {
  display: block;
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}

.summary-item strong {
  font-size: 18px;
  color: #333;
}

/* 메시지 */
.empty-message,
.loading-message,
.initial-message {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.empty-icon,
.initial-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 모바일 반응형 */
@media (max-width: 768px) {
  .date-inputs {
    flex-direction: column;
    align-items: stretch;
  }

  .date-input,
  .search-btn,
  .quick-btn {
    width: 100%;
  }

  .section-header {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }

  .table-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    min-width: auto;
  }

  .settlements-table {
    font-size: 12px;
  }

  .settlements-table th,
  .settlements-table td {
    padding: 8px 4px;
  }

  .settlement-summary {
    flex-direction: column;
    gap: 15px;
  }
}
</style>