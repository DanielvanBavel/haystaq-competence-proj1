package nl.haystaq.tijdwijs.projecten.domain;

import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;

import java.util.Set;

public enum ProjectStatus {
    DRAFT,
    ACTIVE,
    ON_HOLD,
    CLOSED;

    public static ProjectStatus parse(String raw) {
        if (raw == null) {
            return DRAFT;
        }
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw BusinessRuleViolation.invalid("status.unknown");
        }
    }

    public Set<ProjectStatus> allowedNext() {
        return switch (this) {
            case DRAFT -> Set.of(DRAFT, ACTIVE);
            case ACTIVE -> Set.of(ACTIVE, ON_HOLD, CLOSED);
            case ON_HOLD -> Set.of(ON_HOLD, ACTIVE, CLOSED);
            case CLOSED -> Set.of(CLOSED);
        };
    }

    public boolean allowsBooking() {
        return this == ACTIVE;
    }
}
