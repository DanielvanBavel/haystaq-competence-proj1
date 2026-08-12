import { useCallback, useEffect, useState } from 'react';
import { api } from '../api';
import { Client, Employee, Project } from '../types';
import { DataTable } from '../components/DataTable';
import { EntityForm, FieldDefinition } from '../components/EntityForm';

export function Projects() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [employees, setEmployees] = useState<Employee[]>([]);

  const reload = useCallback(async () => {
    const [projectList, clientList, employeeList] = await Promise.all([
      api.get<Project[]>('/projects'),
      api.get<Client[]>('/clients'),
      api.get<Employee[]>('/employees')
    ]);
    setProjects(projectList);
    setClients(clientList);
    setEmployees(employeeList);
  }, []);

  useEffect(() => {
    void reload();
  }, [reload]);

  const clientFields: FieldDefinition[] = [
    { name: 'name', label: 'Naam' },
    { name: 'contactEmail', label: 'Contact e-mail', type: 'email' },
    { name: 'vatNumber', label: 'Btw-nummer', placeholder: 'NL001234567B01' },
    { name: 'country', label: 'Land', defaultValue: 'NL' },
    { name: 'paymentTermDays', label: 'Betaaltermijn (dagen)', type: 'number', defaultValue: 30 }
  ];

  const projectFields: FieldDefinition[] = [
    { name: 'code', label: 'Projectcode', placeholder: 'PRJ-2026-003' },
    { name: 'name', label: 'Naam' },
    {
      name: 'clientId',
      label: 'Opdrachtgever',
      type: 'select',
      options: clients.map((client) => ({ value: client.id, label: client.name }))
    },
    {
      name: 'status',
      label: 'Status',
      type: 'select',
      options: [
        { value: 'DRAFT', label: 'Concept' },
        { value: 'ACTIVE', label: 'Actief' },
        { value: 'ON_HOLD', label: 'On hold' },
        { value: 'CLOSED', label: 'Afgesloten' }
      ]
    },
    { name: 'startDate', label: 'Startdatum', type: 'date' },
    { name: 'endDate', label: 'Einddatum', type: 'date' },
    { name: 'budgetHours', label: 'Budget (uren)', type: 'number', step: '0.25' },
    { name: 'defaultRate', label: 'Standaardtarief', type: 'number', step: '0.01' },
    { name: 'billable', label: 'Declarabel', type: 'checkbox', defaultValue: true }
  ];

  const projectOptions = projects.map((project) => ({
    value: project.id,
    label: `${project.code} ${project.name}`
  }));

  return (
    <section>
      <h2>Projecten</h2>

      <EntityForm
        title="Opdrachtgever toevoegen"
        testId="client-form"
        fields={clientFields}
        submitLabel="Opslaan"
        onSubmit={async (values) => {
          await api.post('/clients', values);
          await reload();
        }}
      />

      <EntityForm
        title="Project starten"
        testId="project-form"
        fields={projectFields}
        submitLabel="Opslaan"
        onSubmit={async (values) => {
          await api.post('/projects', values);
          await reload();
        }}
      />

      <EntityForm
        title="Taak toevoegen"
        testId="task-form"
        fields={[
          { name: 'projectId', label: 'Project', type: 'select', options: projectOptions },
          { name: 'name', label: 'Taaknaam' },
          { name: 'rateOverride', label: 'Afwijkend tarief', type: 'number', step: '0.01' },
          { name: 'billable', label: 'Declarabel', type: 'checkbox', defaultValue: true }
        ]}
        submitLabel="Toevoegen"
        onSubmit={async (values) => {
          await api.post(`/projects/${values.projectId}/tasks`, values);
          await reload();
        }}
      />

      <EntityForm
        title="Medewerker koppelen"
        testId="member-form"
        fields={[
          { name: 'projectId', label: 'Project', type: 'select', options: projectOptions },
          {
            name: 'employeeId',
            label: 'Medewerker',
            type: 'select',
            options: employees.map((e) => ({ value: e.id, label: `${e.employeeCode} ${e.firstName} ${e.lastName}` }))
          },
          {
            name: 'role',
            label: 'Rol',
            type: 'select',
            options: [
              { value: 'MEMBER', label: 'Teamlid' },
              { value: 'LEAD', label: 'Projectleider' }
            ]
          }
        ]}
        submitLabel="Koppelen"
        onSubmit={async (values) => {
          await api.post(`/projects/${values.projectId}/members`, values);
          await reload();
        }}
      />

      <DataTable
        testId="project-table"
        rows={projects}
        columns={[
          { label: 'Code', render: (row) => row.code },
          { label: 'Naam', render: (row) => row.name },
          { label: 'Opdrachtgever', render: (row) => row.clientName ?? '' },
          { label: 'Status', render: (row) => row.status },
          { label: 'Start', render: (row) => row.startDate },
          { label: 'Eind', render: (row) => row.endDate ?? '' },
          { label: 'Budget', render: (row) => row.budgetHours ?? '' },
          { label: 'Taken', render: (row) => row.tasks.map((task) => task.name).join(', ') },
          { label: 'Team', render: (row) => row.members.length }
        ]}
      />
    </section>
  );
}
