package nl.haystaq.tijdwijs.urenregistratie.domain;

import nl.haystaq.tijdwijs.shared.domain.Hours;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Poort naar het context personeel. Urenregistratie kent geen Employee, alleen
 * de gegevens die het nodig heeft om zijn eigen regels te kunnen toepassen.
 */
public interface EmployeeDirectory {

    record EmployeeSnapshot(
            UUID employeeId,
            String employeeCode,
            String displayName,
            Hours contractHours,
            boolean active,
            LocalDate hireDate,
            LocalDate endDate,
            UUID managerId) {

        public boolean canBookOn(LocalDate date) {
            return active && !date.isBefore(hireDate) && (endDate == null || !date.isAfter(endDate));
        }
    }

    Optional<EmployeeSnapshot> find(UUID employeeId);
}
