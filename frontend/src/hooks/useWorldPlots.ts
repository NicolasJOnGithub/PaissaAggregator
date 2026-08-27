import { useEffect, useState } from 'react';
import { paissaApi, type WorldPlotsParams } from '../api/paissaApi';
import type { PagedResponse, Plot } from '../types';

export function useWorldPlots(worldId: number, params: WorldPlotsParams) {
  const [data, setData] = useState<PagedResponse<Plot> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    paissaApi
      .worldPlots(worldId, params)
      .then((res) => {
        if (!cancelled) setData(res);
      })
      .catch(() => {
        if (!cancelled) setError('Failed to load plots');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
    // Arrays are compared by joined value, not reference, so a fresh array literal each render
    // doesn't retrigger the fetch.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    worldId,
    params.size?.join(','),
    params.ownership,
    params.districtId?.join(','),
    params.page,
    params.pageSize,
  ]);

  return { data, loading, error };
}
