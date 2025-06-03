import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true,
});

let isSyncing = false;
let syncQueue = [];

const processSyncQueue = (error = null) => {
  syncQueue.forEach(({ resolve, reject }) => {
    error ? reject(error) : resolve();
  });
  syncQueue = [];
};

axiosInstance.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      console.log("다시!")
      if (isSyncing) {
        return new Promise((resolve, reject) => {
          syncQueue.push({ resolve: () => resolve(axiosInstance(originalRequest)), reject });
        });
      }

      isSyncing = true;

      try {
        // 🎯 최신 토큰만 재설정 받기
        await axios.get('/token/sync');
        processSyncQueue();
        return axiosInstance(originalRequest); // 재시도
      } catch (syncErr) {
        processSyncQueue(syncErr);
        alert(syncErr);
        window.location.href = '/login';
        return Promise.reject(syncErr);
      } finally {
        isSyncing = false;
      }
    }

    return Promise.reject(error);
  }
);


export default axiosInstance;
