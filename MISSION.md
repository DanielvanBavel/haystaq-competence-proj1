# Opdracht 1 - Testdata die zichzelf maakt

**Probleem uit de praktijk:** *"Handmatig testdata maken kost veel tijd."*
Veel formulieren, veel datatypes, moeilijk herbruikbare data, weinig
afhankelijkheden om op te leunen en foutmeldingen die je niets vertellen.

**AI-richting:** een agent die op basis van het databaseschema, de domeincode en
de business rules synthetische testdata genereert, inclusief randgevallen, en die
data er ook daadwerkelijk in krijgt.

---

## 1. De situatie

Je bent net op het TijdWijs-project gestart (zie [README.md](README.md)). Er is
een urenregistratiesysteem met een database vol constraints, een Java-domeinlaag
vol regels en een testomgeving met twee medewerkers en één halfvolle weekstaat.

Wat het team nodig heeft om te kunnen testen:

- een realistisch bedrijf: ~25 medewerkers, 8 projecten, drie maanden aan uren;
- randgevallen: een medewerker die halverwege de week uit dienst gaat, een week
  die precies op contracturen uitkomt, een declaratie van exact 25,00 euro;
- data die op precies één punt ongeldig is, om negatieve tests mee te doen;
- morgen dezelfde dataset, in een schone omgeving.

Handmatig via de UI ben je hier een dag mee bezig. En als je iets fout doet, zegt
de API alleen `{"error":"invalid input"}`.

## 2. Wat je bouwt

Een **AI-gedreven testdata-toolkit**. De kern is een MCP-server waarmee een agent
met deze applicatie kan samenwerken. Minimale scope:

| Component | Wat het moet doen |
| --- | --- |
| **Schema-introspectie** | Tabellen, kolommen, types, checks, unique indexes en defaults rechtstreeks uit Postgres lezen (`information_schema`, `pg_catalog`). Niet hardcoden. |
| **Regel-ontdekking** | De regels achterhalen die *niet* in het schema zitten. Bronnen: de domeinlaag (`backend/src/main/java/**/domain`), de onvolledige `docs/business-rules.md`, en het gedrag van de API zelf. |
| **Generator** | Een samenhangende dataset genereren: medewerkers met geldige IBAN's, projecten met kloppende codes, weekstaten met urenregels binnen de juiste ISO-week. |
| **Loader** | De dataset in de applicatie zetten via de API (en/of direct SQL) en rapporteren wat is afgewezen. |
| **Verificatie** | Achteraf controleren dat de data er echt is en klopt: aantallen, totalen, statussen. |

Voorstel voor MCP-tools:

```
describe_schema(table?)          -> kolommen, types, constraints
list_business_rules()            -> wat de agent tot nu toe heeft ontdekt
generate_dataset(profile, seed)  -> dataset als JSON, deterministisch via seed
load_dataset(dataset)            -> laadt via de API, per record ok of afgewezen
verify_dataset(expectations)     -> query's tegen de database
reset_environment()              -> POST /api/admin/reset
```

Een `profile` is bijvoorbeeld `klein-bureau`, `jaarafsluiting` of
`edge-cases-declaraties`. Maak er iets van dat een tester zonder kennis van de
generator kan gebruiken.

## 3. Aanpak in fases

Reken op ongeveer vier uur. Timebox streng: liever drie fases af dan vijf half.

**Fase 0 - Verkennen (30 min)**
Start de stack. Klik door de UI. Maak met de hand één medewerker aan, en probeer
er één aan te maken die wordt afgewezen. Noteer wat je *niet* uit de foutmelding
kunt afleiden. Dat is je nulmeting.

**Fase 1 - De agent laten begrijpen (45 min)**
Laat de agent het schema introspecteren en de regels uit de domeincode afleiden.
Lever een `docs/discovered-rules.md` op die de agent zelf heeft geschreven.
Vergelijk met `docs/business-rules.md`: wat ontbrak daar, en wat klopte niet meer?

**Fase 2 - MCP-server (60 min)**
Bouw de server met minimaal `describe_schema`, `generate_dataset` en
`load_dataset`. Draai hem lokaal en koppel hem aan je agent. Genereer vijf
medewerkers en laad ze. Alles wat wordt afgewezen is input voor fase 3.

