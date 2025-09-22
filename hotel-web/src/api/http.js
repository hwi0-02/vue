import axios from 'axios';

// Axios instance configured to hit Vite proxy at /api
const http = axios.create({
  baseURL: '/api',
  withCredentials: false,
  timeout: 10000,
});

// Inject JWT token if present
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  console.log('HTTP Interceptor - 토큰 확인:', token ? '토큰 있음' : '토큰 없음');
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
    console.log('HTTP Interceptor - Authorization 헤더 추가됨');
  }
  console.log('HTTP Interceptor - 최종 요청 헤더:', config.headers);
  return config;
});

export default http;
