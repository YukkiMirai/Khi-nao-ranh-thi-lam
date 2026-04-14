import axiosClient from './axiosClient';
import type { UserProfile } from '../../types';
import type { AxiosResponse } from 'axios';

export const commonService = {
  /** Aggregated profile for Redux init (userInfo + roles + menus) */
  getMe: (): Promise<AxiosResponse<UserProfile>> =>
    axiosClient.get('/common/me'),
};
