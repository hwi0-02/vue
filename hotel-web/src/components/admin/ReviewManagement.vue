<template>
  <div class="review-management">
    <div class="page-header">
      <h1>리뷰 관리</h1>
      <p>전체 리뷰를 조회하고 신고된 리뷰를 숨김 처리합니다.</p>
    </div>

    <!-- 필터 및 검색 -->
    <div class="filters">
      <div class="filter-row">
        <div class="filter-group">
          <label>
            <input 
              v-model="showReportedOnly" 
              type="checkbox"
              @change="searchReviews"
            />
            신고된 리뷰만 보기
          </label>
        </div>
        <div class="filter-group">
          <label>
            <input 
              v-model="showHiddenOnly" 
              type="checkbox"
              @change="searchReviews"
            />
            숨김 처리된 리뷰만 보기
          </label>
        </div>
        <input 
          v-model="filters.hotelName" 
          type="text" 
          placeholder="호텔명 검색"
          class="filter-input"
        />
        <input 
          v-model="filters.userName" 
          type="text" 
          placeholder="작성자명 검색"
          class="filter-input"
        />
        <button @click="searchReviews" class="search-btn">검색</button>
        <button @click="resetFilters" class="reset-btn">초기화</button>
      </div>
    </div>

    <!-- 통계 카드 -->
    <div class="stats-grid">
      <div class="stat-card">
        <h3>전체 리뷰</h3>
        <p class="stat-number">{{ totalReviews }}</p>
      </div>
      <div class="stat-card reported">
        <h3>신고된 리뷰</h3>
        <p class="stat-number">{{ reportedReviews }}</p>
      </div>
      <div class="stat-card hidden">
        <h3>숨김 처리</h3>
        <p class="stat-number">{{ hiddenReviews }}</p>
      </div>
      <div class="stat-card rating">
        <h3>평균 평점</h3>
        <p class="stat-number">{{ averageRating }}</p>
      </div>
    </div>

    <!-- 리뷰 목록 테이블 -->
    <div class="table-container">
      <table class="review-table">
        <thead>
          <tr>
            <th>리뷰번호</th>
            <th>호텔명</th>
            <th>작성자</th>
            <th>평점</th>
            <th>제목</th>
            <th>작성일시</th>
            <th>상태</th>
            <th>액션</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="review in reviews" :key="review.reviewId" 
              :class="{ 'reported-row': review.isReported, 'hidden-row': review.isHidden }">
            <td>{{ review.reviewId }}</td>
            <td>{{ review.hotelName }}</td>
            <td>
              <div>
                <div>{{ review.userName }}</div>
                <small>{{ review.userEmail }}</small>
              </div>
            </td>
            <td>
              <div class="rating">
                <span class="stars">{{ getStarRating(review.rating) }}</span>
                <small>({{ review.rating }}/5)</small>
              </div>
            </td>
            <td>
              <div class="review-title" @click="viewReviewDetail(review)">
                {{ review.title }}
                <small v-if="review.content && review.content.length > 50">
                  {{ review.content.substring(0, 50) }}...
                </small>
              </div>
            </td>
            <td>{{ formatDateTime(review.createdAt) }}</td>
            <td>
              <div class="status-badges">
                <span v-if="review.isReported" class="status-badge reported">신고됨</span>
                <span v-if="review.isHidden" class="status-badge hidden">숨김</span>
                <span v-if="!review.isReported && !review.isHidden" class="status-badge normal">정상</span>
              </div>
            </td>
            <td>
              <div class="action-buttons">
                <button 
                  @click="viewReviewDetail(review)" 
                  class="action-btn view-btn"
                >
                  상세보기
                </button>
                <button 
                  v-if="!review.isHidden"
                  @click="hideReview(review)" 
                  class="action-btn hide-btn"
                >
                  숨김
                </button>
                <button 
                  v-else
                  @click="showReview(review)" 
                  class="action-btn show-btn"
                >
                  숨김해제
                </button>
                <button 
                  v-if="!review.isReported"
                  @click="reportReview(review)" 
                  class="action-btn report-btn"
                >
                  신고처리
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 빈 데이터 메시지 -->
      <div v-if="reviews.length === 0" class="empty-message">
        <p>조건에 맞는 리뷰가 없습니다.</p>
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

    <!-- 리뷰 상세 모달 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>리뷰 상세 정보</h2>
          <button @click="closeDetailModal" class="close-btn">×</button>
        </div>
        
        <div class="modal-body" v-if="selectedReview">
          <div class="detail-grid">
            <!-- 리뷰 정보 -->
            <div class="detail-section">
              <h3>리뷰 정보</h3>
              <div class="detail-item">
                <label>리뷰번호:</label>
                <span>{{ selectedReview.reviewId }}</span>
              </div>
              <div class="detail-item">
                <label>평점:</label>
                <span class="rating-detail">
                  {{ getStarRating(selectedReview.rating) }} ({{ selectedReview.rating }}/5)
                </span>
              </div>
              <div class="detail-item">
                <label>제목:</label>
                <span>{{ selectedReview.title }}</span>
              </div>
              <div class="detail-item full-width">
                <label>내용:</label>
                <div class="review-content">{{ selectedReview.content }}</div>
              </div>
              <div class="detail-item" v-if="selectedReview.imageUrls">
                <label>첨부 이미지:</label>
                <div class="image-list">
                  <img v-for="(url, index) in getImageUrls(selectedReview.imageUrls)" 
                       :key="index" :src="url" alt="리뷰 이미지" class="review-image" />
                </div>
              </div>
              <div class="detail-item">
                <label>작성일시:</label>
                <span>{{ formatDateTime(selectedReview.createdAt) }}</span>
              </div>
              <div class="detail-item">
                <label>상태:</label>
                <div class="status-badges">
                  <span v-if="selectedReview.isReported" class="status-badge reported">신고됨</span>
                  <span v-if="selectedReview.isHidden" class="status-badge hidden">숨김</span>
                  <span v-if="!selectedReview.isReported && !selectedReview.isHidden" class="status-badge normal">정상</span>
                </div>
              </div>
            </div>

            <!-- 작성자 정보 -->
            <div class="detail-section">
              <h3>작성자 정보</h3>
              <div class="detail-item">
                <label>사용자번호:</label>
                <span>{{ selectedReview.userId }}</span>
              </div>
              <div class="detail-item">
                <label>작성자명:</label>
                <span>{{ selectedReview.userName }}</span>
              </div>
              <div class="detail-item">
                <label>이메일:</label>
                <span>{{ selectedReview.userEmail }}</span>
              </div>
            </div>

            <!-- 호텔 정보 -->
            <div class="detail-section">
              <h3>호텔 정보</h3>
              <div class="detail-item">
                <label>호텔번호:</label>
                <span>{{ selectedReview.hotelId }}</span>
              </div>
              <div class="detail-item">
                <label>호텔명:</label>
                <span>{{ selectedReview.hotelName }}</span>
              </div>
              <div class="detail-item">
                <label>예약번호:</label>
                <span>{{ selectedReview.reservationNumber }}</span>
              </div>
            </div>

            <!-- 관리자 답변 -->
            <div class="detail-section">
              <h3>관리자 답변</h3>
              <div class="detail-item full-width">
                <label>답변 내용:</label>
                <div v-if="selectedReview.adminReply" class="admin-reply">
                  {{ selectedReview.adminReply }}
                  <small v-if="selectedReview.repliedAt">
                    ({{ formatDateTime(selectedReview.repliedAt) }})
                  </small>
                </div>
                <div v-else class="no-reply">
                  관리자 답변이 없습니다.
                </div>
              </div>
            </div>
          </div>

          <!-- 액션 버튼 -->
          <div class="modal-actions">
            <button 
              v-if="!selectedReview.isHidden"
              @click="hideReview(selectedReview)" 
              class="action-btn hide-btn"
            >
              리뷰 숨김
            </button>
            <button 
              v-else
              @click="showReview(selectedReview)" 
              class="action-btn show-btn"
            >
              숨김 해제
            </button>
            <button 
              v-if="!selectedReview.isReported"
              @click="reportReview(selectedReview)" 
              class="action-btn report-btn"
            >
              신고 처리
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'

