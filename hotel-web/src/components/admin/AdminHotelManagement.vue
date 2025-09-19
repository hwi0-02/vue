<template>
  <div class="hotel-management">
    <div class="page-header">
      <h1>호텔 및 숙소 관리</h1>
      <p class="page-description">전체 호텔 현황을 관리하고 승인/거부 처리를 할 수 있습니다.</p>
    </div>

    <!-- 통계 카드 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon"></div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalHotels }}</div>
          <div class="stat-label">전체 호텔</div>
        </div>
      </div>
      <div class="stat-card pending">
        <div class="stat-icon"></div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.pendingHotels }}</div>
          <div class="stat-label">승인 대기</div>
        </div>
      </div>
      <div class="stat-card approved">
        <div class="stat-icon"></div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.approvedHotels }}</div>
          <div class="stat-label">승인 완료</div>
        </div>
      </div>
      <div class="stat-card revenue">
        <div class="stat-icon"></div>
        <div class="stat-content">
          <div class="stat-number">{{ formatCurrency(stats.totalRevenue) }}</div>
          <div class="stat-label">총 매출</div>
        </div>
      </div>
    </div>

    <!-- 필터 및 검색 -->
    <div class="filters">
      <div class="filter-group">
        <input
          v-model="filters.name"
          type="text"
          placeholder="호텔명 검색..."
          class="search-input"
          @input="debounceSearch"
        />
        <select v-model="filters.status" @change="loadHotels" class="status-select">
          <option value="">모든 상태</option>
          <option value="PENDING">승인 대기</option>
          <option value="APPROVED">승인 완료</option>
          <option value="REJECTED">승인 거부</option>
          <option value="SUSPENDED">정지</option>
        </select>
        <input
          v-model="filters.city"
          type="text"
          placeholder="도시 검색..."
          class="search-input"
          @input="debounceSearch"
        />
      </div>
    </div>

    <!-- 호텔 목록 테이블 -->
    <div class="table-container">
      <table class="hotels-table">
        <thead>
          <tr>
            <th>호텔명</th>
            <th>사업자</th>
            <th>위치</th>
            <th>객실 수</th>
            <th>예약 수</th>
            <th>평점</th>
            <th>매출</th>
            <th>상태</th>
            <th>등록일</th>
            <th>작업</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="hotel in hotels.content" :key="hotel.id" :class="`status-${hotel.status.toLowerCase()}`">
            <td>
              <div class="hotel-info">
                <strong>{{ hotel.name }}</strong>
                <div class="hotel-address">{{ hotel.address }}</div>
              </div>
            </td>
            <td>
              <div class="business-info">
                <div>{{ hotel.businessName }}</div>
                <div class="business-email">{{ hotel.businessEmail }}</div>
              </div>
            </td>
            <td>{{ hotel.city }}</td>
            <td>{{ hotel.roomCount }}</td>
            <td>{{ hotel.reservationCount }}</td>
            <td>
              <div class="rating">{{ hotel.averageRating.toFixed(1) }}</div>
            </td>
            <td>{{ formatCurrency(hotel.totalRevenue) }}</td>
            <td>
              <span :class="`status-badge status-${hotel.status.toLowerCase()}`">
                {{ getStatusText(hotel.status) }}
              </span>
            </td>
            <td>{{ formatDate(hotel.createdAt) }}</td>
            <td>
              <div class="action-buttons">
                <button @click="viewHotelDetail(hotel)" class="btn-detail">상세</button>
                <button 
                  v-if="hotel.status === 'PENDING'" 
                  @click="updateHotelStatus(hotel.id, 'APPROVED')" 
                  class="btn-approve"
                >
                  승인
                </button>
                <button 
                  v-if="hotel.status === 'PENDING'" 
                  @click="showRejectModal(hotel)" 
                  class="btn-reject"
                >
                  거부
                </button>
                <button 
                  v-if="hotel.status === 'APPROVED'" 
                  @click="showSuspendModal(hotel)" 
                  class="btn-suspend"
                >
                  정지
                </button>
                <button 
                  v-if="hotel.status === 'SUSPENDED'" 
                  @click="updateHotelStatus(hotel.id, 'APPROVED')" 
                  class="btn-approve"
                >
                  해제
                </button>
                <button @click="deleteHotel(hotel.id)" class="btn-delete">삭제</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 페이지네이션 -->
    <div class="pagination">
      <button 
        @click="changePage(hotels.number - 1)" 
        :disabled="hotels.first"
        class="page-btn"
      >
        이전
      </button>
      <span class="page-info">
        {{ hotels.number + 1 }} / {{ hotels.totalPages }}
      </span>
      <button 
        @click="changePage(hotels.number + 1)" 
        :disabled="hotels.last"
        class="page-btn"
      >
        다음
      </button>
    </div>

    <!-- 호텔 상세 모달 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content large" @click.stop>
        <div class="modal-header">
          <h2>{{ selectedHotel.name }} 상세 정보</h2>
          <button @click="closeDetailModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="hotel-detail">
            <div class="detail-section">
              <h3>기본 정보</h3>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>호텔명:</label>
                  <span>{{ selectedHotel.name }}</span>
                </div>
                <div class="detail-item">
                  <label>설명:</label>
                  <span>{{ selectedHotel.description }}</span>
                </div>
                <div class="detail-item">
                  <label>주소:</label>
                  <span>{{ selectedHotel.address }}</span>
                </div>
                <div class="detail-item">
                  <label>전화번호:</label>
                  <span>{{ selectedHotel.phone }}</span>
                </div>
                <div class="detail-item">
                  <label>이메일:</label>
                  <span>{{ selectedHotel.email }}</span>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h3>사업자 정보</h3>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>사업자명:</label>
                  <span>{{ selectedHotel.businessName }}</span>
                </div>
                <div class="detail-item">
                  <label>사업자 이메일:</label>
                  <span>{{ selectedHotel.businessEmail }}</span>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h3>운영 현황</h3>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>총 객실 수:</label>
                  <span>{{ selectedHotel.roomCount }}개</span>
                </div>
                <div class="detail-item">
                  <label>총 예약 수:</label>
                  <span>{{ selectedHotel.reservationCount }}건</span>
                </div>
                <div class="detail-item">
                  <label>평균 평점:</label>
                  <span>{{ selectedHotel.averageRating.toFixed(1) }}</span>
                </div>
                <div class="detail-item">
                  <label>총 매출:</label>
                  <span>{{ formatCurrency(selectedHotel.totalRevenue) }}</span>
                </div>
              </div>
            </div>

            <div v-if="selectedHotel.rooms && selectedHotel.rooms.length > 0" class="detail-section">
              <h3>객실 목록</h3>
              <div class="rooms-list">
                <div v-for="room in selectedHotel.rooms" :key="room.id" class="room-item">
                  <div class="room-info">
                    <strong>{{ room.roomType }}</strong>
                    <span class="room-status">{{ room.status }}</span>
                  </div>
                  <div class="room-details">
                    <span>수용인원: {{ room.capacity }}명</span>
                    <span>기본가격: {{ formatCurrency(room.basePrice) }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="selectedHotel.rejectionReason" class="detail-section rejection-reason">
              <h3>거부/정지 사유</h3>
              <p>{{ selectedHotel.rejectionReason }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 거부 사유 입력 모달 -->
    <div v-if="showRejectReasonModal" class="modal-overlay" @click="closeRejectModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>호텔 승인 거부</h2>
          <button @click="closeRejectModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <p><strong>{{ rejectTargetHotel.name }}</strong> 호텔의 승인을 거부하시겠습니까?</p>
          <div class="form-group">
            <label>거부 사유:</label>
            <textarea 
              v-model="rejectReason" 
              placeholder="거부 사유를 입력해주세요..."
              class="reason-textarea"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeRejectModal" class="btn-cancel">취소</button>
          <button @click="confirmReject" class="btn-reject">거부</button>
        </div>
      </div>
    </div>

    <!-- 정지 사유 입력 모달 -->
    <div v-if="showSuspendReasonModal" class="modal-overlay" @click="closeSuspendModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>호텔 운영 정지</h2>
          <button @click="closeSuspendModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <p><strong>{{ suspendTargetHotel.name }}</strong> 호텔의 운영을 정지하시겠습니까?</p>
          <div class="form-group">
            <label>정지 사유:</label>
            <textarea 
              v-model="suspendReason" 
              placeholder="정지 사유를 입력해주세요..."
              class="reason-textarea"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeSuspendModal" class="btn-cancel">취소</button>
          <button @click="confirmSuspend" class="btn-suspend">정지</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/http.js'

export default {
  name: 'AdminHotelManagement',
  setup() {
    const hotels = ref({
      content: [],
      totalPages: 0,
      totalElements: 0,
      number: 0,
      first: true,
      last: true
    })

    const stats = reactive({
      totalHotels: 0,
      pendingHotels: 0,
      approvedHotels: 0,
      totalRevenue: 0
    })

    const filters = reactive({
      name: '',
      status: '',
      city: ''
    })

    const selectedHotel = ref({})
    const showDetailModal = ref(false)
    const showRejectReasonModal = ref(false)
    const showSuspendReasonModal = ref(false)
    const rejectTargetHotel = ref({})
    const suspendTargetHotel = ref({})
    const rejectReason = ref('')
    const suspendReason = ref('')

    let searchTimeout = null

    const loadHotels = async (page = 0) => {
      try {
        const params = {
          page,
          size: 10,
          sort: 'createdAt,desc'
        }

        if (filters.name) params.name = filters.name
        if (filters.status) params.status = filters.status
        if (filters.city) params.city = filters.city

  const response = await axios.get('/admin/hotels', { params })
        hotels.value = response.data
        
        updateStats()
      } catch (error) {
        alert('호텔 목록을 불러오는데 실패했습니다.')
      }
    }

    const updateStats = () => {
      const content = hotels.value.content
      stats.totalHotels = content.length
      stats.pendingHotels = content.filter(h => h.status === 'PENDING').length
      stats.approvedHotels = content.filter(h => h.status === 'APPROVED').length
      stats.totalRevenue = content.reduce((sum, h) => sum + (h.totalRevenue || 0), 0)
    }

    const debounceSearch = () => {
      clearTimeout(searchTimeout)
      searchTimeout = setTimeout(() => {
        loadHotels(0)
      }, 500)
    }

    const changePage = (page) => {
      loadHotels(page)
    }

    const viewHotelDetail = async (hotel) => {
      try {
  const response = await axios.get(`/admin/hotels/${hotel.id}`)
        selectedHotel.value = response.data
        showDetailModal.value = true
      } catch (error) {
        alert('호텔 상세 정보를 불러오는데 실패했습니다.')
      }
    }

    const updateHotelStatus = async (hotelId, status, reason = '') => {
      try {
  await axios.put(`/admin/hotels/${hotelId}/status`, {
          status,
          reason
        })
        
        alert(`호텔 상태가 ${getStatusText(status)}(으)로 변경되었습니다.`)
        loadHotels(hotels.value.number)
      } catch (error) {
        alert('호텔 상태 변경에 실패했습니다.')
      }
    }

    const showRejectModal = (hotel) => {
      rejectTargetHotel.value = hotel
      rejectReason.value = ''
      showRejectReasonModal.value = true
    }

    const closeRejectModal = () => {
      showRejectReasonModal.value = false
      rejectTargetHotel.value = {}
      rejectReason.value = ''
    }

    const confirmReject = () => {
      if (!rejectReason.value.trim()) {
        alert('거부 사유를 입력해주세요.')
        return
      }
      
      updateHotelStatus(rejectTargetHotel.value.id, 'REJECTED', rejectReason.value)
      closeRejectModal()
    }

    const showSuspendModal = (hotel) => {
      suspendTargetHotel.value = hotel
      suspendReason.value = ''
      showSuspendReasonModal.value = true
    }

    const closeSuspendModal = () => {
      showSuspendReasonModal.value = false
      suspendTargetHotel.value = {}
      suspendReason.value = ''
    }

    const confirmSuspend = () => {
      if (!suspendReason.value.trim()) {
        alert('정지 사유를 입력해주세요.')
        return
      }
      
      updateHotelStatus(suspendTargetHotel.value.id, 'SUSPENDED', suspendReason.value)
      closeSuspendModal()
    }

    const closeDetailModal = () => {
      showDetailModal.value = false
      selectedHotel.value = {}
    }

    const deleteHotel = async (hotelId) => {
      if (!confirm('정말로 이 호텔을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
        return
      }

      try {
  await axios.delete(`/admin/hotels/${hotelId}`)
        alert('호텔이 삭제되었습니다.')
        loadHotels(hotels.value.number)
      } catch (error) {
        alert('호텔 삭제에 실패했습니다.')
      }
    }

    const getStatusText = (status) => {
      const statusMap = {
        'PENDING': '승인 대기',
        'APPROVED': '승인 완료',
        'REJECTED': '승인 거부',
        'SUSPENDED': '운영 정지'
      }
      return statusMap[status] || status
    }

    const formatDate = (dateString) => {
      return new Date(dateString).toLocaleDateString('ko-KR')
    }

    const formatCurrency = (amount) => {
      if (!amount) return '₩0'
      return `₩${amount.toLocaleString()}`
    }

    onMounted(() => {
      loadHotels()
    })

    return {
      hotels,
      stats,
      filters,
      selectedHotel,
      showDetailModal,
      showRejectReasonModal,
      showSuspendReasonModal,
      rejectTargetHotel,
      suspendTargetHotel,
      rejectReason,
      suspendReason,
      loadHotels,
      debounceSearch,
      changePage,
      viewHotelDetail,
      updateHotelStatus,
      showRejectModal,
      closeRejectModal,
      confirmReject,
      showSuspendModal,
      closeSuspendModal,
      confirmSuspend,
      closeDetailModal,
      deleteHotel,
      getStatusText,
      formatDate,
      formatCurrency
    }
  }
}
</script>

<style scoped>
.hotel-management {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

/* page-header uses global styles */

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-left: 4px solid #e0e0e0;
}

.stat-card.pending {
  border-left-color: #ff9800;
}

.stat-card.approved {
  border-left-color: #4caf50;
}

.stat-card.revenue {
  border-left-color: #2196f3;
}

.stat-icon {
  font-size: 32px;
  margin-right: 16px;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.filters {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-group {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.search-input, .status-select {
  flex: 1;
  min-width: 200px;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.search-input:focus, .status-select:focus {
  outline: none;
  border-color: #2196f3;
}

.table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.hotels-table {
  width: 100%;
  border-collapse: collapse;
}

.hotels-table th {
  background: #f8f9fa;
  padding: 16px 12px;
  text-align: left;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #e0e0e0;
}

.hotels-table td {
  padding: 16px 12px;
  border-bottom: 1px solid #f0f0f0;
}

.hotels-table tr:hover {
  background: #f8f9fa;
}

.hotels-table tr.status-pending {
  background: #fff8e1;
}

.hotel-info strong {
  color: #333;
  font-size: 16px;
}

.hotel-address {
  color: #666;
  font-size: 14px;
  margin-top: 4px;
}

.business-info {
  font-size: 14px;
}

.business-email {
  color: #666;
  margin-top: 4px;
}

.rating {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stars {
  color: #ffc107;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.status-badge.status-pending {
  background: #fff3cd;
  color: #856404;
}

.status-badge.status-approved {
  background: #d4edda;
  color: #155724;
}

.status-badge.status-rejected {
  background: #f8d7da;
  color: #721c24;
}

.status-badge.status-suspended {
  background: #f8d7da;
  color: #721c24;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-buttons button {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-detail {
  background: #e3f2fd;
  color: #1976d2;
}

.btn-detail:hover {
  background: #bbdefb;
}

.btn-approve {
  background: #e8f5e8;
  color: #2e7d32;
}

.btn-approve:hover {
  background: #c8e6c9;
}

.btn-reject {
  background: #ffebee;
  color: #c62828;
}

.btn-reject:hover {
  background: #ffcdd2;
}

.btn-suspend {
  background: #fff3e0;
  color: #ef6c00;
}

.btn-suspend:hover {
  background: #ffe0b2;
}

.btn-delete {
  background: #ffebee;
  color: #d32f2f;
}

.btn-delete:hover {
  background: #ffcdd2;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
}

.page-btn {
  padding: 10px 16px;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  border-color: #2196f3;
  color: #2196f3;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-weight: 600;
  color: #333;
}

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.modal-content.large {
  max-width: 800px;
}

.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel {
  padding: 10px 20px;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #333;
}

.reason-textarea {
  width: 100%;
  min-height: 100px;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
}

.reason-textarea:focus {
  outline: none;
  border-color: #2196f3;
}

/* 호텔 상세 정보 스타일 */
.detail-section {
  margin-bottom: 24px;
}

.detail-section h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e0e0e0;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
}

.detail-item label {
  font-weight: 600;
  color: #666;
  font-size: 14px;
  margin-bottom: 4px;
}

.detail-item span {
  color: #333;
  font-size: 16px;
}

.rooms-list {
  display: grid;
  gap: 12px;
}

.room-item {
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #f8f9fa;
}

.room-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.room-status {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  background: #e3f2fd;
  color: #1976d2;
}

.room-details {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #666;
}

.rejection-reason {
  background: #ffebee;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #f44336;
}

.rejection-reason p {
  margin: 0;
  color: #c62828;
}

@media (max-width: 768px) {
  .hotels-table {
    font-size: 14px;
  }
  
  .hotels-table th,
  .hotels-table td {
    padding: 8px 6px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-group {
    flex-direction: column;
  }
}
</style>