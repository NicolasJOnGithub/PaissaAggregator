import { useCallback, useEffect, useState } from 'react';
import { paissaApi } from '../api/paissaApi';
import type { DatacenterSummary, Region } from '../types';

export function useDatacenters(region?: Region) {
  const [data, setData] = useState<DatacenterSummary[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refetch = useCallback(() => {
    setLoading(true);
    setError(null);
    paissaApi
      .datacenterSummaries({ region })
      .then(setData)
      .catch(() => setError('Failed to load datacenters'))
      .finally(() => setLoading(false));
  }, [region]);

  useEffect(() => refetch(), [refetch]);

  return { data, loading, error, refetch };
}
