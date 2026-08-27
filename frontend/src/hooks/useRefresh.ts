import { useCallback, useEffect, useState } from 'react';
import { paissaApi } from '../api/paissaApi';
import type { RefreshStatus } from '../types';

export function useRefresh() {
  const [status, setStatus] = useState<RefreshStatus | null>(null);
  const [triggering, setTriggering] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchStatus = useCallback(() => {
    paissaApi.refreshStatus().then(setStatus).catch(() => undefined);
  }, []);

  useEffect(() => {
    fetchStatus();
    const interval = setInterval(fetchStatus, 5000);
    return () => clearInterval(interval);
  }, [fetchStatus]);

  const trigger = useCallback(
    async (refreshKey: string) => {
      setTriggering(true);
      setError(null);
      try {
        await paissaApi.triggerRefresh(refreshKey);
        fetchStatus();
      } catch (e) {
        const status = (e as { response?: { status?: number } })?.response?.status;
        if (status === 401) setError('Invalid refresh key');
        else if (status === 409) setError('A refresh is already in progress');
        else setError('Failed to trigger refresh');
      } finally {
        setTriggering(false);
      }
    },
    [fetchStatus],
  );

  return { status, triggering, error, trigger };
}
