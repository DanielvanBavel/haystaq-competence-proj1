import { useCallback, useEffect, useState } from 'react';
import { api } from '../api';
import { Absence, Employee } from '../types';
import { DataTable } from '../components/DataTable';
import { EntityForm } from '../components/EntityForm';

export function Absences() {
  const [absences, setAbsences] = useState<Absence[]>([]);
  const [employees, setEmployees] = useState<Employee[]>([]);

  const reload = useCallback(async () => {
    const [absenceList, employeeList] = await Promise.all([
      api.get<Absence[]>('/absences'),
      api.get<Employee[]>('/employees')
    ]);
    setAbsences(absenceList);
    setEmployees(employeeList);
  }, []);

  useEffect(() => {
    void reload();
  }, [reload]);

  return (
    <section>
      <h2>Verlof en verzuim</h2>
      <EntityForm
        testId="absence-form"
        fields={[
          {
            name: 'employeeId',
            label: 'Medewerker',
            type: 'select',
            options: employees.map((e) => ({ value: e.id, label: `${e.employeeCode} ${e.firstName} ${e.lastName}` }))
          },
          {
            name: 'absenceType',
            label: 'Soort',
            type: 'select',
            options: [
              { value: 'VACATION', label: 'Vakantie' },
              { value: 'SICK', label: 'Ziek' },
              { value: 'PARENTAL', label: 'Ouderschapsverlof' },
              { value: 'UNPAID', label: 'Onbetaald verlof' },
              { value: 'SPECIAL', label: 'Bijzonder verlof' }
            ]
          },
          { name: 'startDate', label: 'Van', type: 'date' },
          { name: 'endDate', label: 'Tot en met', type: 'date' },
          { name: 'hoursPerDay', label: 'Uren per dag', type: 'number', step: '0.5', defaultValue: 8 },
          { name: 'reason', label: 'Reden' },
          { name: 'approved', label: 'Goedgekeurd', type: 'checkbox' }
        ]}
        submitLabel="Opslaan"
        onSubmit={async (values) => {
          await api.post('/absences', values);
          await reload();
        }}
      />

      <DataTable
        testId="absence-table"
        rows={absences}
        columns={[
          { label: 'Medewerker', render: (row) => row.employeeCode ?? '' },
          { label: 'Soort', render: (row) => row.absenceType },
          { label: 'Van', render: (row) => row.startDate },
          { label: 'Tot', render: (row) => row.endDate },
          { label: 'Uren/dag', render: (row) => row.hoursPerDay },
          { label: 'Goedgekeurd', render: (row) => (row.approved ? 'ja' : 'nee') },
          { label: 'Reden', render: (row) => row.reason ?? '' },
          {
            label: '',
            render: (row) => (
              <button
                type="button"
                onClick={async () => {
                  await api.del(`/absences/${row.id}`);
                  await reload();
                }}
              >
                verwijderen
              </button>
            )
          }
        ]}
      />
    </section>
  );
}
