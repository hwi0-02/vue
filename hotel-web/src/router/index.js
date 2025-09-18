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
// Admin components
import AdminLayout from "@/components/admin/AdminLayout.vue";

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
function checkAdminRole() {
  // TODO: 실제 사용자 정보에서 역할 확인
  // 현재는 임시로 localStorage 사용
  const userRole = localStorage.getItem('userRole');
  return userRole === 'ADMIN';
}

// 라우터 가드
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAdmin) {
    if (checkAdminRole()) {
      next();
    } else {
      alert('관리자 권한이 필요합니다.');
      next('/');
    }
  } else {
    next();
  }
});

export default router;