<template>
  <div class="user-management">
    <div class="page-header">
      <h1>사용자 관리</h1>
      <p class="page-description">전체 사용자를 조회하고 역할을 관리할 수 있습니다.</p>
    </div>

    <!-- 검색 필터 (네이티브) -->
    <div class="search-section">
      <form class="search-form" @submit.prevent="searchUsers">
        <div class="search-group">
          <label>이름</label>
          <input class="search-input" v-model="searchForm.name" placeholder="사용자 이름 검색" @keyup.enter="searchUsers" />
        </div>
        <div class="search-group">
          <label>이메일</label>
          <input class="search-input" v-model="searchForm.email" placeholder="이메일 검색" @keyup.enter="searchUsers" />
        </div>
        <div class="search-group">
          <label>역할</label>
          <select class="search-select" v-model="searchForm.role">
            <option value="">전체</option>
            <option value="USER">일반 사용자</option>
            <option value="BUSINESS">사업자</option>
            <option value="ADMIN">관리자</option>
          </select>
        </div>
        <div class="search-buttons">
          <button type="submit" class="btn btn-primary">검색</button>
          <button type="button" class="btn btn-secondary" @click="resetSearch">초기화</button>
        </div>
      </form>
    </div>

    <!-- 사용자 목록 (네이티브) -->
    <div class="table-section">
      <div class="table-header">
        <h2>사용자 목록</h2>
        <div class="total-count">총 {{ totalElements }}명</div>
      </div>

      <div v-if="loading" class="loading">불러오는 중…</div>
      <div v-else-if="!users || users.length === 0" class="no-data">검색 결과가 없습니다.</div>
      <table v-else class="user-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>이름</th>
            <th>이메일</th>
            <th>현재 역할</th>
            <th>상태</th>
            <th>가입 방식</th>
            <th>가입일</th>
            <th>역할 변경</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in users" :key="row.id" class="user-row">
            <td>{{ row.id }}</td>
            <td>{{ row.name }}</td>
            <td>{{ row.email }}</td>
            <td>
              <span :class="['role-badge', roleBadgeClass(row.role)]">{{ getRoleLabel(row.role) }}</span>
            </td>
            <td>
              <span :class="['status-badge', statusBadgeClass(row.status)]">{{ getStatusLabel(row.status) }}</span>
            </td>
            <td>
              <span class="provider-badge">{{ getProviderLabel(row.provider) }}</span>
            </td>
            <td>{{ formatDate(row.createdAt) }}</td>
            <td>
              <select class="role-select" :disabled="row.id === currentUserId" :value="row.role" @change="e => updateUserRole(row, e.target.value)">
                <option value="USER">일반 사용자</option>
                <option value="BUSINESS">사업자</option>
                <option value="ADMIN">관리자</option>
              </select>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pagination-section" v-if="totalPages > 1">
        <div class="pagination">
          <button class="page-btn" :disabled="currentPage === 0" @click="changePage(currentPage - 1)">이전</button>
          <span class="page-info">{{ currentPage + 1 }} / {{ totalPages }}</span>
          <button class="page-btn" :disabled="currentPage >= totalPages - 1" @click="changePage(currentPage + 1)">다음</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../../api/http'

