import { useEffect, useState } from 'react';
import { paissaApi } from '../api/paissaApi';
import type { District } from '../types';

export function useDistricts() {
  const [data, setData] = useState<District[] | null>(null);

  useEffect(() => {
    paissaApi
      .listDistricts()
      .then(setData)
      .catch(() => setData([]));
  }, []);

  return { data };
}
