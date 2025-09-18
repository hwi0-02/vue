<template>
  <div class="business-management">
    <div class="page-header">
      <h1>사업자 승인 관리</h1>
      <p class="page-description">사업자 등록 신청을 검토하고 승인/반려 처리를 할 수 있습니다.</p>
    </div>

    <!-- 상태별 요약 카드 -->
    <div class="summary-cards">
      <div class="card pending">
        <div class="card-icon">⏳</div>
        <div class="card-content">
          <h3>승인 대기</h3>
          <p class="card-number">{{ pendingCount }}</p>
        </div>
      </div>
      
      <div class="card approved">
        <div class="card-icon">✅</div>
        <div class="card-content">
          <h3>승인 완료</h3>
          <p class="card-number">{{ approvedCount }}</p>
        </div>
      </div>
      
      <div class="card rejected">
        <div class="card-icon">❌</div>
        <div class="card-content">
          <h3>반려</h3>
          <p class="card-number">{{ rejectedCount }}</p>
        </div>
      </div>
      
      <div class="card suspended">
        <div class="card-icon">🚫</div>
        <div class="card-content">
          <h3>정지</h3>
          <p class="card-number">{{ suspendedCount }}</p>
        </div>
      </div>
    </div>

    <!-- 필터 섹션 -->
    <div class="filter-section">
      <div class="filter-form">
        <div class="filter-group">
          <label>상태 필터</label>
          <select v-model="selectedStatus" @change="loadBusinesses" class="filter-select">
            <option value="">전체</option>
            <option value="PENDING">승인 대기</option>
            <option value="APPROVED">승인 완료</option>
            <option value="REJECTED">반려</option>
            <option value="SUSPENDED">정지</option>
          </select>
        </div>
        
        <div class="filter-buttons">
          <button @click="resetFilters" class="btn btn-secondary">필터 초기화</button>
        </div>
      </div>
    </div>

    <!-- 사업자 목록 테이블 -->
    <div class="table-section">
      <div class="table-header">
        <h2>사업자 목록</h2>
        <span class="total-count">총 {{ totalElements }}개</span>
      </div>

      <div v-if="loading" class="loading">
        <p>데이터를 불러오는 중...</p>
      </div>

      <div v-else-if="businesses.length === 0" class="no-data">
        <p>등록된 사업자가 없습니다.</p>
      </div>

      <table v-else class="business-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>상태</th>
            <th>사업자명</th>
            <th>사업자번호</th>
            <th>연락처</th>
            <th>주소</th>
            <th>신청일</th>
            <th>관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="business in businesses" :key="business.id" :class="getRowClass(business.status)">
            <td>{{ business.id }}</td>
            <td>
              <span :class="['status-badge', 'status-' + business.status.toLowerCase()]">
                {{ getStatusLabel(business.status) }}
              </span>
            </td>
            <td>
              <div class="business-info">
                <strong>{{ business.businessName }}</strong>
                <small class="business-user">{{ business.userName }} ({{ business.userEmail }})</small>
              </div>
            </td>
            <td>{{ business.businessNumber }}</td>
            <td>{{ business.phone }}</td>
            <td>{{ business.address }}</td>
            <td>{{ formatDate(business.createdAt) }}</td>
            <td>
              <div class="action-buttons">
                <button
                  v-if="business.status === 'PENDING'"
                  @click="updateStatus(business, 'APPROVED')"
                  class="btn btn-approve"
                  title="승인"
                >
                  ✅ 승인
                </button>
                
                <button
                  v-if="business.status === 'PENDING'"
                  @click="updateStatus(business, 'REJECTED')"
                  class="btn btn-reject"
                  title="반려"
                >
                  ❌ 반려
                </button>
                
                <button
                  v-if="business.status === 'APPROVED'"
                  @click="updateStatus(business, 'SUSPENDED')"
                  class="btn btn-suspend"
                  title="정지"
                >
                  🚫 정지
                </button>
                
                <button
                  v-if="business.status === 'SUSPENDED'"
                  @click="updateStatus(business, 'APPROVED')"
                  class="btn btn-activate"
                  title="활성화"
                >
                  ✅ 활성화
                </button>
                
                <button
                  @click="viewDetails(business)"
                  class="btn btn-info"
                  title="상세보기"
                >
                  📄 상세
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 페이지네이션 -->
    <div class="pagination-section" v-if="totalPages > 1">
      <div class="pagination">
        <button
          @click="changePage(0)"
          :disabled="currentPage === 0"
          class="page-btn"
        >
          ⏮️ 처음
        </button>
        
        <button
          @click="changePage(currentPage - 1)"
          :disabled="currentPage === 0"
          class="page-btn"
        >
          ⬅️ 이전
        </button>
        
        <span class="page-info">
          {{ currentPage + 1 }} / {{ totalPages }} 페이지
        </span>
        
        <button
          @click="changePage(currentPage + 1)"
          :disabled="currentPage >= totalPages - 1"
          class="page-btn"
        >
          다음 ➡️
        </button>
        
        <button
          @click="changePage(totalPages - 1)"
          :disabled="currentPage >= totalPages - 1"
          class="page-btn"
        >
          마지막 ⏭️
        </button>
      </div>
    </div>

    <!-- 상세 정보 모달 -->
    <div v-if="selectedBusiness" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>사업자 상세 정보</h3>
          <button @click="closeModal" class="close-btn">✕</button>
        </div>
        
        <div class="modal-body">
          <div class="detail-section">
            <h4>기본 정보</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <label>사업자명:</label>
                <span>{{ selectedBusiness.businessName }}</span>
              </div>
              <div class="detail-item">
                <label>사업자번호:</label>
                <span>{{ selectedBusiness.businessNumber }}</span>
              </div>
              <div class="detail-item">
                <label>사용자 정보:</label>
                <span>{{ selectedBusiness.userName }} ({{ selectedBusiness.userEmail }})</span>
              </div>
              <div class="detail-item">
                <label>연락처:</label>
                <span>{{ selectedBusiness.phone }}</span>
              </div>
              <div class="detail-item">
                <label>주소:</label>
                <span>{{ selectedBusiness.address }}</span>
              </div>
              <div class="detail-item">
                <label>현재 상태:</label>
                <span :class="['status-badge', 'status-' + selectedBusiness.status.toLowerCase()]">
                  {{ getStatusLabel(selectedBusiness.status) }}
                </span>
              </div>
            </div>
          </div>
          
          <div class="detail-section">
            <h4>신청 정보</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <label>신청일:</label>
                <span>{{ formatDate(selectedBusiness.createdAt) }}</span>
              </div>
              <div class="detail-item">
                <label>최종 수정일:</label>
                <span>{{ formatDate(selectedBusiness.updatedAt) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="closeModal" class="btn btn-secondary">닫기</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HotelManagement',
  data() {
    return {
      businesses: [],
      loading: false,
      selectedStatus: '',
      currentPage: 0,
      totalPages: 0,
      totalElements: 0,
      pageSize: 20,
      selectedBusiness: null,
      
      // 상태별 카운트
      pendingCount: 0,
      approvedCount: 0,
      rejectedCount: 0,
      suspendedCount: 0
    }
  },
  mounted() {
    this.loadBusinesses();
    this.loadStatusCounts();
  },
  methods: {
    async loadBusinesses() {
      this.loading = true;
      try {
        const params = new URLSearchParams();
        params.append('page', this.currentPage);
        params.append('size', this.pageSize);
        
        if (this.selectedStatus) {
          params.append('status', this.selectedStatus);
        }
        
        const response = await fetch(`/api/admin/businesses?${params}`, {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          }
        });
        
        if (!response.ok) {
          throw new Error('사업자 목록 조회 실패');
        }
        
        const data = await response.json();
        this.businesses = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.currentPage = data.number;
        
      } catch (error) {
        console.error('사업자 목록 로드 실패:', error);
        alert('사업자 목록을 불러오는데 실패했습니다.');
      } finally {
        this.loading = false;
      }
    },
    
    async loadStatusCounts() {
      // TODO: 상태별 카운트 API 구현 후 실제 데이터로 교체
      this.pendingCount = 8;
      this.approvedCount = 45;
      this.rejectedCount = 3;
      this.suspendedCount = 2;
    },
    
    async updateStatus(business, newStatus) {
      const statusMessages = {
        'APPROVED': '승인',
        'REJECTED': '반려',
        'SUSPENDED': '정지'
      };
      
      const confirmMessage = `${business.businessName}를 "${statusMessages[newStatus]}" 처리하시겠습니까?`;
      if (!confirm(confirmMessage)) {
        return;
      }
      
      try {
        const response = await fetch(`/api/admin/businesses/${business.id}/status`, {
          method: 'PUT',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ status: newStatus })
        });
        
        if (!response.ok) {
          const errorData = await response.json();
          throw new Error(errorData.error || '상태 변경 실패');
        }
        
        // 로컬 데이터 업데이트
        business.status = newStatus;
        
        alert(`${business.businessName}가 성공적으로 ${statusMessages[newStatus]} 처리되었습니다.`);
        
        // 카운트 다시 로드
        this.loadStatusCounts();
        
      } catch (error) {
        console.error('상태 변경 실패:', error);
        alert(error.message);
      }
    },
    
    viewDetails(business) {
      this.selectedBusiness = business;
    },
    
    closeModal() {
      this.selectedBusiness = null;
    },
    
    resetFilters() {
      this.selectedStatus = '';
      this.currentPage = 0;
      this.loadBusinesses();
    },
    
    changePage(page) {
      if (page >= 0 && page < this.totalPages) {
        this.currentPage = page;
        this.loadBusinesses();
      }
    },
    
    getStatusLabel(status) {
      const statusLabels = {
        'PENDING': '승인 대기',
        'APPROVED': '승인 완료',
        'REJECTED': '반려',
        'SUSPENDED': '정지'
      };
      return statusLabels[status] || status;
    },
    
    getRowClass(status) {
      return status === 'PENDING' ? 'pending-row' : '';
    },
    
    formatDate(dateString) {
      const date = new Date(dateString);
      return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  }
}
</script>

