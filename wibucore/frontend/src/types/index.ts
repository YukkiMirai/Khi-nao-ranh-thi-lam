// ─── Auth ─────────────────────────────────────────────────────────────────────
export interface LoginRequest {
  userId: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userUid: string;
  userId: string;
  type: string;
}

// ─── User Info ────────────────────────────────────────────────────────────────
export interface UserInfo {
  userUid: string;
  userId: string;
  fullName: string;
  email: string;
  phone: string;
  status: string;
  type: string;
  diaChi?: string;
  tenDoanhNghiep?: string;
  soCccd?: string;
  soDkkd?: string;
}

// ─── Role ─────────────────────────────────────────────────────────────────────
export interface Role {
  roleId: string;
  name: string;
  adminRoleYn: string;
  description?: string;
  useYn: string;
  level?: number;
}

// ─── Menu ─────────────────────────────────────────────────────────────────────
export interface Menu {
  menuId: string;
  menuName: string;
  menuNameEn?: string;
  menuNameVi?: string;
  linkUri?: string;
  displayOrder?: number;
  menuType?: string;
  useYn: string;
  lev?: number;
  description?: string;
  parentMenuId?: string;
  children?: Menu[];
}

// ─── Common / Profile (for Redux init) ───────────────────────────────────────
export interface UserProfile {
  userInfo: UserInfo;
  roles: Role[];
  menus: Menu[];
}

// ─── Redux State ──────────────────────────────────────────────────────────────
export interface AuthState {
  token: string | null;
  userUid: string | null;
  userId: string | null;
  isAuthenticated: boolean;
  profile: UserProfile | null;
  loading: boolean;
  error: string | null;
}
