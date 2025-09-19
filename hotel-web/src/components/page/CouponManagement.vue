<template>
  <div class="coupon-management">
    <div class="management-header">
      <h2>쿠폰 관리</h2>
      <button class="btn btn-primary" @click="showCreateModal = true">쿠폰 생성</button>
    </div>

    <!-- 통계 카드 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon total"></div>
        <div class="stat-content">
          <h3>{{ stats.totalCoupons || 0 }}</h3>
          <p>전체 쿠폰</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon active"></div>
        <div class="stat-content">
          <h3>{{ stats.activeCoupons || 0 }}</h3>
          <p>활성 쿠폰</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon inactive"></div>
        <div class="stat-content">
          <h3>{{ stats.inactiveCoupons || 0 }}</h3>
          <p>비활성 쿠폰</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon expired"></div>
        <div class="stat-content">
          <h3>{{ stats.expiredCoupons || 0 }}</h3>
          <p>만료된 쿠폰</p>
        </div>
      </div>
    </div>

    <!-- 필터 섹션 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-group">
          <label>상태</label>
          <select v-model="filters.status" @change="loadCoupons">
            <option value="">전체</option>
            <option value="ACTIVE">활성</option>
            <option value="INACTIVE">비활성</option>
          </select>
        </div>
        <div class="filter-group">
          <label>할인 타입</label>
          <select v-model="filters.discountType" @change="loadCoupons">
            <option value="">전체</option>
            <option value="PERCENT">퍼센트</option>
            <option value="FIXED">고정금액</option>
          </select>
        </div>
        <div class="filter-group">
          <label>쿠폰 코드</label>
          <input 
            type="text" 
            v-model="filters.code" 
            placeholder="코드 검색"
            @input="debounceSearch"
          />
        </div>
        <div class="filter-group">
          <label>쿠폰명</label>
          <input 
            type="text" 
            v-model="filters.name" 
            placeholder="쿠폰명 검색"
            @input="debounceSearch"
          />
        </div>
        <div class="filter-group">
          <button class="btn btn-secondary" @click="resetFilters">초기화</button>
        </div>
      </div>
    </div>

    <!-- 쿠폰 테이블 -->
    <div class="table-container">
      <table class="coupon-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>코드</th>
            <th>쿠폰명</th>
            <th>할인</th>
            <th>상태</th>
            <th>유효기간</th>
            <th>사용량</th>
            <th>생성일</th>
            <th>작업</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="coupon in coupons" :key="coupon.id">
            <td>{{ coupon.id }}</td>
            <td>
              <span class="coupon-code">{{ coupon.code }}</span>
            </td>
            <td>{{ coupon.name }}</td>
            <td>
              <span class="discount-display">{{ coupon.discountDisplay }}</span>
            </td>
            <td>
              <span 
                class="status-badge" 
                :class="coupon.status.toLowerCase()"
              >
                {{ getStatusText(coupon.status) }}
              </span>
            </td>
            <td>
              <div class="validity-info">
                <div>{{ formatDate(coupon.validFrom) }}</div>
                <div>~ {{ formatDate(coupon.validUntil) }}</div>
                <span 
                  v-if="!coupon.isValid" 
                  class="expired-badge"
                >
                  만료됨
                </span>
              </div>
            </td>
            <td>
              <div class="usage-info">
                <span>{{ coupon.usedCount }}</span>
                <span v-if="coupon.usageLimit">/ {{ coupon.usageLimit }}</span>
                <span v-else>/ 무제한</span>
                <div v-if="coupon.remainingUsage !== null" class="remaining">
                  (남은 횟수: {{ coupon.remainingUsage }})
                </div>
              </div>
            </td>
            <td>{{ formatDate(coupon.createdAt) }}</td>
            <td>
              <div class="action-buttons">
                <button class="btn btn-sm btn-outline" @click="editCoupon(coupon)" title="수정">수정</button>
                <button 
                  class="btn btn-sm btn-warning"
                  @click="toggleCouponStatus(coupon)"
                  :title="coupon.status === 'ACTIVE' ? '비활성화' : '활성화'"
                >{{ coupon.status === 'ACTIVE' ? '비활성화' : '활성화' }}</button>
                <button 
                  class="btn btn-sm btn-danger"
                  @click="deleteCoupon(coupon)"
                  :disabled="coupon.usedCount > 0"
                  title="삭제"
                >삭제</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 페이지네이션 -->
    <div class="pagination-container">
      <div class="pagination-info">
        총 {{ pagination.totalElements }}개 중 
        {{ pagination.size * pagination.number + 1 }}-{{ Math.min(pagination.size * (pagination.number + 1), pagination.totalElements) }}개 표시
      </div>
      <div class="pagination">
        <button 
          @click="goToPage(0)" 
          :disabled="pagination.first"
          class="btn btn-sm"
        >
          처음
        </button>
        <button 
          @click="goToPage(pagination.number - 1)" 
          :disabled="pagination.first"
          class="btn btn-sm"
        >
          이전
        </button>
        <span class="page-info">
          {{ pagination.number + 1 }} / {{ pagination.totalPages }}
        </span>
        <button 
          @click="goToPage(pagination.number + 1)" 
          :disabled="pagination.last"
          class="btn btn-sm"
        >
          다음
        </button>
        <button 
          @click="goToPage(pagination.totalPages - 1)" 
          :disabled="pagination.last"
          class="btn btn-sm"
        >
          마지막
        </button>
      </div>
    </div>

    <!-- 쿠폰 생성/수정 모달 -->
    <div v-if="showCreateModal || showEditModal" class="modal-overlay" @click="closeModals">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ showCreateModal ? '쿠폰 생성' : '쿠폰 수정' }}</h3>
          <button class="modal-close" @click="closeModals">×</button>
        </div>
        
        <form @submit.prevent="showCreateModal ? createCoupon() : updateCoupon()">
          <div class="form-row">
            <div class="form-group">
              <label>쿠폰 코드 *</label>
              <input 
                type="text" 
                v-model="couponForm.code" 
                required
                :disabled="showEditModal"
                placeholder="WELCOME2024"
              />
            </div>
            <div class="form-group">
              <label>쿠폰명 *</label>
              <input 
                type="text" 
                v-model="couponForm.name" 
                required
                placeholder="신규 회원 환영 쿠폰"
              />
            </div>
          </div>

          <div class="form-group">
            <label>설명</label>
            <textarea 
              v-model="couponForm.description" 
              placeholder="쿠폰에 대한 설명을 입력하세요"
              rows="3"
            ></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>할인 타입 *</label>
              <select v-model="couponForm.discountType" required>
                <option value="PERCENT">퍼센트 할인</option>
                <option value="FIXED">고정 금액 할인</option>
              </select>
            </div>
            <div class="form-group">
              <label>할인 값 *</label>
              <input 
                type="number" 
                v-model.number="couponForm.discountValue" 
                required
                min="1"
                :max="couponForm.discountType === 'PERCENT' ? 100 : 1000000"
                :placeholder="couponForm.discountType === 'PERCENT' ? '10' : '10000'"
              />
              <span class="input-suffix">
                {{ couponForm.discountType === 'PERCENT' ? '%' : '원' }}
              </span>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>최소 주문 금액</label>
              <input 
                type="number" 
                v-model.number="couponForm.minOrderAmount" 
                min="0"
                placeholder="0"
              />
              <span class="input-suffix">원</span>
            </div>
            <div class="form-group">
              <label>최대 할인 금액</label>
              <input 
                type="number" 
                v-model.number="couponForm.maxDiscountAmount" 
                min="0"
                placeholder="제한 없음"
              />
              <span class="input-suffix">원</span>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>유효 시작일 *</label>
              <input 
                type="datetime-local" 
                v-model="couponForm.validFrom" 
                required
              />
            </div>
            <div class="form-group">
              <label>유효 종료일 *</label>
              <input 
                type="datetime-local" 
                v-model="couponForm.validUntil" 
                required
              />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>사용 제한 횟수</label>
              <input 
                type="number" 
                v-model.number="couponForm.usageLimit" 
                min="1"
                placeholder="제한 없음"
              />
            </div>
            <div class="form-group">
              <label>상태</label>
              <select v-model="couponForm.status">
                <option value="ACTIVE">활성</option>
                <option value="INACTIVE">비활성</option>
              </select>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="closeModals">
              취소
            </button>
            <button type="submit" class="btn btn-primary">
              {{ showCreateModal ? '생성' : '수정' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 로딩 스피너 -->
    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import api from '../../api/http'

export default {
  name: 'CouponManagement',
  setup() {
    // 반응형 데이터
    const coupons = ref([])
    const stats = ref({})
    const loading = ref(false)
    const showCreateModal = ref(false)
    const showEditModal = ref(false)
    const editingCoupon = ref(null)
    
    // 필터 상태
    const filters = reactive({
      status: '',
      discountType: '',
      code: '',
      name: ''
    })

    // 페이지네이션 상태
    const pagination = reactive({
      number: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
      first: true,
      last: true
    })

    // 쿠폰 폼 데이터
    const couponForm = reactive({
      code: '',
      name: '',
      description: '',
      discountType: 'PERCENT',
      discountValue: null,
      minOrderAmount: null,
      maxDiscountAmount: null,
      validFrom: '',
      validUntil: '',
      usageLimit: null,
      status: 'ACTIVE'
    })

    // 검색 디바운스
    let searchTimeout = null
    const debounceSearch = () => {
      clearTimeout(searchTimeout)
      searchTimeout = setTimeout(() => {
        pagination.number = 0
        loadCoupons()
      }, 500)
    }

    // 쿠폰 목록 로드
    const loadCoupons = async () => {
      loading.value = true
      try {
        const params = {
          page: pagination.number,
          size: pagination.size,
          ...Object.fromEntries(
            Object.entries(filters).filter(([_, value]) => value !== '')
          )
        }

  const response = await api.get('/admin/coupons', { params })
        
        coupons.value = response.data.content
        Object.assign(pagination, {
          number: response.data.number,
          size: response.data.size,
          totalElements: response.data.totalElements,
          totalPages: response.data.totalPages,
          first: response.data.first,
          last: response.data.last
        })

      } catch (error) {
        console.error('쿠폰 목록 로드 실패:', error)
        alert('쿠폰 목록을 불러오는데 실패했습니다.')
      } finally {
        loading.value = false
      }
    }

    // 쿠폰 통계 로드
    const loadStats = async () => {
      try {
  const response = await api.get('/admin/coupons/stats')
        stats.value = response.data
      } catch (error) {
        console.error('쿠폰 통계 로드 실패:', error)
      }
    }

    // 쿠폰 생성
    const createCoupon = async () => {
      try {
        const formData = { ...couponForm }
        
        // null 값들 제거
        Object.keys(formData).forEach(key => {
          if (formData[key] === null || formData[key] === '') {
            delete formData[key]
          }
        })

  await api.post('/admin/coupons', formData)
        
        alert('쿠폰이 성공적으로 생성되었습니다.')
        closeModals()
        loadCoupons()
        loadStats()
      } catch (error) {
        console.error('쿠폰 생성 실패:', error)
        alert(error.response?.data?.message || '쿠폰 생성에 실패했습니다.')
      }
    }

    // 쿠폰 수정
    const updateCoupon = async () => {
      try {
        const formData = { ...couponForm }
        
        // 변경되지 않은 필드 제거
        Object.keys(formData).forEach(key => {
          if (formData[key] === editingCoupon.value[key] || 
              formData[key] === null || 
              formData[key] === '') {
            delete formData[key]
          }
        })

  await api.put(`/admin/coupons/${editingCoupon.value.id}`, formData)
        
        alert('쿠폰이 성공적으로 수정되었습니다.')
        closeModals()
        loadCoupons()
        loadStats()
      } catch (error) {
        console.error('쿠폰 수정 실패:', error)
        alert(error.response?.data?.message || '쿠폰 수정에 실패했습니다.')
      }
    }

    // 쿠폰 상태 토글
    const toggleCouponStatus = async (coupon) => {
      const newStatus = coupon.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
      const action = newStatus === 'ACTIVE' ? '활성화' : '비활성화'
      
      if (!confirm(`정말로 이 쿠폰을 ${action}하시겠습니까?`)) return

      try {
  await api.put(`/admin/coupons/${coupon.id}/status`, {
          status: newStatus
        })
        
        alert(`쿠폰이 성공적으로 ${action}되었습니다.`)
        loadCoupons()
        loadStats()
      } catch (error) {
        console.error('쿠폰 상태 변경 실패:', error)
        alert(`쿠폰 ${action}에 실패했습니다.`)
      }
    }

    // 쿠폰 삭제
    const deleteCoupon = async (coupon) => {
      if (coupon.usedCount > 0) {
        alert('이미 사용된 쿠폰은 삭제할 수 없습니다.')
        return
      }

      if (!confirm('정말로 이 쿠폰을 삭제하시겠습니까?')) return

      try {
  await api.delete(`/admin/coupons/${coupon.id}`)
        
        alert('쿠폰이 성공적으로 삭제되었습니다.')
        loadCoupons()
        loadStats()
      } catch (error) {
        console.error('쿠폰 삭제 실패:', error)
        alert('쿠폰 삭제에 실패했습니다.')
      }
    }

    // 쿠폰 수정 모달 열기
    const editCoupon = (coupon) => {
      editingCoupon.value = coupon
      
      // 폼에 기존 데이터 설정
      Object.assign(couponForm, {
        code: coupon.code,
        name: coupon.name,
        description: coupon.description || '',
        discountType: coupon.discountType,
        discountValue: coupon.discountValue,
        minOrderAmount: coupon.minOrderAmount,
        maxDiscountAmount: coupon.maxDiscountAmount,
        validFrom: formatDateTimeLocal(coupon.validFrom),
        validUntil: formatDateTimeLocal(coupon.validUntil),
        usageLimit: coupon.usageLimit,
        status: coupon.status
      })
      
      showEditModal.value = true
    }

    // 모달 닫기
    const closeModals = () => {
      showCreateModal.value = false
      showEditModal.value = false
      editingCoupon.value = null
      
      // 폼 초기화
      Object.assign(couponForm, {
        code: '',
        name: '',
        description: '',
        discountType: 'PERCENT',
        discountValue: null,
        minOrderAmount: null,
        maxDiscountAmount: null,
        validFrom: '',
        validUntil: '',
        usageLimit: null,
        status: 'ACTIVE'
      })
    }

    // 페이지 이동
    const goToPage = (page) => {
      if (page >= 0 && page < pagination.totalPages) {
        pagination.number = page
        loadCoupons()
      }
    }

    // 필터 초기화
    const resetFilters = () => {
      Object.assign(filters, {
        status: '',
        discountType: '',
        code: '',
        name: ''
      })
      pagination.number = 0
      loadCoupons()
    }

    // 유틸리티 함수들
    const formatDate = (dateString) => {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleDateString('ko-KR') + ' ' + 
             date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
    }

    const formatDateTimeLocal = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toISOString().slice(0, 16)
    }

    const getStatusText = (status) => {
      const statusMap = {
        'ACTIVE': '활성',
        'INACTIVE': '비활성'
      }
      return statusMap[status] || status
    }

    // 컴포넌트 마운트 시 데이터 로드
    onMounted(() => {
      loadCoupons()
      loadStats()
    })

    return {
      // 반응형 데이터
      coupons,
      stats,
      loading,
      showCreateModal,
      showEditModal,
      filters,
      pagination,
      couponForm,
      
      // 함수들
      loadCoupons,
      createCoupon,
      updateCoupon,
      editCoupon,
      deleteCoupon,
      toggleCouponStatus,
      closeModals,
      goToPage,
      resetFilters,
      debounceSearch,
      formatDate,
      getStatusText
    }
  }
}
</script>

<style scoped>
.coupon-management {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.management-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e9ecef;
}

.management-header h2 {
  color: #2c3e50;
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 통계 카드 스타일 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 15px;
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.stat-icon.total { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.active { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-icon.inactive { background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%); }
.stat-icon.expired { background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%); }

.stat-content h3 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 5px 0;
  color: #2c3e50;
}

.stat-content p {
  margin: 0;
  color: #7f8c8d;
  font-size: 14px;
}

/* 필터 섹션 */
.filter-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  align-items: end;
}

