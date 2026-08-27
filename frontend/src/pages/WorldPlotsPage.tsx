import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { paissaApi } from '../api/paissaApi';
import { OwnershipTabs } from '../components/OwnershipTabs';
import { useDistricts } from '../hooks/useDistricts';
import { useWorldPlots } from '../hooks/useWorldPlots';
import type { Ownership, Plot, PlotSize, WorldDetail } from '../types';
import { OWNERSHIP_LABELS, SIZE_LABELS, formatGil } from '../utils/format';

const PAGE_SIZE = 200;

function PlotCard({ plot }: { plot: Plot }) {
  const hasActiveLotto = plot.lottoPhase !== null;
  return (
    <div className="flex flex-col gap-1 rounded-lg border border-slate-800 bg-slate-900 p-3 text-sm">
      <div className="flex items-center justify-between">
        <span className="font-semibold">
          Ward {plot.wardNumber} · Plot {plot.plotNumber}
        </span>
        <span className="rounded bg-slate-800 px-1.5 py-0.5 text-xs text-slate-400">{SIZE_LABELS[plot.size]}</span>
      </div>
      <div className="text-slate-300">{formatGil(plot.price)}</div>
      <div className="flex items-center justify-between text-xs text-slate-500">
        <span>{OWNERSHIP_LABELS[plot.ownership]}</span>
        {hasActiveLotto && <span className="rounded-full bg-amber-500/20 px-2 py-0.5 text-amber-300">Lottery</span>}
      </div>
    </div>
  );
}

export default function WorldPlotsPage() {
  const { id } = useParams<{ id: string }>();
  const worldId = Number(id);

  const [world, setWorld] = useState<WorldDetail | null>(null);
  const [size, setSize] = useState<PlotSize | undefined>(undefined);
  const [ownership, setOwnership] = useState<Ownership>('FC_ONLY');
  const [districtId, setDistrictId] = useState<number | undefined>(undefined);
  const [page, setPage] = useState(0);

  const { data: districtOptions } = useDistricts();

  useEffect(() => {
    setPage(0);
  }, [worldId, size, ownership, districtId]);

  useEffect(() => {
    paissaApi.worldDetail(worldId).then(setWorld).catch(() => setWorld(null));
  }, [worldId]);

  const { data, loading, error } = useWorldPlots(worldId, { size, ownership, districtId, page, pageSize: PAGE_SIZE });

  const plotsByDistrict = new Map<string, Plot[]>();
  for (const plot of data?.content ?? []) {
    const bucket = plotsByDistrict.get(plot.districtName) ?? [];
    bucket.push(plot);
    plotsByDistrict.set(plot.districtName, bucket);
  }

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h1 className="text-2xl font-bold">{world ? world.name : `World #${worldId}`}</h1>
        {world && (
          <p className="text-slate-400">
            {world.datacenterName} · {world.totalCount.toLocaleString()} open plots
          </p>
        )}
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <OwnershipTabs value={ownership} onChange={setOwnership} />
        <select
          value={size ?? ''}
          onChange={(e) => setSize((e.target.value || undefined) as PlotSize | undefined)}
          className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-1.5 text-sm text-slate-200"
        >
          <option value="">All sizes</option>
          <option value="SMALL">Small</option>
          <option value="MEDIUM">Medium</option>
          <option value="LARGE">Large</option>
        </select>
        <select
          value={districtId ?? ''}
          onChange={(e) => setDistrictId(e.target.value ? Number(e.target.value) : undefined)}
          className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-1.5 text-sm text-slate-200"
        >
          <option value="">All districts</option>
          {districtOptions?.map((d) => (
            <option key={d.id} value={d.id}>
              {d.name}
            </option>
          ))}
        </select>
      </div>

      {loading && <p className="text-slate-400">Loading plots…</p>}
      {error && <p className="text-red-400">{error}</p>}
      {!loading && (data?.content.length ?? 0) === 0 && !error && (
        <p className="text-slate-400">No open plots match these filters.</p>
      )}

      {[...plotsByDistrict.entries()].map(([districtName, plots]) => (
        <section key={districtName} className="flex flex-col gap-2">
          <h2 className="text-lg font-semibold text-slate-200">{districtName}</h2>
          <div className="grid grid-cols-1 gap-2 sm:grid-cols-2 lg:grid-cols-4">
            {plots.map((plot) => (
              <PlotCard key={plot.id} plot={plot} />
            ))}
          </div>
        </section>
      ))}

      {data && data.page.totalPages > 1 && (
        <div className="flex items-center justify-center gap-3 pt-2">
          <button
            type="button"
            disabled={page === 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            className="rounded-md border border-slate-700 px-3 py-1 text-sm disabled:opacity-40"
          >
            Previous
          </button>
          <span className="text-sm text-slate-400">
            Page {data.page.number + 1} of {data.page.totalPages}
          </span>
          <button
            type="button"
            disabled={page + 1 >= data.page.totalPages}
            onClick={() => setPage((p) => p + 1)}
            className="rounded-md border border-slate-700 px-3 py-1 text-sm disabled:opacity-40"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}
