import { createRouter, createWebHistory } from "vue-router";
// Auth components
import Login from "@/components/auth/Login.vue";
import Register from "@/components/auth/Register.vue";
import ForgotPassword from "@/components/auth/ForgotPassword.vue";
import LoginVerify from "@/components/auth/LoginVerify.vue";
import PasswordReset from "@/components/auth/PasswordReset.vue";
import OAuth2Redirect from "@/components/auth/OAuth2Redirect.vue";
// Page components
import MainPage from "@/components/page/MainPage.vue";
import TermsPage from "@/components/page/Terms.vue";
import PrivacyPage from "@/components/page/Privacy.vue";
import BusinessApply from "@/components/page/BusinessApply.vue";
// Admin components
import AdminLayout from "@/components/admin/AdminLayout.vue";
import http from "@/api/http";

const routes = [
  { path: "/", component: MainPage }, // 기본 경로를 MainPage로 설정
  { path: "/login", component: Login },
  { path: "/register", component: Register },
  { path: "/terms", component: TermsPage },
  { path: "/privacy", component: PrivacyPage },
  { path: "/forgotPassword", component: ForgotPassword },
  { path: "/forgot-password", component: ForgotPassword }, // 추가 경로
  { path: "/verify", component: LoginVerify },
  { path: "/passwordReset", component: PasswordReset },
  { path: "/password-reset", component: PasswordReset }, // 추가 경로
  { path: "/oauth2/redirect", component: OAuth2Redirect },
  { path: "/business/apply", component: BusinessApply, meta: { requiresAuth: true } },
  // 관리자 라우트
  {
    path: "/admin",
    component: AdminLayout,
    meta: { requiresAdmin: true },
    children: [
      {
        path: "",
        redirect: "/admin/dashboard"
      },
      {
        path: "dashboard",
        component: () => import("@/components/admin/AdminDashboard.vue")
      },
      {
        path: "users",
        component: () => import("@/components/admin/UserManagement.vue")
      },
      {
        path: "businesses",
        component: () => import("@/components/admin/HotelManagement.vue")
      },
      {
        path: "hotels",
        component: () => import("@/components/admin/AdminHotelManagement.vue")
      },
      {
        path: "reservations",
        component: () => import("@/components/admin/ReservationMonitoring.vue")
      },
      {
        path: "sales",
        component: () => import("@/components/admin/SalesManagement.vue")
      },
      {
        path: "payments",
        component: () => import("@/components/admin/PaymentManagement.vue")
      },
      {
        path: "reviews",
        component: () => import("@/components/admin/ReviewManagement.vue")
      },
      {
        path: "coupons",
        component: () => import("@/components/page/CouponManagement.vue")
      }
    ]
  }
];
const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 관리자 권한 확인 함수
async function checkAdminRole() {
  // 1) 가능한 경우 서버에서 최신 역할 확인
  try {
    const resp = await http.get('/user/info');
    const serverRole = resp?.data?.role;
    if (serverRole) {
      localStorage.setItem('userRole', serverRole);
      return serverRole === 'ADMIN';
    }
  } catch (e) {
    // 서버가 401을 응답하면 토큰이 없거나 만료된 상태이므로 즉시 실패 처리
    if (e?.response?.status === 401) {
      return false;
    }
    // 기타 오류만 로컬 폴백
  }
  // 2) 폴백: localStorage 확인
  const userRole = localStorage.getItem('userRole');
  if (userRole) return userRole === 'ADMIN';
  const userStr = localStorage.getItem('user');
  if (userStr) {
    try {
      const user = JSON.parse(userStr);
      if (user?.role) {
        localStorage.setItem('userRole', user.role);
        return user.role === 'ADMIN';
      }
    } catch (e) {}
  }
  return false;
}

// 라우터 가드
router.beforeEach(async (to, from, next) => {
  console.log('라우터 가드 실행:', to.path);
  
  // 인증이 필요한 페이지 확인
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('로그인이 필요합니다.');
      next('/login');
      return;
    }
  }
  
  if (to.meta.requiresAdmin) {
    console.log('관리자 권한이 필요한 페이지입니다.');
    const isAdmin = await checkAdminRole();
    console.log('관리자 권한 확인 결과:', isAdmin);
    
    if (isAdmin) {
      console.log('관리자 권한 확인됨. 페이지 접근 허용.');
      next();
    } else {
      console.log('관리자 권한 없음. 메인 페이지로 리다이렉트.');
      alert('관리자 권한이 필요합니다.');
      next('/');
    }
  } else {
    next();
  }
});

export default router;