<style scoped>
.business-management {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
}

.page-description {
  margin: 0;
  color: #7f8c8d;
  font-size: 16px;
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
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.2s ease;
}

.card:hover {
  transform: translateY(-2px);
}

.card.pending { border-left: 4px solid #f39c12; }
.card.approved { border-left: 4px solid #27ae60; }
.card.rejected { border-left: 4px solid #e74c3c; }
.card.suspended { border-left: 4px solid #9b59b6; }

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

/* 필터 섹션 */
.filter-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.filter-form {
  display: flex;
  align-items: end;
  gap: 20px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-group label {
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
}

.filter-select {
  padding: 10px 12px;
  border: 2px solid #e9ecef;
  border-radius: 6px;
  font-size: 14px;
  min-width: 150px;
}

/* 테이블 섹션 */
.table-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}

.total-count {
  font-size: 14px;
  color: #7f8c8d;
  font-weight: 500;
}

.loading,
.no-data {
  text-align: center;
  padding: 40px;
  color: #7f8c8d;
}

.business-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}

.business-table th,
.business-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e9ecef;
}

.business-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.business-table td {
  font-size: 14px;
  color: #2c3e50;
}

.business-table tr:hover {
  background-color: #f8f9fa;
}

.pending-row {
  background-color: #fff8e1 !important;
}

.business-info strong {
  display: block;
  margin-bottom: 4px;
}

.business-user {
  color: #7f8c8d;
  font-size: 12px;
}

/* 상태 배지 */
.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending { background-color: #fff3cd; color: #856404; }
.status-approved { background-color: #d4edda; color: #155724; }
.status-rejected { background-color: #f8d7da; color: #721c24; }
.status-suspended { background-color: #e2d9f3; color: #5a2d82; }

/* 액션 버튼 */
.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.btn {
  padding: 6px 10px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.btn-approve { background-color: #27ae60; color: white; }
.btn-reject { background-color: #e74c3c; color: white; }
.btn-suspend { background-color: #9b59b6; color: white; }
.btn-activate { background-color: #3498db; color: white; }
.btn-info { background-color: #95a5a6; color: white; }
.btn-secondary { background-color: #6c757d; color: white; }

.btn:hover {
  opacity: 0.8;
  transform: translateY(-1px);
}

/* 페이지네이션 */
.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-btn {
  padding: 8px 12px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background-color: #3498db;
  color: white;
  border-color: #3498db;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  padding: 0 15px;
  font-weight: 500;
  color: #2c3e50;
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
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #7f8c8d;
}

.modal-body {
  padding: 20px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item label {
  font-weight: 500;
  color: #7f8c8d;
  font-size: 12px;
}

.detail-item span {
  color: #2c3e50;
  font-size: 14px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
}

/* 반응형 */
@media (max-width: 768px) {
  .filter-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .business-table {
    font-size: 12px;
  }
  
  .business-table th,
  .business-table td {
    padding: 8px 4px;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>