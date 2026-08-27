import { useCallback, useEffect, useState } from 'react';
import { paissaApi } from '../api/paissaApi';
import type { DatacenterSummary } from '../types';

export function useDatacenters() {
  const [data, setData] = useState<DatacenterSummary[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refetch = useCallback(() => {
    setLoading(true);
    setError(null);
    paissaApi
      .datacenterSummaries()
      .then(setData)
      .catch(() => setError('Failed to load datacenters'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => refetch(), [refetch]);

  return { data, loading, error, refetch };
}
