<template>
  <div class="payment-management">
    <div class="page-header">
      <h1>결제 관리</h1>
      <p class="page-description">결제 내역을 모니터링하고 환불을 처리합니다.</p>
    </div>
    <div class="page-toolbar">
      <div class="actions">
        <button class="btn" @click="filterDrawer = true">필터</button>
        <button class="btn btn-primary" :disabled="loading" @click="loadPayments(0)">새로고침</button>
        <div class="dropdown">
          <button class="btn" @click="showColumnMenu = !showColumnMenu">컬럼</button>
          <div class="dropdown-menu" v-if="showColumnMenu">
            <div class="dropdown-item" v-for="col in toggleableColumns" :key="col.key">
              <label>
                <input type="checkbox" :value="col.key" v-model="visibleKeys" /> {{ col.label }}
              </label>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 필터 패널 -->
    <div class="side-panel" v-if="filterDrawer">
      <div class="side-panel-header">
        <strong>결제 필터</strong>
        <button class="btn" @click="filterDrawer = false">닫기</button>
      </div>
      <div class="side-panel-body">
        <div class="field">
          <label>호텔명</label>
          <input v-model="filters.hotelName" placeholder="호텔명" @keyup.enter="applyFilters" />
        </div>
        <div class="field">
          <label>고객명</label>
          <input v-model="filters.userName" placeholder="고객명" @keyup.enter="applyFilters" />
        </div>
        <div class="field">
          <label>상태</label>
          <select v-model="filters.status">
            <option value="">전체</option>
            <option v-for="s in statusOptions" :key="s" :value="s">{{ s }}</option>
          </select>
        </div>
      </div>
      <div class="side-panel-footer">
        <button class="btn btn-primary" @click="applyFilters">적용</button>
        <button class="btn" @click="resetFilters">초기화</button>
      </div>
    </div>

    <!-- 결과 테이블 -->
    <div class="table-section" v-if="payments.items.length">
      <table class="payments-table">
        <thead>
          <tr>
            <th v-if="isVisible('id')" @click="sortBy('id')">ID <span class="sort" :class="sortIcon('id')"></span></th>
            <th v-if="isVisible('reservationNumber')">예약번호</th>
            <th v-if="isVisible('hotelName')">호텔명</th>
            <th v-if="isVisible('userName')">고객명</th>
            <th v-if="isVisible('amount')" class="text-right" @click="sortBy('amount')">금액 <span class="sort" :class="sortIcon('amount')"></span></th>
            <th v-if="isVisible('method')">결제수단</th>
            <th v-if="isVisible('status')" @click="sortBy('status')">상태 <span class="sort" :class="sortIcon('status')"></span></th>
            <th v-if="isVisible('paidAt')" @click="sortBy('paidAt')">결제일시 <span class="sort" :class="sortIcon('paidAt')"></span></th>
            <th v-if="isVisible('cancelledAt')">취소일시</th>
            <th>작업</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in payments.items" :key="row.id">
            <td v-if="isVisible('id')">{{ row.id }}</td>
            <td v-if="isVisible('reservationNumber')"><span class="mono">{{ row.reservationNumber }}</span></td>
            <td v-if="isVisible('hotelName')">{{ row.hotelName }}</td>
            <td v-if="isVisible('userName')">{{ row.userName }}</td>
            <td v-if="isVisible('amount')" class="text-right">{{ formatCurrency(row.amount) }}</td>
            <td v-if="isVisible('method')">{{ row.method }}</td>
            <td v-if="isVisible('status')"><span class="badge" :class="statusBadgeClass(row.status)">{{ row.status }}</span></td>
            <td v-if="isVisible('paidAt')">{{ formatDateTime(row.paidAt) }}</td>
            <td v-if="isVisible('cancelledAt')">{{ formatDateTime(row.cancelledAt) }}</td>
            <td>
              <button 
                class="btn btn-danger btn-sm" 
                :disabled="row.status !== 'PAID' || refundingId === row.id"
                @click="confirmRefund(row)"
              >
                {{ refundingId === row.id ? '처리중...' : '환불' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <div class="left">
          <label>페이지 크기
            <select v-model.number="payments.size" @change="onSizeChange(payments.size)">
              <option :value="10">10</option>
              <option :value="20">20</option>
              <option :value="50">50</option>
            </select>
          </label>
        </div>
        <div class="right">
          <button class="btn" :disabled="payments.page===0" @click="onPageChange(payments.page)">이전</button>
          <span class="page-info">{{ payments.page + 1 }} / {{ payments.totalPages }} · 총 {{ payments.totalElements }}건</span>
          <button class="btn" :disabled="payments.page >= payments.totalPages-1" @click="onPageChange(payments.page + 2)">다음</button>
        </div>
      </div>
    </div>

    <!-- 빈 상태 -->
    <div v-else class="empty">
      <h3>표시할 결제 내역이 없습니다</h3>
      <p>검색 조건을 변경해 보세요.</p>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'

export default {
  name: 'PaymentManagement',
  setup() {
    const loading = ref(false)
    const filterDrawer = ref(false)
    const refundingId = ref(null)
    const statusOptions = ['PENDING', 'PAID', 'CANCELLED', 'FAILED', 'REFUNDED', 'PARTIAL_REFUNDED']
    const filters = reactive({ hotelName: '', userName: '', status: '' })
    const payments = reactive({ items: [], page: 0, totalPages: 0, totalElements: 0, size: 20 })
    const showColumnMenu = ref(false)

    // 컬럼 정의 및 가시성 컨트롤
    const allColumns = [
      { key: 'id', label: 'ID' },
      { key: 'reservationNumber', label: '예약번호' },
      { key: 'hotelName', label: '호텔명' },
      { key: 'userName', label: '고객명' },
      { key: 'amount', label: '금액' },
      { key: 'method', label: '결제수단' },
      { key: 'status', label: '상태' },
      { key: 'paidAt', label: '결제일시' },
      { key: 'cancelledAt', label: '취소일시' }
    ]
    const visibleKeys = ref(allColumns.map(c => c.key))
    const toggleableColumns = computed(() => allColumns)
    const isVisible = (key) => visibleKeys.value.includes(key)

    // 정렬 상태
    const sort = ref({ prop: 'paidAt', order: 'descending' })

    const loadPayments = async (page = 0) => {
      loading.value = true
      try {
        const params = new URLSearchParams()
        if (filters.hotelName) params.append('hotelName', filters.hotelName)
        if (filters.userName) params.append('userName', filters.userName)
        if (filters.status) params.append('status', filters.status)
        params.append('page', page)
        params.append('size', payments.size)
        // 정렬 파라미터
        const sortParam = toApiSort(sort.value)
        if (sortParam) params.append('sort', sortParam)

        const res = await fetch(`/api/admin/payments?${params}`, {
          headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        })
        if (!res.ok) throw new Error('결제 목록 조회 실패')
        const data = await res.json()
        payments.items = data.content || []
        payments.page = data.number || 0
        payments.totalPages = data.totalPages || 0
        payments.totalElements = data.totalElements || 0
      } catch (err) {
        alert('결제 목록을 불러오지 못했습니다.')
      } finally {
        loading.value = false
      }
    }

    const sortBy = (prop) => {
      if (sort.value.prop === prop) {
        sort.value.order = sort.value.order === 'ascending' ? 'descending' : 'ascending'
      } else {
        sort.value = { prop, order: 'ascending' }
      }
      loadPayments(0)
    }

    const sortIcon = (prop) => {
      if (sort.value.prop !== prop) return ''
      return sort.value.order === 'ascending' ? 'asc' : 'desc'
    }

    const toApiSort = ({ prop, order }) => {
      if (!prop || !order) return null
      const dir = order === 'ascending' ? 'asc' : 'desc'
      // 매핑 가능한 필드만 허용
      const allowed = new Set(['id', 'paidAt', 'amount', 'status'])
      if (!allowed.has(prop)) return null
      return `${prop},${dir}`
    }

    const onPageChange = (p) => {
      loadPayments(p - 1)
    }

    const onSizeChange = (s) => {
      payments.size = s
      loadPayments(0)
    }

    const confirmRefund = async (p) => {
      const ok = window.confirm(`결제(ID: ${p.id})를 환불 처리하시겠습니까?`)
      if (!ok) return
      refundingId.value = p.id
      try {
        const res = await fetch(`/api/admin/payments/${p.id}/refund`, {
          method: 'POST',
          headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        })
        if (!res.ok) {
          if (res.status === 400) {
            alert('환불은 결제 완료 상태(PAID)만 가능합니다.')
            return
          }
          throw new Error('환불 처리 실패')
        }
        await loadPayments(payments.page)
        alert('환불이 처리되었습니다.')
      } catch (err) {
        alert('환불 처리 중 오류가 발생했습니다.')
      } finally {
        refundingId.value = null
      }
    }

    const formatCurrency = (v) => {
      if (v == null) return '₩0'
      const num = typeof v === 'number' ? v : Number(v)
      return new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(num)
    }

    const formatDateTime = (dt) => {
      if (!dt) return '-'
      const d = new Date(dt)
      if (isNaN(d.getTime())) return String(dt)
      const yyyy = d.getFullYear()
      const mm = String(d.getMonth()+1).padStart(2,'0')
      const dd = String(d.getDate()).padStart(2,'0')
      const hh = String(d.getHours()).padStart(2,'0')
      const mi = String(d.getMinutes()).padStart(2,'0')
      return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
    }

    onMounted(() => {
      loadPayments(0)
    })

    const resetFilters = () => {
      filters.hotelName = ''
      filters.userName = ''
      filters.status = ''
      loadPayments(0)
    }

    const applyFilters = () => {
      filterDrawer.value = false
      loadPayments(0)
    }

    const statusBadgeClass = (s) => {
      const m = {
        PAID: 'badge-success',
        PENDING: 'badge-warning',
        CANCELLED: 'badge-danger',
        FAILED: 'badge-danger',
        REFUNDED: 'badge-info',
        PARTIAL_REFUNDED: 'badge-info'
      }
      return m[s] || 'badge-default'
    }

    return { 
      loading, filterDrawer, refundingId, showColumnMenu,
      statusOptions, filters, payments,
      visibleKeys, toggleableColumns, isVisible,
      sortBy, sortIcon, onPageChange, onSizeChange,
      loadPayments, confirmRefund, applyFilters, resetFilters,
      formatCurrency, formatDateTime, statusBadgeClass
    }
  }
}
</script>

<style scoped>
.payment-management { padding: 20px; }
.page-toolbar { display: flex; justify-content: flex-end; align-items: center; gap: 8px; margin-bottom: 12px; }
.actions { display: flex; gap: 8px; align-items: center; }

.btn { padding: 8px 12px; border: 1px solid #ddd; background: #fff; border-radius: 6px; cursor: pointer; }
.btn-sm { padding: 4px 8px; font-size: 12px; }
.btn-primary { background: #2c7be5; color: #fff; border-color: #2c7be5; }
.btn-danger { background: #e55353; color: #fff; border-color: #e55353; }
.text-right { text-align: right; }

.table-section { background: #fff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.06); overflow: hidden; }
.payments-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.payments-table th, .payments-table td { padding: 12px 10px; border-bottom: 1px solid #f1f3f5; }
.payments-table th { background: #f8f9fa; color: #333; text-align: left; white-space: nowrap; user-select: none; }
.payments-table th .sort { cursor: pointer; }
.payments-table tr:hover { background: #fafafa; }
.sort.asc::after { content: '▲'; font-size: 10px; margin-left: 4px; }
.sort.desc::after { content: '▼'; font-size: 10px; margin-left: 4px; }

.amount { text-align: right; font-variant-numeric: tabular-nums; }
.mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace; }
.pagination { display: flex; justify-content: space-between; align-items: center; padding: 12px; gap: 8px; }
.pagination .right { display: flex; align-items: center; gap: 8px; }
.pagination .left select { margin-left: 6px; }
.column-selector { display: grid; grid-template-columns: 1fr; gap: 6px; }

.empty { text-align: center; padding: 40px 0; color: #666; }
.empty .empty-icon { font-size: 36px; margin-bottom: 8px; }

/* 드롭다운 */
.dropdown { position: relative; }
.dropdown-menu { position: absolute; right: 0; top: calc(100% + 6px); background: #fff; border: 1px solid #e9ecef; border-radius: 8px; padding: 10px; min-width: 200px; box-shadow: 0 10px 20px rgba(0,0,0,0.08); z-index: 10; }
.dropdown-item { padding: 4px 2px; }

/* 사이드 패널 */
.side-panel { position: fixed; right: 0; top: 0; bottom: 0; width: 320px; background: #fff; box-shadow: -4px 0 20px rgba(0,0,0,0.1); z-index: 1000; display: flex; flex-direction: column; }
.side-panel-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid #eee; }
.side-panel-body { padding: 12px 16px; display: grid; gap: 12px; }
.side-panel-body .field label { font-size: 12px; color: #555; margin-bottom: 4px; }
.side-panel-body .field input, .side-panel-body .field select { padding: 8px 10px; border: 1px solid #ddd; border-radius: 6px; }
.side-panel-footer { padding: 12px 16px; border-top: 1px solid #eee; display: flex; gap: 8px; justify-content: flex-end; }

/* 상태 배지 */
.badge { display: inline-block; padding: 4px 8px; border-radius: 12px; font-size: 12px; }
.badge-success { background: #d4edda; color: #155724; }
.badge-warning { background: #fff3cd; color: #856404; }
.badge-danger { background: #f8d7da; color: #721c24; }
.badge-info { background: #d1ecf1; color: #0c5460; }
.badge-default { background: #e2e3e5; color: #383d41; }
</style>