package nl.haystaq.tijdwijs.urenregistratie.domain;

import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;

public enum EntryType {
    REGULAR,
    OVERTIME,
    TRAVEL,
    STANDBY,
    TRAINING;

    public static EntryType parse(String raw) {
        if (raw == null) {
            return REGULAR;
        }
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw BusinessRuleViolation.invalid("entry_type.unknown");
        }
    }
}
