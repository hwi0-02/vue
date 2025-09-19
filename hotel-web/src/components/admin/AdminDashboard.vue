<template>
  <div class="admin-dashboard">
    <div class="page-header">
      <h1>관리자 대시보드</h1>
      <p class="page-description">전체 현황을 한눈에 보고 필요한 지표를 설정할 수 있습니다.</p>
    </div>
    <div class="page-toolbar">
      <span class="last-updated">마지막 업데이트: {{ lastUpdated }}</span>
      <button class="btn" @click="showFilterDrawer = true">설정</button>
      <button class="btn btn-primary" @click="refreshData">새로고침</button>
    </div>

    <!-- 핵심 지표 요약 카드 (다른 페이지와 동일한 카드 디자인) -->
    <div class="summary-cards mb-16">
      <div class="card users">
        <div class="card-icon"></div>
        <div class="card-content">
          <h3>총 사용자</h3>
          <p class="card-number">{{ formatNumber(dashboardData.totalUsers) }}</p>
        </div>
      </div>
      <div class="card businesses">
        <div class="card-icon"></div>
        <div class="card-content">
          <h3>총 사업자</h3>
          <p class="card-number">{{ formatNumber(dashboardData.totalBusinesses) }}</p>
        </div>
      </div>
      <div class="card reservations">
        <div class="card-icon"></div>
        <div class="card-content">
          <h3>총 예약</h3>
          <p class="card-number">{{ formatNumber(dashboardData.totalReservations) }}</p>
        </div>
      </div>
      <div class="card revenue">
        <div class="card-icon"></div>
        <div class="card-content">
          <h3>총 매출</h3>
          <p class="card-number">{{ formatCurrency(dashboardData.totalRevenue) }}</p>
        </div>
      </div>
      <div class="card reviews">
        <div class="card-icon"></div>
        <div class="card-content">
          <h3>총 리뷰</h3>
          <p class="card-number">{{ formatNumber(dashboardData.totalReviews) }}</p>
        </div>
      </div>
      <div class="card coupons">
        <div class="card-icon"></div>
        <div class="card-content">
          <h3>총 쿠폰</h3>
          <p class="card-number">{{ formatNumber(dashboardData.totalCoupons) }}</p>
        </div>
      </div>
    </div>

  <!-- 차트 섹션 -->
    <div class="charts-section">
      <!-- 매출 추이 차트 -->
      <div class="chart-container" v-if="chartOptions.showRevenue">
        <div class="chart-header">
          <h3>최근 30일 매출 추이</h3>
        </div>
        <div class="chart-content">
          <Line
            v-if="revenueChartData"
            :data="revenueChartData"
            :options="revenueChartOptions"
            :height="100"
          />
          <div v-else class="chart-loading">
            차트 로딩 중...
          </div>
        </div>
      </div>

      <!-- 월별 가입자 차트 -->
      <div class="chart-container" v-if="chartOptions.showSignups">
        <div class="chart-header">
          <h3>최근 12개월 신규 가입자</h3>
        </div>
        <div class="chart-content">
          <Bar
            v-if="signupChartData"
            :data="signupChartData"
            :options="signupChartOptions"
            :height="100"
          />
          <div v-else class="chart-loading">
            차트 로딩 중...
          </div>
        </div>
      </div>
    </div>

  <!-- 인기 호텔 Top 5 -->
    <div class="top-hotels-section" v-if="chartOptions.showTopHotels">
      <div class="section-header">
  <h3>인기 호텔 Top 5 (최근 30일)</h3>
      </div>
      <div class="top-hotels-list">
        <div 
          v-for="(hotel, index) in dashboardData.topHotels" 
          :key="hotel.hotelId"
          class="hotel-item"
          :class="`rank-${index + 1}`"
        >
          <div class="hotel-rank">
            <span class="rank-number">{{ index + 1 }}</span>
            
          </div>
          <div class="hotel-info">
            <h4>{{ hotel.hotelName }}</h4>
            <p class="business-name">{{ hotel.businessName }}</p>
          </div>
          <div class="hotel-stats">
            <div class="stat">
              <span class="label">예약</span>
              <span class="value">{{ formatNumber(hotel.reservationCount) }}건</span>
            </div>
            <div class="stat">
              <span class="label">매출</span>
              <span class="value">{{ formatCurrency(hotel.revenue) }}</span>
            </div>
            <div class="stat">
              <span class="label">평점</span>
              <span class="value">{{ hotel.averageRating.toFixed(1) }}</span>
            </div>
          </div>
        </div>
        
        <div v-if="dashboardData.topHotels.length === 0" class="no-data">
      최근 30일간 예약 데이터가 없습니다.
        </div>
      </div>
    </div>

    <!-- 설정 드로어 -->
    <div class="drawer-overlay" v-if="showFilterDrawer" @click.self="showFilterDrawer = false">
      <div class="drawer" role="dialog" aria-modal="true">
        <div class="drawer-header">
          <strong>대시보드 설정</strong>
          <button class="btn" @click="showFilterDrawer = false">닫기</button>
        </div>
        <div class="drawer-body">
          <label><input type="checkbox" v-model="chartOptions.showRevenue"/> 매출 추이</label>
          <label><input type="checkbox" v-model="chartOptions.showSignups"/> 월별 가입자</label>
          <label><input type="checkbox" v-model="chartOptions.showTopHotels"/> 인기 호텔</label>
        </div>
        <div class="drawer-footer">
          <button class="btn btn-primary" @click="applyDashboardOptions">적용</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { Line, Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js'
import api from '../../api/http'

// Chart.js 등록
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler
)

