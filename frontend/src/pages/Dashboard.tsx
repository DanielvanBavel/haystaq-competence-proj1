import { useEffect, useState } from 'react';
import { api } from '../api';

type Summary = Record<string, number | string>;
type WeeklyRow = Record<string, number | string>;

const LABELS: Record<string, string> = {
  active_employees: 'Actieve medewerkers',
  active_projects: 'Actieve projecten',
  clients: 'Opdrachtgevers',
  timesheets: 'Weekstaten',
  time_entries: 'Urenregels',
  total_hours: 'Totaal uren',
  absences: 'Verlofperiodes',
  expense_claims: 'Declaraties'
};

export function Dashboard() {
  const [summary, setSummary] = useState<Summary>({});
  const [weekly, setWeekly] = useState<WeeklyRow[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([api.get<Summary>('/reports/summary'), api.get<WeeklyRow[]>('/reports/weekly')])
      .then(([summaryResult, weeklyResult]) => {
        setSummary(summaryResult);
        setWeekly(weeklyResult);
      })
      .catch((problem) => setError(String(problem)));
  }, []);

  return (
    <section>
      <h2>Overzicht</h2>
      {error ? <p className="msg error">{error}</p> : null}
      <div className="cards">
        {Object.entries(summary).map(([key, value]) => (
          <div className="card" key={key} data-testid={`stat-${key}`}>
            <span className="value">{String(value)}</span>
            <span className="label">{LABELS[key] ?? key}</span>
          </div>
        ))}
      </div>

      <h3>Weektotalen</h3>
      {weekly.length === 0 ? (
        <p className="empty">Nog geen geboekte uren</p>
      ) : (
        <table>
          <thead>
          <tr>
            <th>Medewerker</th>
            <th>Jaar</th>
            <th>Week</th>
            <th>Status</th>
            <th>Uren</th>
            <th>Declarabel</th>
            <th>Regels</th>
          </tr>
          </thead>
          <tbody>
          {weekly.map((row, index) => (
            <tr key={index}>
              <td>{String(row.employee_code)}</td>
              <td>{String(row.iso_year)}</td>
              <td>{String(row.iso_week)}</td>
              <td>{String(row.status)}</td>
              <td>{String(row.total_hours)}</td>
              <td>{String(row.billable_hours)}</td>
              <td>{String(row.entry_count)}</td>
            </tr>
          ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
