# API-voorbeelden

Basis: `http://localhost:8081`. Geen authenticatie. Alle velden camelCase.

## Foutmodel

| Status | Body | Betekenis |
| --- | --- | --- |
| 400 | `{"error":"invalid input"}` | Waarde afgekeurd. Welk veld staat er niet bij. |
| 404 | `{"error":"not found"}` | Onbekende resource. |
| 409 | `{"error":"conflict"}` | Toestand of business rule verhindert de actie. |
| 500 | `{"error":"internal error","ref":"a1b2c3d4"}` | Onverwacht. Zoek de ref terug in de logs. |

```bash
docker compose logs backend | grep a1b2c3d4
```

## Medewerker aanmaken

```bash
curl -sS -X POST http://localhost:8081/api/employees \
  -H 'content-type: application/json' \
  -d '{
    "employeeCode": "EMP-0101",
    "firstName": "Nadia",
    "lastName": "El Amrani",
    "email": "nadia.elamrani@haystaq.example",
    "birthDate": "1991-06-22",
    "hireDate": "2024-01-08",
    "contractType": "PERMANENT",
    "contractHours": 36,
    "hourlyRate": 92.50,
    "iban": "NL86INGB0002445588",
    "phone": "+31611122233",
    "active": true
  }'
```

Contractvormen: `PERMANENT`, `TEMPORARY`, `FREELANCE`, `INTERN`.

## Project met taak en teamlid

```bash
curl -sS -X POST http://localhost:8081/api/projects \
  -H 'content-type: application/json' \
  -d '{
    "clientId": "22222222-2222-2222-2222-222222222221",
    "code": "PRJ-2026-010",
    "name": "Datamigratie",
    "status": "ACTIVE",
    "startDate": "2026-01-05",
    "billable": true,
    "defaultRate": 110
  }'
```

```bash
curl -sS -X POST http://localhost:8081/api/projects/<projectId>/tasks \
  -H 'content-type: application/json' \
  -d '{"name":"Realisatie","billable":true}'
```

```bash
curl -sS -X POST http://localhost:8081/api/projects/<projectId>/members \
  -H 'content-type: application/json' \
  -d '{"employeeId":"<employeeId>","role":"MEMBER"}'
```

Rollen: `MEMBER`, `LEAD`. Statussen: `DRAFT`, `ACTIVE`, `ON_HOLD`, `CLOSED`.

## Weekstaat vullen, indienen en goedkeuren

```bash
curl -sS -X POST http://localhost:8081/api/timesheets \
  -H 'content-type: application/json' \
  -d '{"employeeId":"<employeeId>","isoYear":2026,"isoWeek":10}'
```

Het antwoord bevat `weekStart` en `weekEnd`; daarbinnen moeten je urenregels vallen.

```bash
curl -sS -X POST http://localhost:8081/api/timesheets/<timesheetId>/entries \
  -H 'content-type: application/json' \
  -d '{
    "taskId": "<taskId>",
    "workDate": "2026-03-02",
    "hours": 8,
    "entryType": "REGULAR",
    "description": "Realisatie sprint 4"
  }'
```

Soorten: `REGULAR`, `OVERTIME`, `TRAVEL`, `STANDBY`, `TRAINING`.

```bash
curl -sS -X POST http://localhost:8081/api/timesheets/<timesheetId>/submit \
  -H 'content-type: application/json' -d '{"comment":"week compleet"}'
```

```bash
curl -sS -X POST http://localhost:8081/api/timesheets/<timesheetId>/approve \
  -H 'content-type: application/json' -d '{"approvedBy":"<employeeId>"}'
```

## Verlof en declaraties

```bash
curl -sS -X POST http://localhost:8081/api/absences \
  -H 'content-type: application/json' \
  -d '{
    "employeeId": "<employeeId>",
    "absenceType": "VACATION",
    "startDate": "2026-07-06",
    "endDate": "2026-07-17",
    "hoursPerDay": 8,
    "approved": true
  }'
```

```bash
curl -sS -X POST http://localhost:8081/api/expenses \
  -H 'content-type: application/json' \
  -d '{
    "employeeId": "<employeeId>",
    "category": "MEALS",
    "amount": 42.50,
    "currency": "EUR",
    "vatRate": 9,
    "expenseDate": "2026-08-01",
    "receiptReference": "RCP-000123",
    "description": "Teamlunch"
  }'
```

## Database rechtstreeks

```bash
docker compose exec db psql -U tijdwijs -d tijdwijs
```

Kolommen en types van een tabel:

```sql
select column_name, data_type, character_maximum_length, numeric_precision,
       numeric_scale, is_nullable, column_default
from information_schema.columns
where table_name = 'time_entry'
order by ordinal_position;
```

Alle check-, unique- en foreign key-constraints:

```sql
select conrelid::regclass as tabel, conname, pg_get_constraintdef(oid)
from pg_constraint
where connamespace = 'public'::regnamespace
order by 1, 2;
```

Let op: de check-constraints in de database zijn een *deelverzameling* van de
regels. De rest zit in de Java-domeinlaag.
