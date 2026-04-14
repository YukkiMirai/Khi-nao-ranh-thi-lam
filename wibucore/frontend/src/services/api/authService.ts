import axiosClient from './axiosClient';
import type { LoginRequest, LoginResponse } from '../../types';
import type { AxiosResponse } from 'axios';

export const authService = {
  login: (data: LoginRequest): Promise<AxiosResponse<LoginResponse>> =>
    axiosClient.post('/auth/login', data),
};
