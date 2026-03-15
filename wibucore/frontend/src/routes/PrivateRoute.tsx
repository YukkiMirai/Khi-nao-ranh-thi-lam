import { Navigate, Outlet } from 'react-router-dom';
import { useAppSelector } from '../hooks/common/useStore';

/** Wraps private routes — redirects to /login if not authenticated */
export const PrivateRoute = () => {
  const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
};