export default {
  name: 'UserManagement',
  data() {
    return {
      users: [],
      loading: false,
      searchForm: {
        name: '',
        email: '',
        role: ''
      },
      currentPage: 0,
      totalPages: 0,
      totalElements: 0,
      pageSize: 20,
      currentUserId: null // TODO: 현재 로그인한 사용자 ID
    }
  },
  mounted() {
    this.loadUsers();
    // TODO: 현재 사용자 정보 로드
    // this.loadCurrentUser();
  },
  methods: {
    async loadUsers() {
      this.loading = true;
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize
        };
        
        if (this.searchForm.name) params.name = this.searchForm.name;
        if (this.searchForm.email) params.email = this.searchForm.email;
        if (this.searchForm.role) params.role = this.searchForm.role;
        
        const response = await api.get('/admin/users', { params });
        
        const data = response.data;
        this.users = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.currentPage = data.number;
        
      } catch (error) {
        alert('사용자 목록을 불러오는데 실패했습니다.');
      } finally {
        this.loading = false;
      }
    },
    
    async updateUserRole(user, newRole) {
      if (user.role === newRole) return;
      
      const confirmMessage = `${user.name}님의 역할을 "${this.getRoleLabel(newRole)}"로 변경하시겠습니까?`;
      const ok = window.confirm(confirmMessage)
      if (!ok) {
        // 선택된 값을 원래대로 되돌림
        this.$forceUpdate();
        return;
      }
      
      try {
        const response = await api.put(`/admin/users/${user.id}/role`, { role: newRole });
        
        // 로컬 데이터 업데이트
        user.role = newRole;
        alert(`${user.name}님의 역할이 성공적으로 변경되었습니다.`);
        
      } catch (error) {
        alert(error.response?.data?.error || '역할 변경에 실패했습니다.');
        // 실패 시 원래 값으로 되돌림
        this.$forceUpdate();
      }
    },
    
    searchUsers() {
      this.currentPage = 0;
      this.loadUsers();
    },
    
    resetSearch() {
      this.searchForm = {
        name: '',
        email: '',
        role: ''
      };
      this.currentPage = 0;
      this.loadUsers();
    },
    
    changePage(page) {
      if (page >= 0 && page < this.totalPages) {
        this.currentPage = page;
        this.loadUsers();
      }
    },
    
    getRoleLabel(role) {
      const roleLabels = {
        'USER': '일반 사용자',
        'BUSINESS': '사업자',
        'ADMIN': '관리자'
      };
      return roleLabels[role] || role;
    },
    
    getStatusLabel(status) {
      const statusLabels = {
        'ACTIVE': '활성',
        'INACTIVE': '비활성',
        'SUSPENDED': '정지'
      };
      return statusLabels[status] || status;
    },
    
    getProviderLabel(provider) {
      const providerLabels = {
        'LOCAL': '이메일',
        'GOOGLE': '구글',
        'NAVER': '네이버',
        'KAKAO': '카카오'
      };
      return providerLabels[provider] || provider;
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
    },
    roleTagType(role) {
      const map = { USER: 'info', BUSINESS: 'warning', ADMIN: 'success' }
      return map[role] || 'info'
    },
    statusTagType(status) {
      const map = { ACTIVE: 'success', INACTIVE: 'info', SUSPENDED: 'danger' }
      return map[status] || 'info'
    },
    roleBadgeClass(role) {
      const map = { USER: 'role-user', BUSINESS: 'role-business', ADMIN: 'role-admin' }
      return map[role] || 'role-user'
    },
    statusBadgeClass(status) {
      const map = { ACTIVE: 'status-active', INACTIVE: 'status-inactive', SUSPENDED: 'status-suspended' }
      return map[status] || 'status-active'
    }
  }
}
</script>

<style scoped>


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

/* 검색 섹션 */
.search-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.search-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  align-items: end;
}

.search-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.search-group label {
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
}

.search-input,
.search-select {
  padding: 10px 12px;
  border: 2px solid #e9ecef;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.search-input:focus,
.search-select:focus {
  outline: none;
  border-color: #3498db;
}

.search-buttons {
  display: flex;
  gap: 10px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background-color: #3498db;
  color: white;
}

.btn-primary:hover {
  background-color: #2980b9;
}

.btn-secondary {
  background-color: #95a5a6;
  color: white;
}

.btn-secondary:hover {
  background-color: #7f8c8d;
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

.user-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}

.user-table th,
.user-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e9ecef;
}

/* 줄바꿈 방지: 역할/상태/가입방식/역할변경 열 */
.user-table td:nth-child(4),
.user-table td:nth-child(5),
.user-table td:nth-child(6),
.user-table td:nth-child(8) {
  white-space: nowrap;
}

.user-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.user-table td {
  font-size: 14px;
  color: #2c3e50;
}

.user-row:hover {
  background-color: #f8f9fa;
}

/* 배지 스타일 */
.role-badge,
.status-badge,
.provider-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
}

.role-user { background-color: #e3f2fd; color: #1976d2; }
.role-business { background-color: #f3e5f5; color: #7b1fa2; }
.role-admin { background-color: #ffebee; color: #d32f2f; }

.status-active { background-color: #e8f5e8; color: #2e7d32; }
.status-inactive { background-color: #fff3e0; color: #f57c00; }
.status-suspended { background-color: #ffebee; color: #d32f2f; }

.provider-badge {
  background-color: #f5f5f5;
  color: #616161;
}

.role-select {
  padding: 6px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 12px;
  min-width: 100px;
  white-space: nowrap;
}

.role-select:disabled {
  background-color: #f5f5f5;
  color: #999;
  cursor: not-allowed;
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

/* 반응형 */
@media (max-width: 768px) {
  .search-form {
    grid-template-columns: 1fr;
  }
  
  .search-buttons {
    justify-content: center;
  }
  
  .table-header {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }
  
  .user-table {
    font-size: 12px;
  }
  
  .user-table th,
  .user-table td {
    padding: 8px 4px;
  }
}
</style>