.filter-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #555;
}

.filter-group input,
.filter-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

/* 테이블 스타일 */
.table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 20px;
}

.coupon-table {
  width: 100%;
  border-collapse: collapse;
}

.coupon-table th {
  background: #f8f9fa;
  padding: 15px 10px;
  text-align: left;
  font-weight: 600;
  color: #2c3e50;
  border-bottom: 1px solid #dee2e6;
  font-size: 14px;
}

.coupon-table td {
  padding: 12px 10px;
  border-bottom: 1px solid #f1f3f4;
  vertical-align: middle;
  font-size: 13px;
}

.coupon-table tr:hover {
  background-color: #f8f9fa;
}

.coupon-code {
  background: #e3f2fd;
  color: #1565c0;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-weight: 600;
}

.discount-display {
  background: #e8f5e8;
  color: #2e7d32;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 600;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
}

.status-badge.active {
  background-color: #d4edda;
  color: #155724;
}

.status-badge.inactive {
  background-color: #f8d7da;
  color: #721c24;
}

.validity-info {
  font-size: 12px;
  line-height: 1.4;
}

.expired-badge {
  background: #dc3545;
  color: white;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: 500;
}

.usage-info {
  text-align: center;
  font-size: 12px;
}

.usage-info .remaining {
  color: #6c757d;
  font-size: 11px;
}

