export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export const TOKEN_KEY = 'token';
export const USER_UID_KEY = 'userUid';
export const USER_ID_KEY = 'userId';

export const ROUTES = {
  LOGIN: '/login',
  DASHBOARD: '/dashboard',
} as const;
