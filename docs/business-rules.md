# Business rules TijdWijs

> Laatst bijgewerkt: 14 maart 2024 door R. Mulder (functioneel beheer)
> Status: concept - niet meer gereviewd na release 3.2

Deze pagina beschrijft de belangrijkste validatieregels. Bij twijfel geldt de
implementatie.

## Medewerkers

- Het personeelsnummer heeft de vorm `EMP-` gevolgd door vier cijfers.
- Het e-mailadres is uniek binnen de organisatie.
- Contracturen liggen tussen 0 en 40 uur.
- Het uurtarief is een bedrag in euro's met twee decimalen.
- Een medewerker is minimaal 16 jaar op de datum van indiensttreding.
- De einddatum ligt na de startdatum.
- Het rekeningnummer moet een geldig IBAN zijn.

> TODO: in release 3.0 is er iets afgesproken over maximum- en minimumtarieven
> per contractvorm, maar ik kan de mail niet meer vinden. Navragen bij finance.

## Opdrachtgevers en projecten

- De projectcode heeft de vorm `PRJ-<jaar>-<volgnummer>`.
- Een project hoort bij precies één opdrachtgever.
- De einddatum mag leeg blijven (doorlopend project).
- Alleen projecten met status `ACTIVE` mogen geboekt worden.
- Een project kan niet terug van `CLOSED` naar `ACTIVE`.

## Weekstaten

- Een medewerker heeft maximaal één weekstaat per week.
- Weeknummering volgt ISO-8601: week 1 is de week waarin 4 januari valt.
- Statussen: `DRAFT` -> `SUBMITTED` -> `APPROVED` of `REJECTED`.
- Een afgekeurde weekstaat kan opnieuw worden ingediend.
- Een weekstaat wordt goedgekeurd door de leidinggevende.

## Urenregels

- Uren worden geregistreerd in stappen van een kwartier.
- Maximaal 10 uur per regel.
- Maximaal 16 uur per dag.
- De datum valt binnen de week van de weekstaat.
- Op een weekstaat met status `SUBMITTED` of `APPROVED` kan niet meer geboekt worden.

## Verlof en verzuim

- Verlofperiodes van dezelfde medewerker mogen niet overlappen.
- Standaard 8 uur per dag.
- Ziekmeldingen kunnen met terugwerkende kracht worden ingevoerd.

## Declaraties

- Bedragen zijn positief.
- Boven de 50 euro is een bonnummer verplicht.
- Het btw-tarief is 0%, 9% of 21%.
- Declaraties ouder dan drie maanden worden niet meer vergoed.

## Bekende openstaande punten

1. De regels rond overwerk staan nergens beschreven.
2. Sinds release 3.1 controleert het systeem iets extra's bij het indienen van
   een weekstaat. De melding is `conflict`, de oorzaak is onbekend. Ticket TW-871.
3. Foutmeldingen bevatten geen veldinformatie. Staat op de backlog sinds 2023.
4. Niemand weet meer waarom een medewerker aan een project gekoppeld moet zijn
   voordat er geboekt kan worden. Het staat wel in de code.
