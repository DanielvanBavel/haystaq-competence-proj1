package nl.haystaq.tijdwijs.urenregistratie.domain;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Poort naar het context projecten. Geeft antwoord op precies twee vragen:
 * mag er op deze taak geboekt worden, en hoort deze medewerker bij het project?
 */
public interface ProjectDirectory {

    record BookableTask(
            UUID taskId,
            UUID projectId,
            String projectCode,
            String taskName,
            boolean archived,
            boolean billable) {
    }

    Optional<BookableTask> findTask(UUID taskId);

    /** Gooit een BusinessRuleViolation als er op deze datum niet geboekt mag worden. */
    void assertProjectBookableOn(UUID projectId, LocalDate date);

    boolean isMember(UUID projectId, UUID employeeId);

    boolean isLeadOnAnyProject(UUID employeeId, java.util.Collection<UUID> projectIds);
}
