<template>
  <div class="business-apply">
    <div class="apply-container">
      <div class="apply-header">
        <h2>사업자 등록 신청</h2>
        <p class="description">사업자 정보를 입력하여 사업자 등록을 신청하세요.</p>
      </div>

      <form @submit.prevent="submitApplication" class="apply-form">
        <div class="form-group">
          <label for="businessName">사업자명 *</label>
          <input
            id="businessName"
            v-model="form.businessName"
            type="text"
            required
            placeholder="사업자명을 입력하세요"
          />
        </div>

        <div class="form-group">
          <label for="businessNumber">사업자 등록번호 *</label>
          <input
            id="businessNumber"
            v-model="form.businessNumber"
            type="text"
            required
            placeholder="000-00-00000 형식으로 입력하세요"
          />
        </div>

        <div class="form-group">
          <label for="address">주소 *</label>
          <input
            id="address"
            v-model="form.address"
            type="text"
            required
            placeholder="사업장 주소를 입력하세요"
          />
        </div>

        <div class="form-group">
          <label for="phone">연락처 *</label>
          <input
            id="phone"
            v-model="form.phone"
            type="tel"
            required
            placeholder="010-0000-0000 형식으로 입력하세요"
          />
        </div>

        <div class="form-actions">
          <button type="button" @click="goBack" class="btn btn-secondary">
            취소
          </button>
          <button type="submit" :disabled="loading" class="btn btn-primary">
            {{ loading ? '신청 중...' : '신청하기' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/http'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  businessName: '',
  businessNumber: '',
  address: '',
  phone: ''
})

const submitApplication = async () => {
  console.log('=== 사업자 신청 폼 제출 시작 ===')
  console.log('폼 데이터:', form)
  
  loading.value = true
  
  try {
  console.log('API 호출 시작: businesses/apply')
  const response = await api.post('/businesses/apply', form)
    console.log('API 응답:', response.data)
    
    alert(response.data.message || '신청이 완료되었습니다. 관리자 승인 후 활동할 수 있습니다.')
    router.push('/')
    
  } catch (error) {
    console.error('사업자 신청 오류:', error)
    console.error('오류 응답:', error.response?.data)
    
    if (error.response?.status === 401) {
      alert('인증이 필요합니다. 다시 로그인 후 시도해주세요.')
    } else if (error.response?.data?.error) {
      alert(error.response.data.error)
    } else {
      alert('신청 처리 중 오류가 발생했습니다. 다시 시도해주세요.')
    }
  } finally {
    loading.value = false
    console.log('=== 사업자 신청 폼 제출 완료 ===')
  }
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.business-apply {
  min-height: 100vh;
  background: #f8f9fa;
  padding: 20px;
}

.apply-container {
  max-width: 600px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.apply-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  text-align: center;
}

.apply-header h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
}

.description {
  margin: 0;
  opacity: 0.9;
  font-size: 16px;
}

.apply-form {
  padding: 30px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 600;
  color: #333;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s ease;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}
</style>