export default {
  name: 'AdminDashboard',
  components: {
    Line,
    Bar
  },
  setup() {
    // 반응형 데이터
  const loading = ref(false)
    const lastUpdated = ref('')
    const dashboardData = reactive({
      totalUsers: 0,
      totalBusinesses: 0,
      totalReservations: 0,
      totalRevenue: 0,
      totalReviews: 0,
      totalCoupons: 0,
      dailyRevenues: [],
      monthlySignups: [],
      topHotels: []
    })
    
    const revenueChartData = ref(null)
    const signupChartData = ref(null)

    // Element Plus - 대시보드 설정 드로어 상태 및 옵션
    const showFilterDrawer = ref(false)
    const chartOptions = reactive({
      showRevenue: true,
      showSignups: true,
      showTopHotels: true
    })

    const loadSavedOptions = () => {
      try {
        const saved = localStorage.getItem('dashboardChartOptions')
        if (saved) {
          const parsed = JSON.parse(saved)
          chartOptions.showRevenue = !!parsed.showRevenue
          chartOptions.showSignups = !!parsed.showSignups
          chartOptions.showTopHotels = !!parsed.showTopHotels
        }
      } catch (e) {
      }
    }
    const saveOptions = () => {
      try {
        localStorage.setItem('dashboardChartOptions', JSON.stringify(chartOptions))
      } catch (e) {
      }
    }

    // 차트 옵션
    const revenueChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          mode: 'index',
          intersect: false,
          callbacks: {
            label: function(context) {
              return '매출: ' + formatCurrency(context.parsed.y)
            }
          }
        }
      },
      scales: {
        x: {
          display: true,
          title: {
            display: true,
            text: '날짜'
          }
        },
        y: {
          display: true,
          title: {
            display: true,
            text: '매출 (원)'
          },
          ticks: {
            callback: function(value) {
              return formatCurrency(value)
            }
          }
        }
      },
      elements: {
        line: {
          tension: 0.4
        }
      }
    }

    const signupChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'top'
        },
        tooltip: {
          mode: 'index',
          intersect: false,
          callbacks: {
            label: function(context) {
              const label = context.dataset.label || ''
              const value = Math.round(context.parsed.y || 0)
              return `${label}: ${formatNumber(value)}명`
            }
          }
        }
      },
      scales: {
        x: {
          display: true,
          title: {
            display: true,
            text: '월'
          }
        },
        y: {
          display: true,
          title: {
            display: true,
            text: '가입자 수'
          },
          beginAtZero: true,
          ticks: {
            precision: 0,
            callback: function(value) {
              return formatNumber(Math.round(value))
            }
          }
        }
      }
    }

    // 대시보드 데이터 로드
    const loadDashboardData = async () => {
      loading.value = true
      try {
  const response = await api.get('/admin/dashboard/summary')
  const data = response.data || {}

        // 기본 통계 데이터 설정
        Object.assign(dashboardData, data)

        // 매출 차트 데이터 설정
        const daily = Array.isArray(data.dailyRevenues) ? data.dailyRevenues : []
        revenueChartData.value = {
          labels: daily.map(item => 
            new Date(item.date).toLocaleDateString('ko-KR', { 
              month: 'short', 
              day: 'numeric' 
            })
          ),
          datasets: [
            {
              label: '일별 매출',
              data: daily.map(item => Number(item.revenue ?? 0)),
              borderColor: 'rgb(75, 192, 192)',
              backgroundColor: 'rgba(75, 192, 192, 0.1)',
              fill: true,
              tension: 0.4
            }
          ]
        }

        // 가입자 차트 데이터 설정
        const monthly = Array.isArray(data.monthlySignups) ? data.monthlySignups : []
        signupChartData.value = {
          labels: monthly.map(item => item.month),
          datasets: [
            {
              label: '사용자',
              data: monthly.map(item => Math.round(Number(item.userCount ?? 0))),
              backgroundColor: 'rgba(54, 162, 235, 0.8)',
              borderColor: 'rgba(54, 162, 235, 1)',
              borderWidth: 1
            },
            {
              label: '사업자',
              data: monthly.map(item => Math.round(Number(item.businessCount ?? 0))),
              backgroundColor: 'rgba(255, 99, 132, 0.8)',
              borderColor: 'rgba(255, 99, 132, 1)',
              borderWidth: 1
            }
          ]
        }

        lastUpdated.value = new Date().toLocaleString('ko-KR')

      } catch (error) {
        alert('대시보드 데이터를 불러오는데 실패했습니다.')
      } finally {
        loading.value = false
      }
    }

    // 데이터 새로고침
    const refreshData = () => {
      loadDashboardData()
    }

    const applyDashboardOptions = () => {
      saveOptions()
      showFilterDrawer.value = false
    }


    // 유틸리티 함수들
    const formatNumber = (num) => {
      if (!num) return '0'
      return num.toLocaleString('ko-KR')
    }

    const formatCurrency = (amount) => {
      const num = typeof amount === 'number' ? amount : Number(amount || 0)
      if (!num) return '0원'
      return num.toLocaleString('ko-KR') + '원'
    }

    // 컴포넌트 마운트 시 데이터 로드
    onMounted(() => {
      loadSavedOptions()
      loadDashboardData()
    })

    return {
      // 반응형 데이터
      loading,
      lastUpdated,
      dashboardData,
      revenueChartData,
      signupChartData,
      
      // 차트 옵션
      revenueChartOptions,
      signupChartOptions,
      
      // 함수들
      refreshData,
      formatNumber,
      formatCurrency,
      showFilterDrawer,
      chartOptions,
      applyDashboardOptions
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

.page-toolbar { display: flex; align-items: center; justify-content: flex-end; gap: 10px; margin-bottom: 16px; }

.last-updated {
  color: #6c757d;
  font-size: 14px;
}

/* 요약 카드 스타일 (HotelManagement와 일치) */
.summary-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.2s ease;
}

