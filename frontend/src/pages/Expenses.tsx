import { useCallback, useEffect, useState } from 'react';
import { api } from '../api';
import { Employee, ExpenseClaim, Project } from '../types';
import { DataTable } from '../components/DataTable';
import { EntityForm } from '../components/EntityForm';

export function Expenses() {
  const [claims, setClaims] = useState<ExpenseClaim[]>([]);
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);

  const reload = useCallback(async () => {
    const [claimList, employeeList, projectList] = await Promise.all([
      api.get<ExpenseClaim[]>('/expenses'),
      api.get<Employee[]>('/employees'),
      api.get<Project[]>('/projects')
    ]);
    setClaims(claimList);
    setEmployees(employeeList);
    setProjects(projectList);
  }, []);

  useEffect(() => {
    void reload();
  }, [reload]);

  return (
    <section>
      <h2>Declaraties</h2>
      <EntityForm
        testId="expense-form"
        fields={[
          {
            name: 'employeeId',
            label: 'Medewerker',
            type: 'select',
            options: employees.map((e) => ({ value: e.id, label: `${e.employeeCode} ${e.firstName} ${e.lastName}` }))
          },
          {
            name: 'projectId',
            label: 'Project',
            type: 'select',
            options: [{ value: '', label: '-' }, ...projects.map((p) => ({ value: p.id, label: p.code }))]
          },
          {
            name: 'category',
            label: 'Categorie',
            type: 'select',
            options: [
              { value: 'TRAVEL', label: 'Reiskosten' },
              { value: 'MEALS', label: 'Maaltijden' },
              { value: 'HARDWARE', label: 'Hardware' },
              { value: 'SOFTWARE', label: 'Software' },
              { value: 'OTHER', label: 'Overig' }
            ]
          },
          { name: 'amount', label: 'Bedrag', type: 'number', step: '0.01' },
          {
            name: 'currency',
            label: 'Valuta',
            type: 'select',
            options: [
              { value: 'EUR', label: 'EUR' },
              { value: 'USD', label: 'USD' },
              { value: 'GBP', label: 'GBP' }
            ]
          },
          {
            name: 'vatRate',
            label: 'Btw-tarief',
            type: 'select',
            options: [
              { value: '21', label: '21%' },
              { value: '9', label: '9%' },
              { value: '0', label: '0%' }
            ]
          },
          { name: 'expenseDate', label: 'Datum', type: 'date' },
          { name: 'receiptReference', label: 'Bonnummer', placeholder: 'RCP-000123' },
          { name: 'description', label: 'Omschrijving' }
        ]}
        submitLabel="Indienen"
        onSubmit={async (values) => {
          await api.post('/expenses', { ...values, vatRate: Number(values.vatRate) });
          await reload();
        }}
      />

      <DataTable
        testId="expense-table"
        rows={claims}
        columns={[
          { label: 'Medewerker', render: (row) => row.employeeCode ?? '' },
          { label: 'Categorie', render: (row) => row.category },
          { label: 'Bedrag', render: (row) => `${row.amount} ${row.currency}` },
          { label: 'Btw', render: (row) => `${row.vatRate}%` },
          { label: 'Datum', render: (row) => row.expenseDate },
          { label: 'Bon', render: (row) => row.receiptReference ?? '' },
          { label: 'Status', render: (row) => row.status },
          {
            label: '',
            render: (row) => (
              <button
                type="button"
                onClick={async () => {
                  await api.post(`/expenses/${row.id}/transitions`, { action: 'submit' });
                  await reload();
                }}
              >
                indienen
              </button>
            )
          }
        ]}
      />
    </section>
  );
}
