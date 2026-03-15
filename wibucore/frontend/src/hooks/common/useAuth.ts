import { useEffect } from 'react';
import { useAppDispatch, useAppSelector } from './useStore';
import { login, logout, fetchUserProfile, clearError } from '../../store/slices/authSlice';
import type { LoginRequest } from '../../types';

export const useAuth = () => {
  const dispatch = useAppDispatch();
  const { token, userId, userUid, isAuthenticated, profile, loading, error } =
    useAppSelector((state) => state.auth);

  // Auto-fetch profile once authenticated but profile not yet loaded
  useEffect(() => {
    if (isAuthenticated && !profile) {
      dispatch(fetchUserProfile());
    }
  }, [isAuthenticated, profile, dispatch]);

  const handleLogin = (credentials: LoginRequest) => dispatch(login(credentials));
  const handleLogout = () => dispatch(logout());
  const handleClearError = () => dispatch(clearError());

  return {
    token,
    userId,
    userUid,
    isAuthenticated,
    profile,
    loading,
    error,
    login: handleLogin,
    logout: handleLogout,
    clearError: handleClearError,
  };
};
