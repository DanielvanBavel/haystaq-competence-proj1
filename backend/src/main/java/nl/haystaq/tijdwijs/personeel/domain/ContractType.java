package nl.haystaq.tijdwijs.personeel.domain;

import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;
import nl.haystaq.tijdwijs.shared.domain.Money;

import java.math.BigDecimal;

public enum ContractType {
    PERMANENT,
    TEMPORARY,
    FREELANCE,
    INTERN;

    private static final Money INTERN_MAX = Money.of(new BigDecimal("45.00"));
    private static final Money FREELANCE_MIN = Money.of(new BigDecimal("60.00"));

    public static ContractType parse(String raw) {
        BusinessRuleViolation.require(raw != null, "contract_type.missing");
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw BusinessRuleViolation.invalid("contract_type.unknown");
        }
    }

    /** Tariefafspraken per contractvorm. Staan niet in de functionele documentatie. */
    public void validateRate(Money hourlyRate) {
        if (this == INTERN) {
            BusinessRuleViolation.require(!hourlyRate.isGreaterThan(INTERN_MAX), "intern.rate");
        }
        if (this == FREELANCE) {
            BusinessRuleViolation.require(!FREELANCE_MIN.isGreaterThan(hourlyRate), "freelance.rate");
        }
    }
}
