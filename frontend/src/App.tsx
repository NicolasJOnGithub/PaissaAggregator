import { useState } from 'react';
import { NavLink, Route, Routes } from 'react-router-dom';
import { useRefresh } from './hooks/useRefresh';
import DatacenterPage from './pages/DatacenterPage';
import WorldLeaderboardPage from './pages/WorldLeaderboardPage';
import WorldPlotsPage from './pages/WorldPlotsPage';

function NavItem({ to, children }: { to: string; children: React.ReactNode }) {
  return (
    <NavLink
      to={to}
      end={to === '/'}
      className={({ isActive }) =>
        `rounded-md px-3 py-2 text-sm font-medium transition-colors ${
          isActive ? 'bg-slate-800 text-white' : 'text-slate-400 hover:text-white'
        }`
      }
    >
      {children}
    </NavLink>
  );
}

function RefreshWidget() {
  const { status, triggering, error, trigger } = useRefresh();
  const [showKeyInput, setShowKeyInput] = useState(false);
  const [key, setKey] = useState('');

  function submit() {
    if (!key) return;
    trigger(key);
    setShowKeyInput(false);
    setKey('');
  }

  return (
    <div className="ml-auto flex items-center gap-2 text-xs text-slate-500">
      {status?.inProgress && <span className="text-amber-400">Syncing…</span>}
      {!status?.inProgress && status?.lastCompletedAt && (
        <span>Last synced {new Date(status.lastCompletedAt).toLocaleString()}</span>
      )}
      {error && <span className="text-red-400">{error}</span>}
      {showKeyInput ? (
        <span className="flex items-center gap-1">
          <input
            autoFocus
            type="password"
            value={key}
            onChange={(e) => setKey(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && submit()}
            placeholder="Refresh key"
            className="w-28 rounded border border-slate-700 bg-slate-800 px-2 py-1 text-slate-200"
          />
          <button type="button" onClick={submit} className="rounded border border-slate-700 px-2 py-1 hover:bg-slate-800">
            Go
          </button>
        </span>
      ) : (
        <button
          type="button"
          disabled={triggering || status?.inProgress}
          onClick={() => setShowKeyInput(true)}
          className="rounded border border-slate-700 px-2 py-1 hover:bg-slate-800 disabled:opacity-40"
        >
          Refresh now
        </button>
      )}
    </div>
  );
}

export default function App() {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <header className="border-b border-slate-800">
        <div className="mx-auto flex max-w-6xl items-center gap-6 px-4 py-3">
          <span className="text-lg font-semibold tracking-tight">PaissaAggregator</span>
          <nav className="flex gap-1">
            <NavItem to="/">Datacenters</NavItem>
            <NavItem to="/leaderboard">Leaderboard</NavItem>
          </nav>
          <RefreshWidget />
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-6">
        <Routes>
          <Route path="/" element={<DatacenterPage />} />
          <Route path="/leaderboard" element={<WorldLeaderboardPage />} />
          <Route path="/worlds/:id" element={<WorldPlotsPage />} />
        </Routes>
      </main>
    </div>
  );
}
