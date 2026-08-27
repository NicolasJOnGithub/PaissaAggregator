import { useEffect, useRef, useState } from 'react';

interface Option<T> {
  value: T;
  label: string;
}

interface MultiSelectProps<T extends string | number> {
  label: string;
  options: Option<T>[];
  selected: T[];
  onChange: (values: T[]) => void;
}

export function MultiSelect<T extends string | number>({ label, options, selected, onChange }: MultiSelectProps<T>) {
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    function handleClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [open]);

  function toggle(value: T) {
    onChange(selected.includes(value) ? selected.filter((v) => v !== value) : [...selected, value]);
  }

  const selectedLabels = options.filter((o) => selected.includes(o.value)).map((o) => o.label);
  const summary =
    selectedLabels.length === 0
      ? 'All'
      : selectedLabels.length <= 2
        ? selectedLabels.join(', ')
        : `${selectedLabels.length} selected`;

  return (
    <div ref={containerRef} className="relative">
      <button
        type="button"
        onClick={() => setOpen((o) => !o)}
        className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-1.5 text-sm text-slate-200 hover:border-slate-600"
      >
        {label}: <span className="text-slate-400">{summary}</span>
      </button>
      {open && (
        <div className="absolute z-10 mt-1 min-w-[12rem] rounded-lg border border-slate-700 bg-slate-800 p-2 shadow-lg">
          {selected.length > 0 && (
            <button
              type="button"
              onClick={() => onChange([])}
              className="mb-1 w-full rounded px-2 py-1 text-left text-xs text-indigo-300 hover:bg-slate-700"
            >
              Clear
            </button>
          )}
          {options.map((opt) => (
            <label
              key={String(opt.value)}
              className="flex cursor-pointer items-center gap-2 rounded px-2 py-1 text-sm text-slate-200 hover:bg-slate-700"
            >
              <input
                type="checkbox"
                checked={selected.includes(opt.value)}
                onChange={() => toggle(opt.value)}
                className="accent-indigo-500"
              />
              {opt.label}
            </label>
          ))}
        </div>
      )}
    </div>
  );
}