export default {
  name: 'ReviewManagement',
  setup() {
    const reviews = ref([])
    const selectedReview = ref(null)
    const showDetailModal = ref(false)
    const showReportedOnly = ref(false)
    const showHiddenOnly = ref(false)
    
    // 필터 상태
    const filters = reactive({
      hotelName: '',
      userName: ''
    })

    // 페이지네이션 상태
    const pagination = reactive({
      currentPage: 0,
      totalPages: 0,
      totalElements: 0,
      size: 20
    })

    // 통계 데이터
    const totalReviews = ref(0)
    const reportedReviews = ref(0)
    const hiddenReviews = ref(0)
    const averageRating = ref(0)

    // 리뷰 목록 조회
    const searchReviews = async () => {
      try {
        const params = new URLSearchParams({
          page: pagination.currentPage,
          size: pagination.size
        })

        if (showReportedOnly.value) params.append('reported', 'true')
        if (showHiddenOnly.value) params.append('hidden', 'true')
        if (filters.hotelName) params.append('hotelName', filters.hotelName)
        if (filters.userName) params.append('userName', filters.userName)

        const response = await fetch(`/api/admin/reviews?${params}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })

        if (!response.ok) throw new Error('리뷰 목록 조회 실패')

        const data = await response.json()
        reviews.value = data.content || []
        
        pagination.totalPages = data.totalPages || 0
        pagination.totalElements = data.totalElements || 0

        // 통계 업데이트 (실제로는 별도 API 호출)
        updateStatistics()
        
      } catch (error) {
        console.error('리뷰 목록 조회 오류:', error)
        alert('리뷰 목록을 불러오는데 실패했습니다.')
      }
    }

    // 통계 업데이트
    const updateStatistics = () => {
      totalReviews.value = reviews.value.length
      reportedReviews.value = reviews.value.filter(r => r.isReported).length
      hiddenReviews.value = reviews.value.filter(r => r.isHidden).length
      
      if (reviews.value.length > 0) {
        const avgRating = reviews.value.reduce((sum, r) => sum + r.rating, 0) / reviews.value.length
        averageRating.value = avgRating.toFixed(1)
      }
    }

    // 페이지 변경
    const changePage = (page) => {
      pagination.currentPage = page
      searchReviews()
    }

    // 필터 초기화
    const resetFilters = () => {
      showReportedOnly.value = false
      showHiddenOnly.value = false
      filters.hotelName = ''
      filters.userName = ''
      pagination.currentPage = 0
      searchReviews()
    }

    // 리뷰 상세 보기
    const viewReviewDetail = async (review) => {
      try {
        const response = await fetch(`/api/admin/reviews/${review.reviewId}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })

        if (!response.ok) throw new Error('리뷰 상세 조회 실패')

        selectedReview.value = await response.json()
        showDetailModal.value = true
        
      } catch (error) {
        console.error('리뷰 상세 조회 오류:', error)
        alert('리뷰 상세 정보를 불러오는데 실패했습니다.')
      }
    }

    // 상세 모달 닫기
    const closeDetailModal = () => {
      showDetailModal.value = false
      selectedReview.value = null
    }

    // 리뷰 숨김 처리
    const hideReview = async (review) => {
      if (!confirm('이 리뷰를 숨김 처리하시겠습니까?')) return

      try {
        const response = await fetch(`/api/admin/reviews/${review.reviewId}/hide`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })

        if (!response.ok) throw new Error('리뷰 숨김 처리 실패')

        alert('리뷰가 성공적으로 숨김 처리되었습니다.')
        searchReviews()
        
        if (selectedReview.value && selectedReview.value.reviewId === review.reviewId) {
          selectedReview.value.isHidden = true
        }
        
      } catch (error) {
        console.error('리뷰 숨김 처리 오류:', error)
        alert('리뷰 숨김 처리에 실패했습니다.')
      }
    }

    // 리뷰 숨김 해제
    const showReview = async (review) => {
      if (!confirm('이 리뷰의 숨김을 해제하시겠습니까?')) return

      try {
        const response = await fetch(`/api/admin/reviews/${review.reviewId}/show`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })

        if (!response.ok) throw new Error('리뷰 숨김 해제 실패')

        alert('리뷰 숨김이 성공적으로 해제되었습니다.')
        searchReviews()
        
        if (selectedReview.value && selectedReview.value.reviewId === review.reviewId) {
          selectedReview.value.isHidden = false
        }
        
      } catch (error) {
        console.error('리뷰 숨김 해제 오류:', error)
        alert('리뷰 숨김 해제에 실패했습니다.')
      }
    }

    // 리뷰 신고 처리
    const reportReview = async (review) => {
      if (!confirm('이 리뷰를 신고 처리하시겠습니까?')) return

      try {
        const response = await fetch(`/api/admin/reviews/${review.reviewId}/report`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })

        if (!response.ok) throw new Error('리뷰 신고 처리 실패')

        alert('리뷰가 성공적으로 신고 처리되었습니다.')
        searchReviews()
        
        if (selectedReview.value && selectedReview.value.reviewId === review.reviewId) {
          selectedReview.value.isReported = true
        }
        
      } catch (error) {
        console.error('리뷰 신고 처리 오류:', error)
        alert('리뷰 신고 처리에 실패했습니다.')
      }
    }

    // 별점 표시
    const getStarRating = (rating) => {
      return '★'.repeat(rating) + '☆'.repeat(5 - rating)
    }

    // 이미지 URL 파싱
    const getImageUrls = (imageUrlString) => {
      if (!imageUrlString) return []
      try {
        return JSON.parse(imageUrlString)
      } catch {
        return imageUrlString.split(',').map(url => url.trim())
      }
    }

    // 날짜시간 포맷팅
    const formatDateTime = (dateString) => {
      if (!dateString) return '-'
      return new Date(dateString).toLocaleString('ko-KR')
    }

    // 컴포넌트 마운트 시 데이터 로드
    onMounted(() => {
      searchReviews()
    })

    return {
      reviews,
      selectedReview,
      showDetailModal,
      showReportedOnly,
      showHiddenOnly,
      filters,
      pagination,
      totalReviews,
      reportedReviews,
      hiddenReviews,
      averageRating,
      searchReviews,
      changePage,
      resetFilters,
      viewReviewDetail,
      closeDetailModal,
      hideReview,
      showReview,
      reportReview,
      getStarRating,
      getImageUrls,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.review-management {
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

.filter-group {
  display: flex;
  align-items: center;
}

.filter-group label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  cursor: pointer;
}

.filter-input {
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

.stat-card.reported .stat-number { color: #dc3545; }
.stat-card.hidden .stat-number { color: #6c757d; }
.stat-card.rating .stat-number { color: #ffc107; }

/* 테이블 */
.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow-x: auto;
  margin-bottom: 20px;
}

.review-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.review-table th {
  background-color: #f8f9fa;
  padding: 12px 8px;
  text-align: left;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #dee2e6;
  white-space: nowrap;
}

.review-table td {
  padding: 12px 8px;
  border-bottom: 1px solid #dee2e6;
  vertical-align: top;
}

.review-table tr:hover {
  background-color: #f8f9fa;
}

.reported-row {
  background-color: #fff3cd !important;
}

.hidden-row {
  background-color: #f8d7da !important;
}

.rating {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stars {
  color: #ffc107;
  font-size: 16px;
}

.review-title {
  cursor: pointer;
  color: #007bff;
  max-width: 200px;
}

.review-title:hover {
  text-decoration: underline;
}

.review-title small {
  display: block;
  color: #666;
  font-size: 12px;
  margin-top: 4px;
}

/* 상태 배지 */
.status-badges {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.status-badge {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 500;
  text-align: center;
  white-space: nowrap;
}

.status-badge.normal { background-color: #d4edda; color: #155724; }
.status-badge.reported { background-color: #f8d7da; color: #721c24; }
.status-badge.hidden { background-color: #e2e3e5; color: #383d41; }

/* 액션 버튼 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.action-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 11px;
  white-space: nowrap;
}

.view-btn { background-color: #007bff; color: white; }
.hide-btn { background-color: #6c757d; color: white; }
.show-btn { background-color: #28a745; color: white; }
.report-btn { background-color: #dc3545; color: white; }

.action-btn:hover {
  opacity: 0.8;
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
  max-width: 900px;
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
  margin-bottom: 20px;
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
  align-items: flex-start;
  margin-bottom: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f1f1f1;
}

.detail-item.full-width {
  flex-direction: column;
  align-items: flex-start;
}

.detail-item label {
  font-weight: 500;
  color: #666;
  min-width: 100px;
  margin-bottom: 5px;
}

.detail-item span {
  text-align: right;
  word-break: break-all;
}

.review-content {
  margin-top: 8px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.rating-detail {
  color: #ffc107;
  font-size: 16px;
}

.image-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.review-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.admin-reply {
  background-color: #f8f9fa;
  padding: 10px;
  border-radius: 4px;
  margin-top: 8px;
  line-height: 1.5;
}

.no-reply {
  color: #666;
  font-style: italic;
  margin-top: 8px;
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
  padding-top: 20px;
  border-top: 1px solid #dee2e6;
}

@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-input {
    min-width: auto;
    width: 100%;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .review-table {
    font-size: 12px;
  }

  .review-table th,
  .review-table td {
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

  .modal-actions {
    flex-direction: column;
  }
}
</style>