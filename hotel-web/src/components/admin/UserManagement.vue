<template>
  <div class="user-management">
    <div class="page-header">
      <h1>사용자 관리</h1>
      <p class="page-description">전체 사용자를 조회하고 역할을 관리할 수 있습니다.</p>
    </div>

    <!-- 검색 필터 -->
    <div class="search-section">
      <div class="search-form">
        <div class="search-group">
          <label>이름</label>
          <input
            v-model="searchForm.name"
            type="text"
            placeholder="사용자 이름 검색"
            @keyup.enter="searchUsers"
            class="search-input"
          />
        </div>
        
        <div class="search-group">
          <label>이메일</label>
          <input
            v-model="searchForm.email"
            type="text"
            placeholder="이메일 검색"
            @keyup.enter="searchUsers"
            class="search-input"
          />
        </div>
        
        <div class="search-group">
          <label>역할</label>
          <select v-model="searchForm.role" class="search-select">
            <option value="">전체</option>
            <option value="USER">일반 사용자</option>
            <option value="BUSINESS">사업자</option>
            <option value="ADMIN">관리자</option>
          </select>
        </div>
        
        <div class="search-buttons">
          <button @click="searchUsers" class="btn btn-primary">검색</button>
          <button @click="resetSearch" class="btn btn-secondary">초기화</button>
        </div>
      </div>
    </div>

    <!-- 사용자 목록 테이블 -->
    <div class="table-section">
      <div class="table-header">
        <h2>사용자 목록</h2>
        <span class="total-count">총 {{ totalElements }}명</span>
      </div>

      <div v-if="loading" class="loading">
        <p>데이터를 불러오는 중...</p>
      </div>

      <div v-else-if="users.length === 0" class="no-data">
        <p>검색 결과가 없습니다.</p>
      </div>

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
          <tr v-for="user in users" :key="user.id" class="user-row">
            <td>{{ user.id }}</td>
            <td>{{ user.name }}</td>
            <td>{{ user.email }}</td>
            <td>
              <span :class="['role-badge', 'role-' + user.role.toLowerCase()]">
                {{ getRoleLabel(user.role) }}
              </span>
            </td>
            <td>
              <span :class="['status-badge', 'status-' + user.status.toLowerCase()]">
                {{ getStatusLabel(user.status) }}
              </span>
            </td>
            <td>
              <span class="provider-badge">
                {{ getProviderLabel(user.provider) }}
              </span>
            </td>
            <td>{{ formatDate(user.createdAt) }}</td>
            <td>
              <select
                :value="user.role"
                @change="updateUserRole(user, $event.target.value)"
                :disabled="user.id === currentUserId"
                class="role-select"
              >
                <option value="USER">일반 사용자</option>
                <option value="BUSINESS">사업자</option>
                <option value="ADMIN">관리자</option>
              </select>
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
  </div>
</template>

<script>
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
        const params = new URLSearchParams();
        params.append('page', this.currentPage);
        params.append('size', this.pageSize);
        
        if (this.searchForm.name) params.append('name', this.searchForm.name);
        if (this.searchForm.email) params.append('email', this.searchForm.email);
        if (this.searchForm.role) params.append('role', this.searchForm.role);
        
        const response = await fetch(`/api/admin/users?${params}`, {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          }
        });
        
        if (!response.ok) {
          throw new Error('사용자 목록 조회 실패');
        }
        
        const data = await response.json();
        this.users = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.currentPage = data.number;
        
      } catch (error) {
        console.error('사용자 목록 로드 실패:', error);
        alert('사용자 목록을 불러오는데 실패했습니다.');
      } finally {
        this.loading = false;
      }
    },
    
    async updateUserRole(user, newRole) {
      if (user.role === newRole) return;
      
      const confirmMessage = `${user.name}님의 역할을 "${this.getRoleLabel(newRole)}"로 변경하시겠습니까?`;
      if (!confirm(confirmMessage)) {
        // 선택된 값을 원래대로 되돌림
        this.$forceUpdate();
        return;
      }
      
      try {
        const response = await fetch(`/api/admin/users/${user.id}/role`, {
          method: 'PUT',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ role: newRole })
        });
        
        if (!response.ok) {
          const errorData = await response.json();
          throw new Error(errorData.error || '역할 변경 실패');
        }
        
        const result = await response.json();
        
        // 로컬 데이터 업데이트
        user.role = newRole;
        
        alert(`${user.name}님의 역할이 성공적으로 변경되었습니다.`);
        
      } catch (error) {
        console.error('역할 변경 실패:', error);
        alert(error.message);
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
    }
  }
}
</script>

<style scoped>
.user-management {
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