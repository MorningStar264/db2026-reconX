// useMemo for portfolio-value calc.
// useTradeStream live feed.
import React, { useMemo } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import { useTradeStream } from '@hooks/useTradeStream.js';
import { Profiler } from 'react';

function StatCard({ label, value }) {
  return (
    <article className="stat-card">
      <h3>{label}</h3>
      <p>{value}</p>
    </article>
  );
}
function onRender(id, phase, actualDuration, baseDuration) {
  // eslint-disable-next-line no-console
  console.log(`[Profiler] ${id} ${phase}  actual=${actualDuration.toFixed(2)}ms  base=${baseDuration.toFixed(2)}ms`);
}

function DashboardContents() {
  const { trades, isConnected } = useTradeStream();

  const portfolioValue = useMemo(
    () => trades.reduce((sum, t) => sum + (t.quantity * t.price || 0), 0),
    [trades]
  );

  const matched = trades.filter((t) => t.status === 'MATCHED').length;
  const breaks  = trades.filter((t) => ['UNMATCHED','DISPUTED'].includes(t.status)).length;

  return (
    <section>
      <h2>Dashboard</h2>
      <div className="stat-grid">
        <StatCard label="Portfolio value (USD)" value={portfolioValue.toLocaleString()} />
        <StatCard label="Trades streamed" value={trades.length} />
        <StatCard label="Matched" value={matched} />
        <StatCard label="Open breaks" value={breaks} />
      </div>
      <div role="status" aria-live="polite">
        SSE: {isConnected ? 'connected' : 'disconnected'}
      </div>
    </section>
  );
}

export default function Dashboard({ trades }) {
  return (
    <Profiler id="TradeDashboard" onRender={onRender}>
      <DashboardContents trades={trades} />
    </Profiler>
  );
}
