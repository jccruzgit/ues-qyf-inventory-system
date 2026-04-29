import axios from 'axios';
import { clearStoredSession, getStoredSession } from './storage';

export const SESSION_EXPIRED_EVENT = 'qyf:session-expired';

let isRedirectingToLogin = false;

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = getStoredSession()?.token;

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    const requestUrl = String(error?.config?.url ?? '');
    const isLoginRequest = requestUrl.includes('/auth/login');

    if (status === 401 && !isLoginRequest) {
      clearStoredSession();

      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent(SESSION_EXPIRED_EVENT));

        if (!isRedirectingToLogin) {
          isRedirectingToLogin = true;

          const loginUrl = new URL(window.location.origin);
          loginUrl.pathname = '/';
          loginUrl.searchParams.set('reason', 'session-expired');
          window.location.replace(loginUrl.toString());
        }
      }
    }

    return Promise.reject(error);
  },
);

export default api;
