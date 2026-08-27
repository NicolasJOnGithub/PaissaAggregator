import { useEffect, useState } from 'react';
import { paissaApi, type LeaderboardParams } from '../api/paissaApi';
import type { WorldStats } from '../types';

export function useLeaderboard(params: LeaderboardParams) {
  const [data, setData] = useState<WorldStats[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    paissaApi
      .worldLeaderboard(params)
      .then((res) => {
        if (!cancelled) setData(res);
      })
      .catch(() => {
        if (!cancelled) setError('Failed to load leaderboard');
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
    params.size?.join(','),
    params.ownership,
    params.datacenterId,
    params.districtId?.join(','),
    params.region,
  ]);

  return { data, loading, error };
}
