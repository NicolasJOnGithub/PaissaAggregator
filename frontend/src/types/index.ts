export type PlotSize = 'SMALL' | 'MEDIUM' | 'LARGE';

export type Ownership = 'FC_ONLY' | 'INDIVIDUAL_ONLY' | 'UNRESTRICTED';

export interface World {
  id: number;
  name: string;
  datacenterId: number;
  datacenterName: string;
}

export interface WorldDetail extends World {
  smallCount: number;
  mediumCount: number;
  largeCount: number;
  totalCount: number;
}

export interface DatacenterSummary {
  datacenterId: number;
  datacenterName: string;
  smallCount: number;
  mediumCount: number;
  largeCount: number;
  totalCount: number;
}

export interface WorldStats {
  worldId: number;
  worldName: string;
  datacenterId: number;
  datacenterName: string;
  smallCount: number;
  mediumCount: number;
  largeCount: number;
  totalCount: number;
}

export interface Plot {
  id: number;
  worldId: number;
  worldName: string;
  districtId: number;
  districtName: string;
  wardNumber: number;
  plotNumber: number;
  size: PlotSize;
  price: number;
  ownership: Ownership;
  lottoEntries: number | null;
  lottoPhase: number | null;
  lottoPhaseUntil: number | null;
  firstSeenTime: number;
  lastUpdatedTime: number;
}

export interface PagedResponse<T> {
  content: T[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

export interface RefreshStatus {
  lastStartedAt: string | null;
  lastCompletedAt: string | null;
  inProgress: boolean;
  worldsSynced: number | null;
  lastError: string | null;
}
