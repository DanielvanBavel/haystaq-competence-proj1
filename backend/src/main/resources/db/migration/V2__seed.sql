-- Bewust minimale seed: net genoeg om de applicatie te kunnen starten.
-- Alles wat je nodig hebt om echt te testen moet gegenereerd worden.

insert into employee (id, employee_code, first_name, last_name, email, birth_date, hire_date,
                      contract_type, contract_hours, hourly_rate, iban, phone, active)
values ('33333333-3333-3333-3333-333333333331', 'EMP-0001', 'Sanne', 'de Wit',
        'sanne.dewit@haystaq.example', '1988-04-12', '2019-03-01', 'PERMANENT', 40.00, 95.00,
        'NL91ABNA0417164300', '+31612345678', true),
       ('33333333-3333-3333-3333-333333333332', 'EMP-0002', 'Joost', 'Bakker',
        'joost.bakker@haystaq.example', '1994-11-30', '2022-09-15', 'TEMPORARY', 32.00, 78.50,
        'NL44RABO0123456789', '+31687654321', true);

update employee set manager_id = '33333333-3333-3333-3333-333333333331' where employee_code = 'EMP-0002';

insert into client (id, name, contact_email, vat_number, payment_term_days)
values ('22222222-2222-2222-2222-222222222221', 'Gemeente Zandvliet', 'inkoop@zandvliet.example',
        'NL001234567B01', 30),
       ('22222222-2222-2222-2222-222222222222', 'Vervoerbedrijf Noord', 'finance@vbnoord.example',
        'NL009876543B01', 60);

insert into project (id, client_id, code, name, status, start_date, end_date, budget_hours, billable, default_rate)
values ('44444444-4444-4444-4444-444444444441', '22222222-2222-2222-2222-222222222221',
        'PRJ-2026-001', 'Migratie zaaksysteem', 'ACTIVE', '2026-01-05', '2026-12-31', 1800.00, true, 105.00),
       ('44444444-4444-4444-4444-444444444442', '22222222-2222-2222-2222-222222222222',
        'PRJ-2026-002', 'Reisinformatie API', 'ON_HOLD', '2026-02-01', null, 640.00, true, 98.00);

insert into project_task (id, project_id, name, billable, rate_override)
values ('55555555-5555-5555-5555-555555555551', '44444444-4444-4444-4444-444444444441', 'Analyse', true, null),
       ('55555555-5555-5555-5555-555555555552', '44444444-4444-4444-4444-444444444441', 'Realisatie', true, 115.00),
       ('55555555-5555-5555-5555-555555555553', '44444444-4444-4444-4444-444444444441', 'Intern overleg', false, null),
       ('55555555-5555-5555-5555-555555555554', '44444444-4444-4444-4444-444444444442', 'Realisatie', true, null);

insert into project_member (project_id, employee_id, role)
values ('44444444-4444-4444-4444-444444444441', '33333333-3333-3333-3333-333333333331', 'LEAD'),
       ('44444444-4444-4444-4444-444444444441', '33333333-3333-3333-3333-333333333332', 'MEMBER');

insert into timesheet (id, employee_id, iso_year, iso_week, status)
values ('66666666-6666-6666-6666-666666666661', '33333333-3333-3333-3333-333333333331', 2026, 6, 'DRAFT');

insert into time_entry (id, timesheet_id, task_id, project_id, work_date, hours, entry_type, description, billable)
values ('77777777-7777-7777-7777-777777777771', '66666666-6666-6666-6666-666666666661',
        '55555555-5555-5555-5555-555555555551', '44444444-4444-4444-4444-444444444441',
        '2026-02-02', 6.50, 'REGULAR', 'Interviews met key users', true),
       ('77777777-7777-7777-7777-777777777772', '66666666-6666-6666-6666-666666666661',
        '55555555-5555-5555-5555-555555555553', '44444444-4444-4444-4444-444444444441',
        '2026-02-02', 1.00, 'REGULAR', 'Weekstart', false);
