import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDatacenters } from '../hooks/useDatacenters';
import type { DatacenterSummary, Region } from '../types';
import { REGIONS, REGION_LABELS } from '../utils/format';

function DatacenterCard({ dc, onClick }: { dc: DatacenterSummary; onClick: () => void }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="flex flex-col gap-3 rounded-xl border border-slate-800 bg-slate-900 p-5 text-left transition-colors hover:border-indigo-500"
    >
      <div className="flex items-baseline justify-between">
        <h3 className="text-lg font-semibold">{dc.datacenterName}</h3>
        <span className="text-2xl font-bold text-indigo-400">{dc.totalCount.toLocaleString()}</span>
      </div>
      <div className="grid grid-cols-3 gap-2 text-sm text-slate-400">
        <div>
          <div className="text-slate-500">Small</div>
          <div className="font-medium text-slate-200">{dc.smallCount.toLocaleString()}</div>
        </div>
        <div>
          <div className="text-slate-500">Medium</div>
          <div className="font-medium text-slate-200">{dc.mediumCount.toLocaleString()}</div>
        </div>
        <div>
          <div className="text-slate-500">Large</div>
          <div className="font-medium text-slate-200">{dc.largeCount.toLocaleString()}</div>
        </div>
      </div>
    </button>
  );
}

export default function DatacenterPage() {
  const [region, setRegion] = useState<Region | undefined>(undefined);
  const { data, loading, error } = useDatacenters(region);
  const navigate = useNavigate();

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h1 className="text-2xl font-bold">Open Plots by Datacenter</h1>
        <p className="text-slate-400">At-a-glance view of available housing plots across every datacenter.</p>
      </div>

      <select
        value={region ?? ''}
        onChange={(e) => setRegion((e.target.value || undefined) as Region | undefined)}
        className="w-fit rounded-lg border border-slate-700 bg-slate-800 px-3 py-1.5 text-sm text-slate-200"
      >
        <option value="">All regions</option>
        {REGIONS.map((r) => (
          <option key={r} value={r}>
            {REGION_LABELS[r]}
          </option>
        ))}
      </select>

      {loading && <p className="text-slate-400">Loading datacenters…</p>}
      {error && <p className="text-red-400">{error}</p>}

      {data && data.length === 0 && (
        <p className="text-slate-400">
          No plot data yet — trigger a refresh (see <code>POST /api/refresh</code>) and check back shortly.
        </p>
      )}

      {data && data.length > 0 && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {data.map((dc) => (
            <DatacenterCard
              key={dc.datacenterId}
              dc={dc}
              onClick={() => navigate(`/leaderboard?datacenterId=${dc.datacenterId}`)}
            />
          ))}
        </div>
      )}
    </div>
  );
}
