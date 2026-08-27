import type { Ownership, PlotSize } from '../types';

export function formatGil(amount: number): string {
  return `${amount.toLocaleString('en-US')} gil`;
}

export function formatRelativeTime(epochSeconds: number): string {
  const deltaMs = Date.parse(new Date(epochSeconds * 1000).toISOString()) - Date.now();
  const minutes = Math.round(deltaMs / 60000);
  const formatter = new Intl.RelativeTimeFormat('en', { numeric: 'auto' });
  if (Math.abs(minutes) < 60) return formatter.format(minutes, 'minute');
  const hours = Math.round(minutes / 60);
  if (Math.abs(hours) < 24) return formatter.format(hours, 'hour');
  return formatter.format(Math.round(hours / 24), 'day');
}

export const SIZE_LABELS: Record<PlotSize, string> = {
  SMALL: 'Small',
  MEDIUM: 'Medium',
  LARGE: 'Large',
};

export const OWNERSHIP_LABELS: Record<Ownership, string> = {
  FC_ONLY: 'FC-only',
  INDIVIDUAL_ONLY: 'Individual-only',
  UNRESTRICTED: 'Unrestricted',
};

export const OWNERSHIP_TABS: Ownership[] = ['FC_ONLY', 'INDIVIDUAL_ONLY', 'UNRESTRICTED'];
