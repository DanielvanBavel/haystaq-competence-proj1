import { useState } from 'react';
import { NavLink, Navigate, Route, Routes } from 'react-router-dom';
import { api } from './api';
import { Dashboard } from './pages/Dashboard';
import { Employees } from './pages/Employees';
import { Projects } from './pages/Projects';
import { Timesheets } from './pages/Timesheets';
import { Absences } from './pages/Absences';
import { Expenses } from './pages/Expenses';

const TABS = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/medewerkers', label: 'Medewerkers' },
  { to: '/projecten', label: 'Projecten' },
  { to: '/weekstaten', label: 'Weekstaten' },
  { to: '/verlof', label: 'Verlof' },
  { to: '/declaraties', label: 'Declaraties' }
];

export function App() {
  const [resetMessage, setResetMessage] = useState<string | null>(null);

  async function reset() {
    setResetMessage('Bezig...');
    try {
      await api.post('/admin/reset');
      setResetMessage('Database teruggezet naar de seed');
      window.location.reload();
    } catch (error) {
      setResetMessage(String(error));
    }
  }

  return (
    <div className="app">
      <header>
        <h1>TijdWijs</h1>
        <nav>
          {TABS.map((tab) => (
            <NavLink key={tab.to} to={tab.to} className={({ isActive }) => (isActive ? 'tab selected' : 'tab')}>
              {tab.label}
            </NavLink>
          ))}
        </nav>
      </header>

      <main>
        <Routes>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/medewerkers" element={<Employees />} />
          <Route path="/projecten" element={<Projects />} />
          <Route path="/weekstaten" element={<Timesheets />} />
          <Route path="/verlof" element={<Absences />} />
          <Route path="/declaraties" element={<Expenses />} />
        </Routes>
      </main>

      <footer>
        <button type="button" onClick={() => void reset()}>
          Demo-data herstellen
        </button>
        {resetMessage ? <span>{resetMessage}</span> : null}
      </footer>
    </div>
  );
}