.card:hover { transform: translateY(-2px); }

.card.users { border-left: 4px solid #667eea; }
.card.businesses { border-left: 4px solid #f093fb; }
.card.reservations { border-left: 4px solid #4facfe; }
.card.revenue { border-left: 4px solid #43e97b; }
.card.reviews { border-left: 4px solid #ffecd2; }
.card.coupons { border-left: 4px solid #a8edea; }

.card-icon {
  font-size: 32px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  border-radius: 50%;
}

.card-content h3 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #7f8c8d;
  font-weight: 500;
}

.card-number {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
}

/* 차트 섹션 */
.charts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 40px;
}

.chart-container {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.chart-header {
  margin-bottom: 20px;
  border-bottom: 1px solid #e9ecef;
  padding-bottom: 15px;
}

.chart-header h3 {
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-content {
  height: 300px;
  position: relative;
}

.chart-loading {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6c757d;
  font-size: 16px;
}

.chart-loading i {
  margin-right: 10px;
}

.mb-16 { margin-bottom: 16px; }
.kpi { font-size: 22px; font-weight: 700; margin-top: 6px; }

/* 인기 호텔 섹션 */
.top-hotels-section {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.section-header {
  margin-bottom: 25px;
  border-bottom: 1px solid #e9ecef;
  padding-bottom: 15px;
}

.section-header h3 {
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-hotels-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.hotel-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 10px;
  background: #f8f9fa;
  transition: all 0.3s ease;
}

.hotel-item:hover {
  background: #e9ecef;
  transform: translateX(5px);
}

.hotel-item.rank-1 {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4a 100%);
  color: #2c3e50;
}

.hotel-item.rank-2 {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
  color: #2c3e50;
}

.hotel-item.rank-3 {
  background: linear-gradient(135deg, #cd7f32 0%, #daa520 100%);
  color: white;
}

.hotel-rank {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-right: 20px;
  min-width: 60px;
}

.rank-number {
  font-size: 24px;
  font-weight: 700;
  width: 30px;
  text-align: center;
}

/* icon styles removed */

.hotel-info {
  flex: 1;
  margin-right: 20px;
}

.hotel-info h4 {
  margin: 0 0 5px 0;
  font-size: 18px;
  font-weight: 600;
}

.business-name {
  margin: 0;
  color: #6c757d;
  font-size: 14px;
}

.hotel-item.rank-1 .business-name,
.hotel-item.rank-2 .business-name,
.hotel-item.rank-3 .business-name {
  color: rgba(0, 0, 0, 0.7);
}

.hotel-stats {
  display: flex;
  gap: 20px;
}

.stat {
  text-align: center;
  min-width: 80px;
}

.stat .label {
  display: block;
  font-size: 12px;
  color: #6c757d;
  margin-bottom: 2px;
}

.hotel-item.rank-1 .stat .label,
.hotel-item.rank-2 .stat .label,
.hotel-item.rank-3 .stat .label {
  color: rgba(0, 0, 0, 0.6);
}

.stat .value {
  display: block;
  font-size: 14px;
  font-weight: 600;
}

.stat .value i {
  color: #ffc107;
  margin-right: 2px;
}

.no-data {
  text-align: center;
  padding: 40px;
  color: #6c757d;
  font-size: 16px;
}

.no-data i {
  margin-right: 8px;
}

/* 버튼 스타일 */
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  justify-content: center;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 설정 드로어 */
.drawer-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  justify-content: flex-end;
  z-index: 1000;
}

.drawer {
  width: min(360px, 90vw);
  background: #fff;
  height: 100%;
  box-shadow: -4px 0 12px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e9ecef;
}

.drawer-body {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.drawer-footer {
  margin-top: auto;
  padding: 12px 20px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
}


/* 로딩 오버레이 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 반응형 디자인 */
@media (max-width: 1200px) {
  .charts-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .summary-cards {
    grid-template-columns: 1fr;
  }
  
  .hotel-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .hotel-rank {
    margin-right: 0;
  }
  
  .hotel-stats {
    width: 100%;
    justify-content: space-around;
  }
  
  .dashboard-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
}
</style>