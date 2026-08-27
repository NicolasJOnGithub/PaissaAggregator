import type { Ownership } from '../types';
import { OWNERSHIP_LABELS, OWNERSHIP_TABS } from '../utils/format';

interface OwnershipTabsProps {
  value: Ownership;
  onChange: (value: Ownership) => void;
}

export function OwnershipTabs({ value, onChange }: OwnershipTabsProps) {
  return (
    <div className="inline-flex rounded-lg border border-slate-700 bg-slate-800 p-1">
      {OWNERSHIP_TABS.map((tab) => (
        <button
          key={tab}
          type="button"
          onClick={() => onChange(tab)}
          className={`rounded-md px-4 py-1.5 text-sm font-medium transition-colors ${
            value === tab ? 'bg-indigo-500 text-white' : 'text-slate-300 hover:text-white'
          }`}
        >
          {OWNERSHIP_LABELS[tab]}
        </button>
      ))}
    </div>
  );
}
