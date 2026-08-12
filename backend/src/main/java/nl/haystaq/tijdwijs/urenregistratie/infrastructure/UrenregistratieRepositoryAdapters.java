package nl.haystaq.tijdwijs.urenregistratie.infrastructure;

import nl.haystaq.tijdwijs.urenregistratie.domain.Absence;
import nl.haystaq.tijdwijs.urenregistratie.domain.AbsenceRepository;
import nl.haystaq.tijdwijs.urenregistratie.domain.Timesheet;
import nl.haystaq.tijdwijs.urenregistratie.domain.TimesheetRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Configuration
public class UrenregistratieRepositoryAdapters {

    @Bean
    TimesheetRepository timesheetRepository(TimesheetSpringDataRepository delegate) {
        return new TimesheetRepository() {
            @Override
            public Timesheet save(Timesheet timesheet) {
                return delegate.save(timesheet);
            }

            @Override
            public Optional<Timesheet> findById(UUID id) {
                return delegate.findById(id);
            }

            @Override
            public Optional<Timesheet> findByEntryId(UUID entryId) {
                return delegate.findByEntryId(entryId);
            }

            @Override
            public Optional<Timesheet> findByEmployeeAndWeek(UUID employeeId, int isoYear, int isoWeek) {
                return delegate.findByEmployeeAndWeek(employeeId, isoYear, isoWeek);
            }

            @Override
            public List<Timesheet> findAll() {
                return delegate.findAll();
            }

            @Override
            public List<Timesheet> findByEmployee(UUID employeeId) {
                return delegate.findByEmployeeId(employeeId);
            }

            @Override
            public BigDecimal totalHoursOn(UUID employeeId, LocalDate date) {
                BigDecimal total = delegate.totalHoursOn(employeeId, date);
                return total == null ? BigDecimal.ZERO : total;
            }
        };
    }

    @Bean
    AbsenceRepository absenceRepository(AbsenceSpringDataRepository delegate) {
        return new AbsenceRepository() {
            @Override
            public Absence save(Absence absence) {
                return delegate.save(absence);
            }

            @Override
            public Optional<Absence> findById(UUID id) {
                return delegate.findById(id);
            }

            @Override
            public List<Absence> findAll() {
                return delegate.findAllByOrderByStartDateDesc();
            }

            @Override
            public List<Absence> findByEmployee(UUID employeeId) {
                return delegate.findByEmployeeIdOrderByStartDateDesc(employeeId);
            }

            @Override
            public void delete(Absence absence) {
                delegate.delete(absence);
            }
        };
    }
}
