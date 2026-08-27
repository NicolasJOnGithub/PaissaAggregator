import { useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { MultiSelect } from '../components/MultiSelect';
import { OwnershipTabs } from '../components/OwnershipTabs';
import { useDistricts } from '../hooks/useDistricts';
import { useLeaderboard } from '../hooks/useLeaderboard';
import type { Ownership, PlotSize, Region } from '../types';
import { REGIONS, REGION_LABELS, SIZE_LABELS } from '../utils/format';

const SIZE_OPTIONS: { value: PlotSize; label: string }[] = [
  { value: 'SMALL', label: SIZE_LABELS.SMALL },
  { value: 'MEDIUM', label: SIZE_LABELS.MEDIUM },
  { value: 'LARGE', label: SIZE_LABELS.LARGE },
];

function parseListParam<T extends string>(value: string | null): T[] {
  return value ? (value.split(',') as T[]) : [];
}

export default function WorldLeaderboardPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();

  const ownership = (searchParams.get('ownership') as Ownership | null) ?? 'FC_ONLY';
  const size = parseListParam<PlotSize>(searchParams.get('size'));
  const datacenterIdParam = searchParams.get('datacenterId');
  const datacenterId = datacenterIdParam ? Number(datacenterIdParam) : undefined;
  const districtId = parseListParam(searchParams.get('districtId')).map(Number);
  const region = (searchParams.get('region') as Region | null) ?? undefined;

  const { data: districts } = useDistricts();
  const { data, loading, error } = useLeaderboard({ size, ownership, datacenterId, districtId, region });

  const rankColumnLabel = size.length === 0 ? 'Total' : size.map((s) => SIZE_LABELS[s]).join(' + ');

  const rows = useMemo(() => data ?? [], [data]);

  function updateParam(key: string, value: string | undefined) {
    const next = new URLSearchParams(searchParams);
    if (value) next.set(key, value);
    else next.delete(key);
    setSearchParams(next);
  }

  function rankValueFor(w: (typeof rows)[number]) {
    if (size.length === 0) return w.totalCount;
    return size.reduce((sum, s) => sum + (s === 'SMALL' ? w.smallCount : s === 'MEDIUM' ? w.mediumCount : w.largeCount), 0);
  }

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h1 className="text-2xl font-bold">World Leaderboard</h1>
        <p className="text-slate-400">Ranked by open plots matching the selected filters.</p>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <OwnershipTabs value={ownership} onChange={(v) => updateParam('ownership', v)} />

        <MultiSelect
          label="Size"
          options={SIZE_OPTIONS}
          selected={size}
          onChange={(values) => updateParam('size', values.length ? values.join(',') : undefined)}
        />

        <MultiSelect
          label="District"
          options={(districts ?? []).map((d) => ({ value: d.id, label: d.name }))}
          selected={districtId}
          onChange={(values) => updateParam('districtId', values.length ? values.join(',') : undefined)}
        />

        <select
          value={region ?? ''}
          onChange={(e) => updateParam('region', e.target.value || undefined)}
          className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-1.5 text-sm text-slate-200"
        >
          <option value="">All regions</option>
          {REGIONS.map((r) => (
            <option key={r} value={r}>
              {REGION_LABELS[r]}
            </option>
          ))}
        </select>

        {datacenterId && (
          <button
            type="button"
            onClick={() => updateParam('datacenterId', undefined)}
            className="rounded-full border border-indigo-500 px-3 py-1 text-xs font-medium text-indigo-300 hover:bg-indigo-500/10"
          >
            Datacenter #{datacenterId} ✕
          </button>
        )}
      </div>

      {loading && <p className="text-slate-400">Loading leaderboard…</p>}
      {error && <p className="text-red-400">{error}</p>}

      {rows.length > 0 && (
        <div className="overflow-x-auto rounded-xl border border-slate-800">
          <table className="w-full min-w-[640px] text-left text-sm">
            <thead className="bg-slate-900 text-slate-400">
              <tr>
                <th className="px-4 py-2">#</th>
                <th className="px-4 py-2">World</th>
                <th className="px-4 py-2">Datacenter</th>
                <th className="px-4 py-2 text-right">Small</th>
                <th className="px-4 py-2 text-right">Medium</th>
                <th className="px-4 py-2 text-right">Large</th>
                <th className="px-4 py-2 text-right">{rankColumnLabel}</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((w, i) => (
                <tr
                  key={w.worldId}
                  onClick={() => navigate(`/worlds/${w.worldId}`)}
                  className="cursor-pointer border-t border-slate-800 hover:bg-slate-900"
                >
                  <td className="px-4 py-2 text-slate-500">{i + 1}</td>
                  <td className="px-4 py-2 font-medium">{w.worldName}</td>
                  <td className="px-4 py-2 text-slate-400">{w.datacenterName}</td>
                  <td className="px-4 py-2 text-right">{w.smallCount.toLocaleString()}</td>
                  <td className="px-4 py-2 text-right">{w.mediumCount.toLocaleString()}</td>
                  <td className="px-4 py-2 text-right">{w.largeCount.toLocaleString()}</td>
                  <td className="px-4 py-2 text-right font-semibold text-indigo-400">
                    {rankValueFor(w).toLocaleString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {!loading && rows.length === 0 && !error && <p className="text-slate-400">No worlds match these filters yet.</p>}
    </div>
  );
}
