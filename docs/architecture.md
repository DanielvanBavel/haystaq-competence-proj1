# Architectuur

TijdWijs is een modulaire monoliet volgens Domain Driven Design. Een monoliet,
omdat het domein klein genoeg is om in een transactie te passen; modulair, omdat
de contexten elkaar alleen via expliciete poorten mogen kennen.

## Bounded contexts

```
nl.haystaq.tijdwijs
├── shared            gedeelde waarde-objecten (Money, Hours, IsoWeek) en de domeinfout
├── personeel         medewerkers en dienstverbanden
├── projecten         opdrachtgevers, projecten, taken en teamsamenstelling
├── urenregistratie   weekstaten, urenregels, verlof en verzuim
├── declaraties       onkostendeclaraties
└── rapportage        leesmodel voor dashboards (gaat rechtstreeks naar de database)
```

Elk context heeft dezelfde indeling:

| Laag | Verantwoordelijkheid |
| --- | --- |
| `domain` | Aggregates, entiteiten, waarde-objecten, domeinregels en repository-poorten. Kent geen Spring en geen HTTP. |
| `application` | Use cases, commando's en leesmodellen. Coordineert aggregates en poorten binnen een transactie. |
| `infrastructure` | Spring Data JPA-repositories en de adapters die ze achter de domeinpoorten verbergen. |
| `api` | REST-controllers. Vertalen HTTP naar commando's en terug. |

## Aggregates

| Context | Aggregate root | Bevat | Belangrijkste invarianten |
| --- | --- | --- | --- |
| personeel | `Employee` | `EmploymentPeriod` | leeftijd bij indiensttreding, IBAN mod-97, tarief per contractvorm |
| projecten | `Client` | - | naam uniek, btw-nummer en land |
| projecten | `Project` | `ProjectTask`, `ProjectMember` | code hoort bij het startjaar, statusovergangen, boekbaarheid |
| urenregistratie | `Timesheet` | `TimeEntry` | datum binnen de ISO-week, dagmaximum, indienen alleen bij volledige week |
| urenregistratie | `Absence` | - | geen overlap, maximale duur, terugwerkende ziekmelding |
| declaraties | `ExpenseClaim` | - | bonnummer boven een drempel, btw per categorie, ouderdom |

Een aggregate bewaakt alles wat het zelf kan zien. Regels die kennis van een
ander aggregate nodig hebben, staan in de applicatielaag.

## Poorten tussen contexten

Contexten praten nooit rechtstreeks met elkaars repositories. De consument
definieert een poort, de leverancier levert de adapter:

| Poort (consument) | Adapter (leverancier) | Waarvoor |
| --- | --- | --- |
| `urenregistratie.domain.EmployeeDirectory` | `personeel.application.EmployeeDirectoryAdapter` | contracturen, dienstverband, leidinggevende |
| `urenregistratie.domain.ProjectDirectory` | `projecten.application.ProjectDirectoryAdapter` | is deze taak boekbaar, hoort deze medewerker bij het project |
| `declaraties.domain.EmployeeLookup` | `personeel.application.EmployeeLookupAdapter` | bestaat deze medewerker |

De snapshots die deze poorten teruggeven bevatten alleen wat de consument nodig
heeft. `Employee` zelf verlaat het personeelscontext nooit.

## Database

Eén PostgreSQL-database, per context een eigen groep tabellen. Er staan bewust
**geen foreign keys tussen contexten** (`timesheet.employee_id` verwijst niet naar
`employee`): die relatie wordt door de applicatielaag bewaakt. Dat is realistisch
voor een systeem dat later opgeknipt kan worden - en het betekent dat een
testdata-generator die alleen op foreign keys let, de helft van de samenhang mist.

Migraties staan in `backend/src/main/resources/db/migration` en draaien met
Flyway bij het opstarten. `V1__schema.sql` bevat het schema, `V2__seed.sql` de
minimale seed.

## Foutafhandeling

Het domein gooit één type fout: `BusinessRuleViolation`, met een `kind`
(`INVALID_INPUT`, `CONFLICT`, `NOT_FOUND`) en een `code` zoals `iban.mod97`.
`RestExceptionHandler` vertaalt dat naar HTTP:

| kind | HTTP | body |
| --- | --- | --- |
| INVALID_INPUT | 400 | `{"error":"invalid input"}` |
| CONFLICT | 409 | `{"error":"conflict"}` |
| NOT_FOUND | 404 | `{"error":"not found"}` |
| onverwacht | 500 | `{"error":"internal error","ref":"..."}` |

De `code` gaat nooit mee in het antwoord. Dat is een bewuste eigenschap van deze
applicatie: het is het probleem waar de opdracht over gaat. Met `DEBUG_RULES=true`
komt de code wel in de logs.