.action-buttons {
  display: flex;
  gap: 5px;
  justify-content: center;
}

.action-buttons .btn {
  padding: 6px 10px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-buttons .btn:hover:not(:disabled) {
  background: #f8f9fa;
}

.action-buttons .btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-buttons .btn-warning {
  color: #856404;
  border-color: #ffc107;
}

.action-buttons .btn-warning:hover:not(:disabled) {
  background: #ffc107;
  color: white;
}

.action-buttons .btn-danger {
  color: #721c24;
  border-color: #dc3545;
}

.action-buttons .btn-danger:hover:not(:disabled) {
  background: #dc3545;
  color: white;
}

/* 페이지네이션 */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.pagination {
  display: flex;
  gap: 10px;
  align-items: center;
}

.pagination .btn {
  padding: 8px 12px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pagination .btn:hover:not(:disabled) {
  background: #f8f9fa;
}

.pagination .btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-weight: 500;
  color: #495057;
  margin: 0 10px;
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
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 0;
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
  border-radius: 12px 12px 0 0;
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 10px;
}

.modal-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #6c757d;
  padding: 5px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.modal-close:hover {
  background: #e9ecef;
  color: #495057;
}

.modal-content form {
  padding: 30px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  position: relative;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #495057;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s ease;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #80bdff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.input-suffix {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #6c757d;
  font-size: 14px;
  pointer-events: none;
}

.form-group:has(textarea) .input-suffix {
  top: auto;
  bottom: 12px;
  transform: none;
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
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

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
}

/* 로딩 스피너 */
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
@media (max-width: 768px) {
  .filter-row {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .coupon-table {
    font-size: 12px;
  }
  
  .coupon-table th,
  .coupon-table td {
    padding: 8px 6px;
  }
  
  .modal-content {
    width: 95%;
  }
  
  .modal-content form {
    padding: 20px;
  }
}
</style>