<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 왼쪽 : 로그인 폼 -->
      <div class="login-form">
        <div class="back-link">← 돌아가기</div>
        <h1>로그인</h1>
        <p class="sub-text">Login</p>

        <!-- 이메일 -->
        <div class="input-group">
          <label for="email">이메일</label>
          <input type="email" id="email" v-model="email" placeholder="이메일" />
        </div>

        <!-- 비밀번호 -->
        <div class="input-group">
            <label for="password">비밀번호</label>
            <div class="password-box">
                <input :type="showPassword ? 'text' : 'password'" id="password" v-model="password" placeholder="비밀번호"/>
                <button type="button" class="toggle-btn" @click="togglePassword">
                {{ showPassword ? '숨김' : '보기' }}
                </button>
            </div>
        </div>

        <!-- 비밀번호 저장 -->
        <div class="options">
          <label>
            <input type="checkbox" v-model="remember" /> 비밀번호 저장하기
          </label>
          <a href="#" class="forgot" @click="$router.push('forgotPassword')">비밀번호를 잊으셨나요?</a>
        </div>

        <!-- 로그인 버튼 -->
        <button class="login-btn" @click="login">로그인</button>

        <button class="register-btn" @click="$router.push('/register')">회원가입</button>

        <!-- 구분선 -->
        <div class="divider">-------------------------------- 또는 ---------------------------------</div>

        <!-- 소셜 로그인 -->
        <div class="social-login">
          <div class="social-box" @click="naverLogin">
            <img src="/naverLogo.png" alt="네이버 로그인" />
          </div>
          <div class="social-box" @click="googleLogin">
            <img src="/googleLogo.png" alt="구글 로그인" />
          </div>
          <div class="social-box" @click="kakaoLogin">
            <img src="/kakaoLogo.png" alt="카카오 로그인" />
          </div>
        </div>
      </div>

      <!-- 오른쪽 : 로고 -->
      <div class="login-logo">
        <img src="/egodaLogo.png" alt="egoda" />
      </div>
    </div>
  </div>
</template>

<style scoped src="@/assets/css/login/login.css"></style>

<script>
import axios from "axios";

export default {
  name: "LoginPage",
  data() {
    return {
      email: "",
      password: "",
      remember: false,
      showPassword: false,
    };
  },
  methods: {
    togglePassword() {
      this.showPassword = !this.showPassword;
    },
    async login() {
      try {
        const response = await axios.post("http://localhost:8888/users/login", null, {
          params: {
            email: this.email,
            password: this.password,
          },
        });
        console.log("로그인 성공:", response.data);
        alert("로그인 성공!");
      } catch (error) {
        console.error("로그인 실패:", error.response?.data || error.message);
        alert("로그인 실패!");
      }
    },
    naverLogin() {
      // 네이버 OAuth2 로그인 URL로 리다이렉트
      window.location.href = "http://localhost:8888/oauth2/authorization/naver";
    },
    googleLogin() {
      // 구글 OAuth2 로그인 URL로 리다이렉트
      window.location.href = "http://localhost:8888/oauth2/authorization/google";
    },
    kakaoLogin() {
      // 카카오 OAuth2 로그인 URL로 리다이렉트
      window.location.href = "http://localhost:8888/oauth2/authorization/kakao";
    },
  },
};
</script>
