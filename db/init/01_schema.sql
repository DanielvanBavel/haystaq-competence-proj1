-- TijdWijs - urenregistratie schema
-- Deliberately rich in data types and constraints: this schema is the input
-- for the test-data generation challenge.

create extension if not exists "pgcrypto";

create type contract_type    as enum ('permanent', 'temporary', 'freelance', 'intern');
create type project_status   as enum ('draft', 'active', 'on_hold', 'closed');
create type timesheet_status as enum ('draft', 'submitted', 'approved', 'rejected');
create type entry_type       as enum ('regular', 'overtime', 'travel', 'standby', 'training');
create type absence_type     as enum ('vacation', 'sick', 'parental', 'unpaid', 'special');
create type expense_category as enum ('travel', 'meals', 'hardware', 'software', 'other');
create type claim_status     as enum ('draft', 'submitted', 'approved', 'rejected', 'paid');

create table organizations (
    id          uuid primary key default gen_random_uuid(),
    name        text        not null,
    kvk_number  char(8)     not null unique,
    vat_number  varchar(14),
    country     char(2)     not null default 'NL',
    week_start  smallint    not null default 1 check (week_start in (1, 7)),
    created_at  timestamptz not null default now()
);

create table clients (
    id                uuid primary key default gen_random_uuid(),
    organization_id   uuid not null references organizations (id) on delete cascade,
    name              text not null,
    contact_email     text,
    vat_number        varchar(14),
    country           char(2)  not null default 'NL',
    payment_term_days smallint not null default 30 check (payment_term_days between 0 and 120),
    active            boolean  not null default true,
    created_at        timestamptz not null default now(),
    unique (organization_id, name)
);

create table employees (
    id              uuid primary key default gen_random_uuid(),
    organization_id uuid          not null references organizations (id) on delete cascade,
    employee_code   varchar(8)    not null unique,
    first_name      text          not null,
    last_name       text          not null,
    email           text          not null,
    birth_date      date          not null,
    hire_date       date          not null,
    end_date        date,
    contract_type   contract_type not null,
    contract_hours  numeric(4, 2) not null check (contract_hours > 0 and contract_hours <= 40),
    hourly_rate     numeric(8, 2) not null check (hourly_rate >= 0),
    iban            varchar(34)   not null,
    phone           varchar(20),
    manager_id      uuid references employees (id) on delete set null,
    active          boolean       not null default true,
    created_at      timestamptz   not null default now(),
    check (end_date is null or end_date > hire_date)
);

create unique index employees_email_uniq on employees (organization_id, lower(email));

create table projects (
    id              uuid primary key default gen_random_uuid(),
    organization_id uuid           not null references organizations (id) on delete cascade,
    client_id       uuid           not null references clients (id),
    code            varchar(12)    not null unique,
    name            text           not null,
    status          project_status not null default 'draft',
    start_date      date           not null,
    end_date        date,
    budget_hours    numeric(8, 2) check (budget_hours is null or budget_hours > 0),
    billable        boolean        not null default true,
    default_rate    numeric(8, 2) check (default_rate is null or default_rate >= 0),
    created_at      timestamptz    not null default now(),
    check (end_date is null or end_date >= start_date)
);

create table tasks (
    id            uuid primary key default gen_random_uuid(),
    project_id    uuid    not null references projects (id) on delete cascade,
    name          text    not null,
    billable      boolean not null default true,
    rate_override numeric(8, 2) check (rate_override is null or rate_override >= 0),
    archived      boolean not null default false,
    unique (project_id, name)
);

create table project_members (
    project_id  uuid not null references projects (id) on delete cascade,
    employee_id uuid not null references employees (id) on delete cascade,
    role        text not null default 'member',
    primary key (project_id, employee_id)
);

create table timesheets (
    id           uuid primary key default gen_random_uuid(),
    employee_id  uuid             not null references employees (id) on delete cascade,
    iso_year     smallint         not null check (iso_year between 2000 and 2100),
    iso_week     smallint         not null check (iso_week between 1 and 53),
    status       timesheet_status not null default 'draft',
    submitted_at timestamptz,
    approved_at  timestamptz,
    approved_by  uuid references employees (id),
    comment      text,
    created_at   timestamptz      not null default now(),
    unique (employee_id, iso_year, iso_week)
);

create table time_entries (
    id           uuid          primary key default gen_random_uuid(),
    timesheet_id uuid          not null references timesheets (id) on delete cascade,
    task_id      uuid          not null references tasks (id),
    work_date    date          not null,
    hours        numeric(4, 2) not null check (hours > 0 and hours <= 12),
    entry_type   entry_type    not null default 'regular',
    description  text,
    billable     boolean       not null default true,
    created_at   timestamptz   not null default now()
);

create index time_entries_sheet_idx on time_entries (timesheet_id);
create index time_entries_date_idx on time_entries (work_date);

create table absences (
    id            uuid          primary key default gen_random_uuid(),
    employee_id   uuid          not null references employees (id) on delete cascade,
    absence_type  absence_type  not null,
    start_date    date          not null,
    end_date      date          not null,
    hours_per_day numeric(4, 2) not null default 8.00 check (hours_per_day > 0 and hours_per_day <= 8),
    approved      boolean       not null default false,
    reason        text,
    created_at    timestamptz   not null default now(),
    check (end_date >= start_date)
);

create table expense_claims (
    id                uuid             primary key default gen_random_uuid(),
    employee_id       uuid             not null references employees (id) on delete cascade,
    project_id        uuid references projects (id),
    category          expense_category not null,
    amount            numeric(10, 2)   not null check (amount > 0),
    currency          char(3)          not null default 'EUR',
    vat_rate          numeric(4, 2)    not null default 21.00 check (vat_rate in (0, 9, 21)),
    expense_date      date             not null,
    receipt_reference varchar(20),
    description       text,
    status            claim_status     not null default 'draft',
    created_at        timestamptz      not null default now()
);

-- Reporting view used by the UI dashboard.
create view v_weekly_totals as
select ts.id            as timesheet_id,
       ts.employee_id,
       e.employee_code,
       ts.iso_year,
       ts.iso_week,
       ts.status,
       coalesce(sum(te.hours), 0)                                        as total_hours,
       coalesce(sum(te.hours) filter (where te.billable), 0)             as billable_hours,
       count(te.id)                                                      as entry_count
from timesheets ts
         join employees e on e.id = ts.employee_id
         left join time_entries te on te.timesheet_id = ts.id
group by ts.id, e.employee_code;
