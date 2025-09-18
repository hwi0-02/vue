<template>
  <div class="admin-layout">
    <!-- 사이드바 -->
    <nav class="sidebar">
      <div class="sidebar-header">
        <h2>관리자 패널</h2>
      </div>
      
      <ul class="sidebar-menu">
        <li>
          <router-link to="/admin/dashboard" class="menu-item">
            <i class="icon">📊</i>
            대시보드
          </router-link>
        </li>
        <li>
          <router-link to="/admin/users" class="menu-item">
            <i class="icon">👥</i>
            사용자 관리
          </router-link>
        </li>
        <li>
          <router-link to="/admin/businesses" class="menu-item">
            <i class="icon">🏢</i>
            사업자 승인
          </router-link>
        </li>
        <li>
          <router-link to="/admin/hotels" class="menu-item">
            <i class="icon">🏨</i>
            호텔 관리
          </router-link>
        </li>
        <li>
          <router-link to="/admin/reservations" class="menu-item">
            <i class="icon">📋</i>
            예약 관리
          </router-link>
        </li>
        <li>
          <router-link to="/admin/sales" class="menu-item">
            <i class="icon">💰</i>
            매출·수수료
          </router-link>
        </li>
        <li>
          <router-link to="/admin/payments" class="menu-item">
            <i class="icon">💳</i>
            결제 관리
          </router-link>
        </li>
        <li>
          <router-link to="/admin/reviews" class="menu-item">
            <i class="icon">⭐</i>
            리뷰 관리
          </router-link>
        </li>
        <li>
          <router-link to="/admin/coupons" class="menu-item">
            <i class="icon">🎫</i>
            쿠폰 관리
          </router-link>
        </li>
      </ul>
    </nav>

    <!-- 메인 콘텐츠 영역 -->
    <div class="main-content">
      <!-- 상단 네비게이션 -->
      <header class="top-nav">
        <div class="nav-left">
          <h1>{{ pageTitle }}</h1>
        </div>
        <div class="nav-right">
          <span class="admin-name">{{ adminName }}</span>
          <button @click="logout" class="logout-btn">로그아웃</button>
        </div>
      </header>

      <!-- 라우터 뷰 (메인 콘텐츠) -->
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminLayout',
  data() {
    return {
      adminName: '관리자'
    }
  },
  computed: {
    pageTitle() {
      const routeMap = {
        '/admin/dashboard': '대시보드',
        '/admin/users': '사용자 관리',
        '/admin/businesses': '사업자 승인',
        '/admin/hotels': '호텔 관리',
        '/admin/reservations': '예약 관리',
        '/admin/payments': '결제 관리',
        '/admin/reviews': '리뷰 관리',
        '/admin/coupons': '쿠폰 관리'
      }
      return routeMap[this.$route.path] || '관리자 패널'
    }
  },
  methods: {
    logout() {
      if (confirm('로그아웃 하시겠습니까?')) {
        // TODO: 로그아웃 로직 구현
        this.$router.push('/')
      }
    }
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background-color: #f5f5f5;
}

/* 사이드바 */
.sidebar {
  width: 250px;
  background-color: #2c3e50;
  color: white;
  overflow-y: auto;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #34495e;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.sidebar-menu {
  list-style: none;
  padding: 0;
  margin: 0;
}

.sidebar-menu li {
  margin: 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  color: #bdc3c7;
  text-decoration: none;
  transition: all 0.3s ease;
}

.menu-item:hover {
  background-color: #34495e;
  color: white;
}

.menu-item.router-link-active {
  background-color: #3498db;
  color: white;
}

.menu-item .icon {
  margin-right: 10px;
  font-size: 16px;
}

/* 메인 콘텐츠 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 상단 네비게이션 */
.top-nav {
  background-color: white;
  padding: 0 30px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  border-bottom: 1px solid #e9ecef;
}

.nav-left h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.admin-name {
  font-weight: 500;
  color: #2c3e50;
}

.logout-btn {
  padding: 8px 16px;
  background-color: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s ease;
}

.logout-btn:hover {
  background-color: #c0392b;
}

/* 콘텐츠 영역 */
.content {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
  background-color: #f8f9fa;
}
</style>