package nl.haystaq.tijdwijs.urenregistratie.application;

import nl.haystaq.tijdwijs.urenregistratie.domain.Absence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AbsenceView(
        UUID id,
        UUID employeeId,
        String employeeCode,
        String absenceType,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal hoursPerDay,
        boolean approved,
        String reason) {

    public static AbsenceView from(Absence absence, String employeeCode) {
        return new AbsenceView(
                absence.id(),
                absence.employeeId(),
                employeeCode,
                absence.absenceType().name(),
                absence.startDate(),
                absence.endDate(),
                absence.hoursPerDay(),
                absence.isApproved(),
                absence.reason());
    }
}
