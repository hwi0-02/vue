import { createRouter, createWebHistory } from "vue-router";
import Login from "@/components/Login.vue";
import Register from "@/components/Register.vue";
import ForgotPassword from "@/components/ForgotPassword.vue";
import LoginVerify from "@/components/LoginVerify.vue";
import PasswordReset from "@/components/PasswordReset.vue";
import OAuth2Redirect from "@/components/OAuth2Redirect.vue";

const routes = [
  { path: "/", component: Login },
  { path: "/login", component: Login },
  { path: "/register", component: Register },
  { path: "/forgotPassword", component: ForgotPassword },
  { path: "/verify", component: LoginVerify },
  { path: "/passwordReset", component: PasswordReset },
  { path: "/oauth2/redirect", component: OAuth2Redirect },
];
const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