**Fase 3 - Feedbackloop (45 min)**
Laat de agent zichzelf corrigeren: afgewezen record -> hypothese over de regel ->
regel toevoegen aan de generator -> opnieuw proberen. Dit is het interessantste
deel: de agent leert de ongedocumenteerde regels kennen door te falen. Schaal
daarna op naar 25 medewerkers en drie maanden uren.

**Fase 4 - Randgevallen en herbruikbaarheid (45 min)**
Voeg bewust randgevallen toe (zie hieronder). Verpak je werk in een skill of
slash-command, zodat een collega het morgen kan gebruiken zonder jullie prompts
te kennen.

**Fase 5 - Demo (15 min)**
Schone database, één commando, dataset staat erin. Laat zien wat de agent zelf
heeft ontdekt.

## 4. Randgevallen om te halen

- [ ] Urenregel van 0,25 uur en van de maximale waarde (welke is dat?).
- [ ] Dag met precies het dagmaximum, verdeeld over meerdere weekstaten.
- [ ] Week die exact op contracturen uitkomt, en één die er net onder zit.
- [ ] Medewerker met een einddatum midden in een geboekte week.
- [ ] Goedgekeurd verlof dat een volledige dag blokkeert, met een urenboeking op
      diezelfde dag.
- [ ] Declaratie van exact 25,00 euro en van 25,01 euro.
- [ ] Project waarvan de code niet bij de startdatum past.
- [ ] Stagiair met een tarief boven de limiet.
- [ ] IBAN die er goed uitziet maar de mod-97-controle niet haalt.
- [ ] Overwerk boeken op een week die nog niet vol is.

Geef bij elk randgeval aan of het een *geldig* geval is (de applicatie moet het
accepteren) of een *negatief* geval (de applicatie moet het afwijzen). Laat je
toolkit dat onderscheid expliciet maken.

## 5. Definition of done

- [ ] `docker compose up -d --build` en daarna één commando geeft een gevulde omgeving.
- [ ] De generator gebruikt het echte schema; een kolom toevoegen breekt hem niet stilletjes.
- [ ] Dezelfde seed geeft dezelfde dataset.
- [ ] De ontdekte business rules staan in de repo, geschreven door de agent.
- [ ] Het laadresultaat is leesbaar: X geaccepteerd, Y afgewezen, met reden per record.
- [ ] Een collega kan het draaien met alleen de README.
- [ ] Alles staat in deze repo, in een branch met een pull request.

## 6. Stretch goals

- Laat de agent de nietszeggende foutmeldingen vertalen: probe de API met
  systematische varianten en bouw een lookup van "afgewezen input" naar
  "waarschijnlijk overtreden regel".
- Genereer naast data een **datacontract**: een JSON-schema per entiteit dat je in
  andere tests kunt hergebruiken.
- Laat de generator een tijdreeks maken die statistisch klopt: vakantiepieken in
  juli, minder uren in week 52.
- Maak een subagent die na elke wijziging in `db/migration` of in de domeinlaag
  controleert of de generator nog voldoet.
- Genereer data rechtstreeks tegen de DDD-structuur: één profiel per bounded
  context, zodat je ook los kunt testen.

## 7. Valkuilen

- **Alles in de prompt proppen.** Het schema is groot. Laat de agent het ophalen,
  niet onthouden.
- **Alleen SQL inserten.** Dan sla je de hele domeinlaag over en heb je data die
  in de UI niet werkt. Gebruik minimaal één keer de API-route.
- **De ISO-week vergeten.** Een urenregel moet binnen de week van de weekstaat
  vallen. Hier gaat het bijna altijd mis.
- **Niet resetten tussen runs.** `POST /api/admin/reset` is je vriend.
- **Alleen de happy path.** Zonder randgevallen is de dataset niet interessant.

## 8. Wat je oplevert

1. Werkende code in deze repo: MCP-server, generator, skill of slash-command.
2. `docs/discovered-rules.md`, geschreven door de agent.
3. Een demo van maximaal 10 minuten.
4. Eén alinea: wat kostte dit handmatig, wat kost het nu, en wat zou je niet nog
   een keer zo doen?
