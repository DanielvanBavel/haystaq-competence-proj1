package nl.haystaq.tijdwijs.urenregistratie.domain;

public enum TimesheetStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    REJECTED;

    public boolean isEditable() {
        return this == DRAFT || this == REJECTED;
    }
}
