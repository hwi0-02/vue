<template>
  <div class="admin-dashboard">
    <div class="dashboard-header">
      <h2><i class="fas fa-chart-line"></i> 관리자 대시보드</h2>
      <div class="refresh-section">
        <span class="last-updated">마지막 업데이트: {{ lastUpdated }}</span>
        <button class="btn btn-primary" @click="refreshData">
          <i class="fas fa-refresh" :class="{ 'fa-spin': loading }"></i>
          새로고침
        </button>
      </div>
    </div>

    <!-- 핵심 지표 요약 카드 -->
    <div class="summary-cards">
      <div class="summary-card users">
        <div class="card-icon">
          <i class="fas fa-users"></i>
        </div>
        <div class="card-content">
          <h3>{{ formatNumber(dashboardData.totalUsers) }}</h3>
          <p>총 사용자</p>
        </div>
      </div>
      
      <div class="summary-card businesses">
        <div class="card-icon">
          <i class="fas fa-building"></i>
        </div>
        <div class="card-content">
          <h3>{{ formatNumber(dashboardData.totalBusinesses) }}</h3>
          <p>총 사업자</p>
        </div>
      </div>
      
      <div class="summary-card reservations">
        <div class="card-icon">
          <i class="fas fa-calendar-check"></i>
        </div>
        <div class="card-content">
          <h3>{{ formatNumber(dashboardData.totalReservations) }}</h3>
          <p>총 예약</p>
        </div>
      </div>
      
      <div class="summary-card revenue">
        <div class="card-icon">
          <i class="fas fa-won-sign"></i>
        </div>
        <div class="card-content">
          <h3>{{ formatCurrency(dashboardData.totalRevenue) }}</h3>
          <p>총 매출</p>
        </div>
      </div>
      
      <div class="summary-card reviews">
        <div class="card-icon">
          <i class="fas fa-star"></i>
        </div>
        <div class="card-content">
          <h3>{{ formatNumber(dashboardData.totalReviews) }}</h3>
          <p>총 리뷰</p>
        </div>
      </div>
      
      <div class="summary-card coupons">
        <div class="card-icon">
          <i class="fas fa-ticket-alt"></i>
        </div>
        <div class="card-content">
          <h3>{{ formatNumber(dashboardData.totalCoupons) }}</h3>
          <p>총 쿠폰</p>
        </div>
      </div>
    </div>

    <!-- 차트 섹션 -->
    <div class="charts-section">
      <!-- 매출 추이 차트 -->
      <div class="chart-container">
        <div class="chart-header">
          <h3><i class="fas fa-chart-line"></i> 최근 30일 매출 추이</h3>
        </div>
        <div class="chart-content">
          <Line
            v-if="revenueChartData"
            :data="revenueChartData"
            :options="revenueChartOptions"
            :height="100"
          />
          <div v-else class="chart-loading">
            <i class="fas fa-spinner fa-spin"></i>
            차트 로딩 중...
          </div>
        </div>
      </div>

      <!-- 월별 가입자 차트 -->
      <div class="chart-container">
        <div class="chart-header">
          <h3><i class="fas fa-chart-bar"></i> 최근 12개월 신규 가입자</h3>
        </div>
        <div class="chart-content">
          <Bar
            v-if="signupChartData"
            :data="signupChartData"
            :options="signupChartOptions"
            :height="100"
          />
          <div v-else class="chart-loading">
            <i class="fas fa-spinner fa-spin"></i>
            차트 로딩 중...
          </div>
        </div>
      </div>
    </div>

    <!-- 인기 호텔 Top 5 -->
    <div class="top-hotels-section">
      <div class="section-header">
        <h3><i class="fas fa-trophy"></i> 인기 호텔 Top 5 (최근 30일)</h3>
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
            <i v-if="index === 0" class="fas fa-crown gold"></i>
            <i v-else-if="index === 1" class="fas fa-medal silver"></i>
            <i v-else-if="index === 2" class="fas fa-medal bronze"></i>
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
              <span class="value">
                <i class="fas fa-star"></i>
                {{ hotel.averageRating.toFixed(1) }}
              </span>
            </div>
          </div>
        </div>
        
        <div v-if="dashboardData.topHotels.length === 0" class="no-data">
          <i class="fas fa-info-circle"></i>
          최근 30일간 예약 데이터가 없습니다.
        </div>
      </div>
    </div>

    <!-- 로딩 오버레이 -->
    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
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
          intersect: false
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
          beginAtZero: true
        }
      }
    }

    // 대시보드 데이터 로드
    const loadDashboardData = async () => {
      loading.value = true
      try {
        const response = await api.get('/api/admin/dashboard/summary')
        const data = response.data

        // 기본 통계 데이터 설정
        Object.assign(dashboardData, data)

        // 매출 차트 데이터 설정
        revenueChartData.value = {
          labels: data.dailyRevenues.map(item => 
            new Date(item.date).toLocaleDateString('ko-KR', { 
              month: 'short', 
              day: 'numeric' 
            })
          ),
          datasets: [
            {
              label: '일별 매출',
              data: data.dailyRevenues.map(item => item.revenue),
              borderColor: 'rgb(75, 192, 192)',
              backgroundColor: 'rgba(75, 192, 192, 0.1)',
              fill: true,
              tension: 0.4
            }
          ]
        }

        // 가입자 차트 데이터 설정
        signupChartData.value = {
          labels: data.monthlySignups.map(item => item.month),
          datasets: [
            {
              label: '사용자',
              data: data.monthlySignups.map(item => item.userCount),
              backgroundColor: 'rgba(54, 162, 235, 0.8)',
              borderColor: 'rgba(54, 162, 235, 1)',
              borderWidth: 1
            },
            {
              label: '사업자',
              data: data.monthlySignups.map(item => item.businessCount),
              backgroundColor: 'rgba(255, 99, 132, 0.8)',
              borderColor: 'rgba(255, 99, 132, 1)',
              borderWidth: 1
            }
          ]
        }

        lastUpdated.value = new Date().toLocaleString('ko-KR')

      } catch (error) {
        console.error('대시보드 데이터 로드 실패:', error)
        alert('대시보드 데이터를 불러오는데 실패했습니다.')
      } finally {
        loading.value = false
      }
    }

    // 데이터 새로고침
    const refreshData = () => {
      loadDashboardData()
    }

    // 유틸리티 함수들
    const formatNumber = (num) => {
      if (!num) return '0'
      return num.toLocaleString('ko-KR')
    }

    const formatCurrency = (amount) => {
      if (!amount) return '0원'
      return amount.toLocaleString('ko-KR') + '원'
    }

    // 컴포넌트 마운트 시 데이터 로드
    onMounted(() => {
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
      formatCurrency
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e9ecef;
}

.dashboard-header h2 {
  color: #2c3e50;
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.refresh-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.last-updated {
  color: #6c757d;
  font-size: 14px;
}

/* 요약 카드 스타일 */
.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.summary-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.summary-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
}

.summary-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.summary-card.users::before { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.summary-card.businesses::before { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.summary-card.reservations::before { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.summary-card.revenue::before { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.summary-card.reviews::before { background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%); }
.summary-card.coupons::before { background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); }

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.summary-card.users .card-icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.summary-card.businesses .card-icon { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.summary-card.reservations .card-icon { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.summary-card.revenue .card-icon { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.summary-card.reviews .card-icon { background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%); }
.summary-card.coupons .card-icon { background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); }

.card-content h3 {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 5px 0;
  color: #2c3e50;
}

.card-content p {
  margin: 0;
  color: #7f8c8d;
  font-size: 16px;
  font-weight: 500;
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

.fa-crown.gold { color: #ffd700; }
.fa-medal.silver { color: #c0c0c0; }
.fa-medal.bronze { color: #cd7f32; }

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