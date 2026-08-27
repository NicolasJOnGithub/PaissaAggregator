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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params.size, params.ownership, params.datacenterId]);

  return { data, loading, error };
}
