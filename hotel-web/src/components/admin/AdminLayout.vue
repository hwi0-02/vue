<template>
  <div class="admin-layout">
    <aside class="sidebar" :class="{ open: isSidebarOpen }">
      <div class="sidebar-header">
        <h2>관리자 패널</h2>
      </div>
      <nav class="menu">
        <router-link class="menu-item" :class="{active: $route.path === '/admin/dashboard'}" to="/admin/dashboard">대시보드</router-link>
        <router-link class="menu-item" :class="{active: $route.path === '/admin/users'}" to="/admin/users">사용자 관리</router-link>
        <router-link class="menu-item" :class="{active: $route.path === '/admin/businesses'}" to="/admin/businesses">사업자 승인</router-link>
        <router-link class="menu-item" :class="{active: $route.path === '/admin/hotels'}" to="/admin/hotels">호텔 관리</router-link>
        <router-link class="menu-item" :class="{active: $route.path === '/admin/reservations'}" to="/admin/reservations">예약 관리</router-link>
        <router-link class="menu-item" :class="{active: $route.path === '/admin/sales'}" to="/admin/sales">매출·수수료</router-link>
        <router-link class="menu-item" :class="{active: $route.path === '/admin/payments'}" to="/admin/payments">결제 관리</router-link>
        <router-link class="menu-item" :class="{active: $route.path === '/admin/reviews'}" to="/admin/reviews">리뷰 관리</router-link>
        <router-link class="menu-item" :class="{active: $route.path === '/admin/coupons'}" to="/admin/coupons">쿠폰 관리</router-link>
      </nav>
    </aside>

    <div v-if="isSidebarOpen" class="sidebar-overlay" @click="closeSidebar"></div>

    <div class="main-content">
      <header class="top-nav">
        <div class="nav-left">
          <button class="burger" aria-label="사이드바 열기/닫기" @click="toggleSidebar">
            <span></span>
            <span></span>
            <span></span>
          </button>
          <h1>{{ pageTitle }}</h1>
        </div>
        <div class="nav-right">
          <span class="admin-name">{{ adminName }}</span>
          <button class="btn btn-logout" @click="logout">로그아웃</button>
        </div>
      </header>
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
      adminName: '관리자',
      isSidebarOpen: false
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
    },
    toggleSidebar() { this.isSidebarOpen = !this.isSidebarOpen },
    closeSidebar() { this.isSidebarOpen = false }
  },
  mounted() {
    this.$watch(() => this.$route.fullPath, () => {
      this.isSidebarOpen = false
    })
  }
}
</script>

<style scoped>
.admin-layout { min-height: 100vh; display: block; --sidebar-width: 280px; }
.sidebar { position: fixed; top: 0; left: 0; bottom: 0; background: #2c3e50; color: #fff; width: var(--sidebar-width); overflow-y: auto; transition: transform 0.25s ease; }
.sidebar-header { padding: 24px; border-bottom: 1px solid #34495e; }
.sidebar-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
.menu { display: flex; flex-direction: column; padding: 8px 0 16px; }
.menu-item { padding: 12px 20px; color: #bdc3c7; text-decoration: none; transition: background 0.2s ease, color 0.2s ease, border-left-color 0.2s ease; border-left: 3px solid transparent; outline: none; }
.menu-item:hover { background: #34495e; color: #fff; }
.menu-item.active { background: #2f4153; color: #fff; border-left-color: #3498db; }
.menu-item:focus-visible { box-shadow: 0 0 0 2px rgba(52,152,219,0.4) inset; }

.main-content { margin-left: var(--sidebar-width); min-height: 100vh; background: #f5f7fa; }
.top-nav { position: sticky; top: 0; z-index: 10; display: flex; align-items: center; justify-content: space-between; background: #fff; padding: 0 24px; box-shadow: 0 2px 4px rgba(0,0,0,0.06); height: 64px; }
.nav-left h1 { margin: 0; font-size: 20px; font-weight: 600; color: #2c3e50; }
.nav-right { display: flex; align-items: center; gap: 10px; }
.btn-logout { background: #fff; color: #e74c3c; border: 1px solid #e74c3c; padding: 6px 12px; border-radius: 6px; cursor: pointer; }
.btn-logout:hover { background: #e74c3c; color: #fff; }

.content { padding: 24px; min-height: calc(100vh - 64px); overflow: auto; }

/* 햄버거 버튼 */
.burger { display: none; width: 40px; height: 40px; margin-right: 8px; background: transparent; border: none; padding: 0; cursor: pointer; align-items: center; justify-content: center; }
.burger span { display: block; width: 20px; height: 2px; background: #2c3e50; margin: 3px 0; transition: background 0.2s; }
.burger:focus-visible { outline: 2px solid #3498db; border-radius: 4px; }

/* 모바일 사이드바 & 오버레이 */
.sidebar-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); z-index: 9; }

@media (max-width: 1024px) {
  .admin-layout { --sidebar-width: 240px; }
  .main-content { margin-left: 0; }
  .sidebar { transform: translateX(-100%); z-index: 10; }
  .sidebar.open { transform: translateX(0); }
  .burger { display: inline-flex; }
}

/* Admin 화면에서는 전역 #app, body의 중앙 정렬을 비활성화 */
:global(#app) { max-width: none; margin: 0; padding: 0; }
:global(body) { display: block; place-items: initial; }
</style>