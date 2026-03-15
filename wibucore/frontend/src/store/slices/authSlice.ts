import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';
import type { AuthState, LoginRequest, LoginResponse, UserProfile } from '../../types';
import { authService } from '../../services/api/authService';
import { commonService } from '../../services/api/commonService';


// ─── Async Thunks ─────────────────────────────────────────────────────────────

export const login = createAsyncThunk<LoginResponse, LoginRequest>(
  'auth/login',
  async (credentials, { rejectWithValue }) => {
    try {
      const response = await authService.login(credentials);
      return response.data;
    } catch (err: any) {
      return rejectWithValue(err.response?.data?.message ?? 'Login failed');
    }
  }
);

export const fetchUserProfile = createAsyncThunk<UserProfile>(
  'auth/fetchUserProfile',
  async (_, { rejectWithValue }) => {
    try {
      const response = await commonService.getMe();
      return response.data;
    } catch (err: any) {
      return rejectWithValue(err.response?.data?.message ?? 'Failed to load profile');
    }
  }
);

// ─── Initial State ────────────────────────────────────────────────────────────

const initialState: AuthState = {
  token: localStorage.getItem('token'),
  userUid: localStorage.getItem('userUid'),
  userId: localStorage.getItem('userId'),
  isAuthenticated: !!localStorage.getItem('token'),
  profile: null,
  loading: false,
  error: null,
};

// ─── Slice ────────────────────────────────────────────────────────────────────

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    logout(state) {
      state.token = null;
      state.userUid = null;
      state.userId = null;
      state.isAuthenticated = false;
      state.profile = null;
      state.error = null;
      localStorage.removeItem('token');
      localStorage.removeItem('userUid');
      localStorage.removeItem('userId');
    },
    clearError(state) {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    // login
    builder
      .addCase(login.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action: PayloadAction<LoginResponse>) => {
        state.loading = false;
        state.token = action.payload.token;
        state.userUid = action.payload.userUid;
        state.userId = action.payload.userId;
        state.isAuthenticated = true;
        localStorage.setItem('token', action.payload.token);
        localStorage.setItem('userUid', action.payload.userUid);
        localStorage.setItem('userId', action.payload.userId);
      })
      .addCase(login.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // fetchUserProfile
    builder
      .addCase(fetchUserProfile.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchUserProfile.fulfilled, (state, action: PayloadAction<UserProfile>) => {
        state.loading = false;
        state.profile = action.payload;
      })
      .addCase(fetchUserProfile.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });
  },
});

export const { logout, clearError } = authSlice.actions;
export default authSlice.reducer;
