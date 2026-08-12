export interface Employee {
  id: string;
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  birthDate: string;
  hireDate: string;
  endDate: string | null;
  contractType: string;
  contractHours: number;
  hourlyRate: number;
  iban: string;
  phone: string | null;
  managerId: string | null;
  active: boolean;
}

export interface Client {
  id: string;
  name: string;
  contactEmail: string | null;
  vatNumber: string | null;
  country: string;
  paymentTermDays: number;
  active: boolean;
}

export interface ProjectTask {
  id: string;
  name: string;
  billable: boolean;
  rateOverride: number | null;
  archived: boolean;
}

export interface ProjectMember {
  employeeId: string;
  role: string;
}

export interface Project {
  id: string;
  clientId: string;
  clientName: string | null;
  code: string;
  name: string;
  status: string;
  startDate: string;
  endDate: string | null;
  budgetHours: number | null;
  billable: boolean;
  defaultRate: number | null;
  tasks: ProjectTask[];
  members: ProjectMember[];
}

export interface TimeEntry {
  id: string;
  taskId: string;
  projectId: string;
  projectCode: string | null;
  taskName: string | null;
  workDate: string;
  hours: number;
  entryType: string;
  description: string | null;
  billable: boolean;
}

export interface Timesheet {
  id: string;
  employeeId: string;
  employeeCode: string | null;
  isoYear: number;
  isoWeek: number;
  weekStart: string;
  weekEnd: string;
  status: string;
  submittedAt: string | null;
  approvedAt: string | null;
  approvedBy: string | null;
  comment: string | null;
  totalHours: number;
  entries: TimeEntry[];
}

export interface Absence {
  id: string;
  employeeId: string;
  employeeCode: string | null;
  absenceType: string;
  startDate: string;
  endDate: string;
  hoursPerDay: number;
  approved: boolean;
  reason: string | null;
}

export interface ExpenseClaim {
  id: string;
  employeeId: string;
  employeeCode: string | null;
  projectId: string | null;
  category: string;
  amount: number;
  currency: string;
  vatRate: number;
  expenseDate: string;
  receiptReference: string | null;
  description: string | null;
  status: string;
}
