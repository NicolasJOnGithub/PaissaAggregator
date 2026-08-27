import axios from 'axios';
import type {
  DatacenterSummary,
  District,
  PagedResponse,
  Plot,
  PlotSize,
  Ownership,
  Region,
  RefreshStatus,
  World,
  WorldDetail,
  WorldStats,
} from '../types';

// Spring's `@RequestParam List<T>` binds repeated `key=a&key=b` pairs, not axios's default
// `key[]=a&key[]=b` bracket notation — serialize arrays the plain way so multi-select filters work.
function serializeParams(params: Record<string, unknown>): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null) return;
    if (Array.isArray(value)) {
      value.forEach((v) => search.append(key, String(v)));
    } else {
      search.append(key, String(value));
    }
  });
  return search.toString();
}

const client = axios.create({ baseURL: '/api', paramsSerializer: { serialize: serializeParams } });

export interface DatacenterParams {
  region?: Region;
}

export interface LeaderboardParams {
  size?: PlotSize[];
  ownership?: Ownership;
  datacenterId?: number;
  districtId?: number[];
  region?: Region;
}

export interface WorldPlotsParams {
  size?: PlotSize[];
  ownership?: Ownership;
  districtId?: number[];
  page?: number;
  pageSize?: number;
}

export const paissaApi = {
  listWorlds: () => client.get<World[]>('/worlds').then((r) => r.data),

  listDistricts: () => client.get<District[]>('/districts').then((r) => r.data),

  worldDetail: (worldId: number) => client.get<WorldDetail>(`/worlds/${worldId}`).then((r) => r.data),

  worldPlots: (worldId: number, params: WorldPlotsParams = {}) =>
    client.get<PagedResponse<Plot>>(`/worlds/${worldId}/plots`, { params }).then((r) => r.data),

  datacenterSummaries: (params: DatacenterParams = {}) =>
    client.get<DatacenterSummary[]>('/datacenters', { params }).then((r) => r.data),

  worldLeaderboard: (params: LeaderboardParams = {}) =>
    client.get<WorldStats[]>('/leaderboard/worlds', { params }).then((r) => r.data),

  triggerRefresh: (refreshKey: string) =>
    client.post<void>('/refresh', null, { headers: { 'X-Refresh-Key': refreshKey } }).then((r) => r.status),

  refreshStatus: () => client.get<RefreshStatus>('/refresh/status').then((r) => r.data),
};
