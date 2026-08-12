import { useCallback, useEffect, useState } from 'react';
import { api, ApiError } from '../api';
import { Employee, Project, Timesheet } from '../types';
import { DataTable } from '../components/DataTable';
import { EntityForm, FieldDefinition } from '../components/EntityForm';

export function Timesheets() {
  const [timesheets, setTimesheets] = useState<Timesheet[]>([]);
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [selected, setSelected] = useState<Timesheet | null>(null);
  const [approver, setApprover] = useState('');
  const [actionMessage, setActionMessage] = useState<string | null>(null);

  const reload = useCallback(async () => {
    const [sheets, employeeList, projectList] = await Promise.all([
      api.get<Timesheet[]>('/timesheets'),
      api.get<Employee[]>('/employees'),
      api.get<Project[]>('/projects')
    ]);
    setTimesheets(sheets);
    setEmployees(employeeList);
    setProjects(projectList);
  }, []);

  useEffect(() => {
    void reload();
  }, [reload]);

  async function open(id: string) {
    setActionMessage(null);
    setSelected(await api.get<Timesheet>(`/timesheets/${id}`));
  }

  async function runAction(action: () => Promise<unknown>) {
    try {
      await action();
      setActionMessage('Gelukt');
      if (selected) {
        await open(selected.id);
      }
      await reload();
    } catch (error) {
      setActionMessage(error instanceof ApiError ? error.describe() : String(error));
    }
  }

  const taskOptions = projects.flatMap((project) =>
    project.tasks.map((task) => ({ value: task.id, label: `${project.code} / ${task.name}` }))
  );

  const entryFields: FieldDefinition[] = [
    { name: 'taskId', label: 'Taak', type: 'select', options: taskOptions },
    { name: 'workDate', label: 'Datum', type: 'date', defaultValue: selected?.weekStart ?? '' },
    { name: 'hours', label: 'Uren', type: 'number', step: '0.25', defaultValue: 8 },
    {
      name: 'entryType',
      label: 'Soort',
      type: 'select',
      options: [
        { value: 'REGULAR', label: 'Regulier' },
        { value: 'OVERTIME', label: 'Overwerk' },
        { value: 'TRAVEL', label: 'Reistijd' },
        { value: 'STANDBY', label: 'Consignatie' },
        { value: 'TRAINING', label: 'Opleiding' }
      ]
    },
    { name: 'description', label: 'Omschrijving' }
  ];

  return (
    <section>
      <h2>Weekstaten</h2>

      <EntityForm
        title="Weekstaat openen"
        testId="timesheet-form"
        fields={[
          {
            name: 'employeeId',
            label: 'Medewerker',
            type: 'select',
            options: employees.map((e) => ({ value: e.id, label: `${e.employeeCode} ${e.firstName} ${e.lastName}` }))
          },
          { name: 'isoYear', label: 'Jaar', type: 'number', defaultValue: 2026 },
          { name: 'isoWeek', label: 'Week', type: 'number', defaultValue: 6 }
        ]}
        submitLabel="Openen"
        onSubmit={async (values) => {
          const created = await api.post<Timesheet>('/timesheets', values);
          await reload();
          await open(created.id);
        }}
      />

      <DataTable
        testId="timesheet-table"
        rows={timesheets}
        columns={[
          { label: 'Medewerker', render: (row) => row.employeeCode ?? '' },
          { label: 'Jaar', render: (row) => row.isoYear },
          { label: 'Week', render: (row) => row.isoWeek },
          { label: 'Status', render: (row) => row.status },
          { label: 'Uren', render: (row) => row.totalHours },
          {
            label: '',
            render: (row) => (
              <button type="button" onClick={() => void open(row.id)}>
                Open
              </button>
            )
          }
        ]}
      />

      {selected ? (
        <div className="panel" data-testid="timesheet-detail">
          <h3>
            {selected.employeeCode} - week {selected.isoWeek}/{selected.isoYear} ({selected.weekStart} t/m{' '}
            {selected.weekEnd}) - {selected.status}
          </h3>

          <DataTable
            testId="entry-table"
            rows={selected.entries}
            columns={[
              { label: 'Datum', render: (row) => row.workDate },
              { label: 'Project', render: (row) => row.projectCode ?? '' },
              { label: 'Taak', render: (row) => row.taskName ?? '' },
              { label: 'Uren', render: (row) => row.hours },
              { label: 'Soort', render: (row) => row.entryType },
              { label: 'Declarabel', render: (row) => (row.billable ? 'ja' : 'nee') },
              { label: 'Omschrijving', render: (row) => row.description ?? '' },
              {
                label: '',
                render: (row) => (
                  <button type="button" onClick={() => void runAction(() => api.del(`/time-entries/${row.id}`))}>
                    verwijderen
                  </button>
                )
              }
            ]}
            empty="Nog geen uren geboekt"
          />

          <EntityForm
            title="Uren boeken"
            testId="entry-form"
            fields={entryFields}
            submitLabel="Boeken"
            onSubmit={async (values) => {
              await api.post(`/timesheets/${selected.id}/entries`, values);
              await open(selected.id);
              await reload();
            }}
          />

          <div className="actions">
            <button type="button" onClick={() => void runAction(() => api.post(`/timesheets/${selected.id}/submit`, {}))}>
              Indienen
            </button>
            <select value={approver} onChange={(event) => setApprover(event.target.value)}>
              <option value="">Goedkeurder...</option>
              {employees.map((employee) => (
                <option key={employee.id} value={employee.id}>
                  {employee.employeeCode} {employee.firstName} {employee.lastName}
                </option>
              ))}
            </select>
            <button
              type="button"
              onClick={() =>
                void runAction(() => api.post(`/timesheets/${selected.id}/approve`, { approvedBy: approver }))
              }
            >
              Goedkeuren
            </button>
            {actionMessage ? <span className="msg">{actionMessage}</span> : null}
          </div>
        </div>
      ) : null}
    </section>
  );
}
