package nl.haystaq.tijdwijs.urenregistratie.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimesheetRepository {

    Timesheet save(Timesheet timesheet);

    Optional<Timesheet> findById(UUID id);

    Optional<Timesheet> findByEntryId(UUID entryId);

    Optional<Timesheet> findByEmployeeAndWeek(UUID employeeId, int isoYear, int isoWeek);

    List<Timesheet> findAll();

    List<Timesheet> findByEmployee(UUID employeeId);

    /** Alle uren van een medewerker op een datum, over weekstaten heen. */
    BigDecimal totalHoursOn(UUID employeeId, LocalDate date);
}
