-- TijdWijs - urenregistratie
-- Elk bounded context heeft zijn eigen tabellen. Er zijn bewust geen foreign keys
-- tussen contexten heen: de koppeling loopt via de applicatielaag (ports).

create extension if not exists "pgcrypto";

-- ---------------------------------------------------------------------------
-- context: personeel
-- ---------------------------------------------------------------------------

create table employee (
    id              uuid primary key,
    employee_code   varchar(8)    not null unique,
    first_name      varchar(60)   not null,
    last_name       varchar(60)   not null,
    email           varchar(160)  not null,
    birth_date      date          not null,
    hire_date       date          not null,
    end_date        date,
    contract_type   varchar(20)   not null check (contract_type in ('PERMANENT', 'TEMPORARY', 'FREELANCE', 'INTERN')),
    contract_hours  numeric(4, 2) not null check (contract_hours > 0 and contract_hours <= 40),
    hourly_rate     numeric(8, 2) not null check (hourly_rate >= 0),
    iban            varchar(34)   not null,
    phone           varchar(20),
    manager_id      uuid references employee (id),
    active          boolean       not null default true,
    created_at      timestamptz   not null default now(),
    version         bigint        not null default 0,
    constraint employee_period check (end_date is null or end_date > hire_date)
);

create unique index employee_email_uniq on employee (lower(email));

-- ---------------------------------------------------------------------------
-- context: projecten
-- ---------------------------------------------------------------------------

create table client (
    id                uuid primary key,
    name              varchar(120) not null unique,
    contact_email     varchar(160),
    vat_number        varchar(14),
    country           varchar(2)   not null default 'NL',
    payment_term_days smallint     not null default 30 check (payment_term_days between 0 and 120),
    active            boolean      not null default true,
    created_at        timestamptz  not null default now(),
    version           bigint       not null default 0
);

create table project (
    id           uuid primary key,
    client_id    uuid          not null references client (id),
    code         varchar(12)   not null unique,
    name         varchar(120)  not null,
    status       varchar(10)   not null check (status in ('DRAFT', 'ACTIVE', 'ON_HOLD', 'CLOSED')),
    start_date   date          not null,
    end_date     date,
    budget_hours numeric(8, 2) check (budget_hours is null or budget_hours > 0),
    billable     boolean       not null default true,
    default_rate numeric(8, 2) check (default_rate is null or default_rate >= 0),
    created_at   timestamptz   not null default now(),
    version      bigint        not null default 0,
    constraint project_period check (end_date is null or end_date >= start_date)
);

create table project_task (
    id            uuid primary key,
    project_id    uuid         not null references project (id) on delete cascade,
    name          varchar(80)  not null,
    billable      boolean      not null default true,
    rate_override numeric(8, 2) check (rate_override is null or rate_override >= 0),
    archived      boolean      not null default false,
    unique (project_id, name)
);

create table project_member (
    project_id  uuid        not null references project (id) on delete cascade,
    employee_id uuid        not null,
    role        varchar(10) not null default 'MEMBER' check (role in ('MEMBER', 'LEAD')),
    primary key (project_id, employee_id)
);

-- ---------------------------------------------------------------------------
-- context: urenregistratie
-- ---------------------------------------------------------------------------

create table timesheet (
    id           uuid primary key,
    employee_id  uuid        not null,
    iso_year     smallint    not null check (iso_year between 2000 and 2100),
    iso_week     smallint    not null check (iso_week between 1 and 53),
    status       varchar(10) not null check (status in ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED')),
    submitted_at timestamptz,
    approved_at  timestamptz,
    approved_by  uuid,
    comment      varchar(500),
    created_at   timestamptz not null default now(),
    version      bigint      not null default 0,
    unique (employee_id, iso_year, iso_week)
);

create table time_entry (
    id           uuid          primary key,
    timesheet_id uuid          not null references timesheet (id) on delete cascade,
    task_id      uuid          not null,
    project_id   uuid          not null,
    work_date    date          not null,
    hours        numeric(4, 2) not null check (hours > 0 and hours <= 12),
    entry_type   varchar(10)   not null check (entry_type in ('REGULAR', 'OVERTIME', 'TRAVEL', 'STANDBY', 'TRAINING')),
    description  varchar(500),
    billable     boolean       not null default true,
    created_at   timestamptz   not null default now()
);

create index time_entry_sheet_idx on time_entry (timesheet_id);
create index time_entry_date_idx on time_entry (work_date);

create table absence (
    id            uuid          primary key,
    employee_id   uuid          not null,
    absence_type  varchar(12)   not null check (absence_type in ('VACATION', 'SICK', 'PARENTAL', 'UNPAID', 'SPECIAL')),
    start_date    date          not null,
    end_date      date          not null,
    hours_per_day numeric(4, 2) not null default 8.00 check (hours_per_day > 0 and hours_per_day <= 8),
    approved      boolean       not null default false,
    reason        varchar(200),
    created_at    timestamptz   not null default now(),
    version       bigint        not null default 0,
    constraint absence_period check (end_date >= start_date)
);

-- ---------------------------------------------------------------------------
-- context: declaraties
-- ---------------------------------------------------------------------------

create table expense_claim (
    id                uuid           primary key,
    employee_id       uuid           not null,
    project_id        uuid,
    category          varchar(10)    not null check (category in ('TRAVEL', 'MEALS', 'HARDWARE', 'SOFTWARE', 'OTHER')),
    amount            numeric(10, 2) not null check (amount > 0),
    currency          varchar(3)     not null default 'EUR',
    vat_rate          numeric(4, 2)  not null default 21.00 check (vat_rate in (0, 9, 21)),
    expense_date      date           not null,
    receipt_reference varchar(20),
    description       varchar(500),
    status            varchar(10)    not null check (status in ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'PAID')),
    created_at        timestamptz    not null default now(),
    version           bigint         not null default 0
);

-- ---------------------------------------------------------------------------
-- read model voor rapportage
-- ---------------------------------------------------------------------------

create view v_weekly_totals as
select ts.id                                            as timesheet_id,
       ts.employee_id,
       e.employee_code,
       ts.iso_year,
       ts.iso_week,
       ts.status,
       coalesce(sum(te.hours), 0)                       as total_hours,
       coalesce(sum(te.hours) filter (where te.billable), 0) as billable_hours,
       count(te.id)                                     as entry_count
from timesheet ts
         join employee e on e.id = ts.employee_id
         left join time_entry te on te.timesheet_id = ts.id
group by ts.id, e.employee_code;
