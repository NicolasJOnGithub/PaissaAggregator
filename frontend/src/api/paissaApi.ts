import axios from 'axios';
import type {
  DatacenterSummary,
  District,
  PagedResponse,
  Plot,
  PlotSize,
  Ownership,
  RefreshStatus,
  World,
  WorldDetail,
  WorldStats,
} from '../types';

const client = axios.create({ baseURL: '/api' });

export interface LeaderboardParams {
  size?: PlotSize;
  ownership?: Ownership;
  datacenterId?: number;
  districtId?: number;
}

export interface WorldPlotsParams {
  size?: PlotSize;
  ownership?: Ownership;
  districtId?: number;
  page?: number;
  pageSize?: number;
}

export const paissaApi = {
  listWorlds: () => client.get<World[]>('/worlds').then((r) => r.data),

  listDistricts: () => client.get<District[]>('/districts').then((r) => r.data),

  worldDetail: (worldId: number) => client.get<WorldDetail>(`/worlds/${worldId}`).then((r) => r.data),

  worldPlots: (worldId: number, params: WorldPlotsParams = {}) =>
    client.get<PagedResponse<Plot>>(`/worlds/${worldId}/plots`, { params }).then((r) => r.data),

  datacenterSummaries: () => client.get<DatacenterSummary[]>('/datacenters').then((r) => r.data),

  worldLeaderboard: (params: LeaderboardParams = {}) =>
    client.get<WorldStats[]>('/leaderboard/worlds', { params }).then((r) => r.data),

  triggerRefresh: (refreshKey: string) =>
    client.post<void>('/refresh', null, { headers: { 'X-Refresh-Key': refreshKey } }).then((r) => r.status),

  refreshStatus: () => client.get<RefreshStatus>('/refresh/status').then((r) => r.data),
};
