import { useCallback, useEffect, useState } from 'react';
import { api } from '../api';
import { Employee } from '../types';
import { DataTable } from '../components/DataTable';
import { EntityForm, FieldDefinition } from '../components/EntityForm';

export function Employees() {
  const [employees, setEmployees] = useState<Employee[]>([]);

  const reload = useCallback(async () => {
    setEmployees(await api.get<Employee[]>('/employees'));
  }, []);

  useEffect(() => {
    void reload();
  }, [reload]);

  const fields: FieldDefinition[] = [
    { name: 'employeeCode', label: 'Personeelsnummer', placeholder: 'EMP-0007' },
    { name: 'firstName', label: 'Voornaam' },
    { name: 'lastName', label: 'Achternaam' },
    { name: 'email', label: 'E-mail', type: 'email' },
    { name: 'birthDate', label: 'Geboortedatum', type: 'date' },
    { name: 'hireDate', label: 'Datum in dienst', type: 'date' },
    { name: 'endDate', label: 'Datum uit dienst', type: 'date' },
    {
      name: 'contractType',
      label: 'Contractvorm',
      type: 'select',
      options: [
        { value: 'PERMANENT', label: 'Vast' },
        { value: 'TEMPORARY', label: 'Tijdelijk' },
        { value: 'FREELANCE', label: 'Freelance' },
        { value: 'INTERN', label: 'Stagiair' }
      ]
    },
    { name: 'contractHours', label: 'Contracturen', type: 'number', step: '0.5', defaultValue: 40 },
    { name: 'hourlyRate', label: 'Uurtarief', type: 'number', step: '0.01', defaultValue: 85 },
    { name: 'iban', label: 'IBAN', placeholder: 'NL91ABNA0417164300' },
    { name: 'phone', label: 'Telefoon', type: 'tel', placeholder: '+31612345678' },
    {
      name: 'managerId',
      label: 'Manager',
      type: 'select',
      options: [{ value: '', label: '-' }, ...employees.map((e) => ({
        value: e.id,
        label: `${e.employeeCode} ${e.firstName} ${e.lastName}`
      }))]
    },
    { name: 'active', label: 'Actief', type: 'checkbox', defaultValue: true }
  ];

  return (
    <section>
      <h2>Medewerkers</h2>
      <EntityForm
        testId="employee-form"
        fields={fields}
        submitLabel="Opslaan"
        onSubmit={async (values) => {
          await api.post('/employees', values);
          await reload();
        }}
      />
      <DataTable
        testId="employee-table"
        rows={employees}
        columns={[
          { label: 'Code', render: (row) => row.employeeCode },
          { label: 'Naam', render: (row) => `${row.firstName} ${row.lastName}` },
          { label: 'E-mail', render: (row) => row.email },
          { label: 'Contract', render: (row) => row.contractType },
          { label: 'Uren', render: (row) => row.contractHours },
          { label: 'Tarief', render: (row) => row.hourlyRate },
          { label: 'IBAN', render: (row) => row.iban },
          { label: 'Actief', render: (row) => (row.active ? 'ja' : 'nee') }
        ]}
      />
    </section>
  );
}
