package nl.haystaq.tijdwijs.urenregistratie.infrastructure;

import nl.haystaq.tijdwijs.urenregistratie.domain.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimesheetSpringDataRepository extends JpaRepository<Timesheet, UUID> {

    @Query("select t from Timesheet t where t.employeeId = :employeeId "
            + "and t.week.year = :isoYear and t.week.week = :isoWeek")
    Optional<Timesheet> findByEmployeeAndWeek(UUID employeeId, int isoYear, int isoWeek);

    List<Timesheet> findByEmployeeId(UUID employeeId);

    @Query("select t from Timesheet t join t.entries e where e.id = :entryId")
    Optional<Timesheet> findByEntryId(UUID entryId);

    /**
     * Native query: het totaal per dag loopt over weekstaten heen en telt een
     * waarde-object op dat JPQL niet kan optellen.
     */
    @Query(value = "select coalesce(sum(te.hours), 0) from time_entry te "
            + "join timesheet t on t.id = te.timesheet_id "
            + "where t.employee_id = :employeeId and te.work_date = :date", nativeQuery = true)
    BigDecimal totalHoursOn(UUID employeeId, LocalDate date);
}
