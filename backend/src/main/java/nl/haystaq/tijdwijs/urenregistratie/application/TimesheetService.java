package nl.haystaq.tijdwijs.urenregistratie.application;

import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;
import nl.haystaq.tijdwijs.shared.domain.Hours;
import nl.haystaq.tijdwijs.shared.domain.IsoWeek;
import nl.haystaq.tijdwijs.urenregistratie.domain.Absence;
import nl.haystaq.tijdwijs.urenregistratie.domain.AbsenceRepository;
import nl.haystaq.tijdwijs.urenregistratie.domain.EmployeeDirectory;
import nl.haystaq.tijdwijs.urenregistratie.domain.EntryType;
import nl.haystaq.tijdwijs.urenregistratie.domain.ProjectDirectory;
import nl.haystaq.tijdwijs.urenregistratie.domain.TimeEntry;
import nl.haystaq.tijdwijs.urenregistratie.domain.Timesheet;
import nl.haystaq.tijdwijs.urenregistratie.domain.TimesheetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Coordineert de weekstaat met de andere contexten. Regels die binnen de
 * weekstaat passen staan in het aggregate zelf; alles wat kennis van
 * medewerkers, projecten of verlof nodig heeft, staat hier.
 */
@Service
@Transactional
public class TimesheetService {

    private static final BigDecimal MAX_HOURS_PER_DAY = new BigDecimal("16.00");

    private final TimesheetRepository timesheets;
    private final AbsenceRepository absences;
    private final EmployeeDirectory employees;
    private final ProjectDirectory projects;

    public TimesheetService(TimesheetRepository timesheets, AbsenceRepository absences,
                            EmployeeDirectory employees, ProjectDirectory projects) {
        this.timesheets = timesheets;
        this.absences = absences;
        this.employees = employees;
        this.projects = projects;
    }

    public record OpenTimesheet(UUID employeeId, Integer isoYear, Integer isoWeek) {
    }

    public record BookTime(UUID taskId, LocalDate workDate, BigDecimal hours, String entryType,
                           String description, Boolean billable) {
    }

    public record SubmitTimesheet(String comment) {
    }

    public record ApproveTimesheet(UUID approvedBy) {
    }

    public TimesheetView open(OpenTimesheet command) {
        EmployeeDirectory.EmployeeSnapshot employee = requireEmployee(command.employeeId());
        BusinessRuleViolation.requireState(employee.active(), "employee.inactive");
        BusinessRuleViolation.require(command.isoYear() != null && command.isoWeek() != null, "week.missing");

        IsoWeek week = new IsoWeek(command.isoYear(), command.isoWeek());
        timesheets.findByEmployeeAndWeek(employee.employeeId(), week.year(), week.week())
                .ifPresent(existing -> {
                    throw BusinessRuleViolation.conflict("timesheet.duplicate");
                });

        Timesheet timesheet = Timesheet.open(employee.employeeId(), week);
        return view(timesheets.save(timesheet));
    }

    public TimesheetView book(UUID timesheetId, BookTime command) {
        Timesheet timesheet = load(timesheetId);
        EmployeeDirectory.EmployeeSnapshot employee = requireEmployee(timesheet.employeeId());

        BusinessRuleViolation.require(command.workDate() != null, "work_date.missing");
        BusinessRuleViolation.require(command.hours() != null, "hours.missing");
        Hours hours = new Hours(command.hours());

        ProjectDirectory.BookableTask task = projects.findTask(command.taskId())
                .orElseThrow(() -> BusinessRuleViolation.invalid("task.missing"));
        BusinessRuleViolation.requireState(!task.archived(), "task.archived");

        projects.assertProjectBookableOn(task.projectId(), command.workDate());
        BusinessRuleViolation.requireState(
                projects.isMember(task.projectId(), employee.employeeId()), "employee.not_member");
        BusinessRuleViolation.requireState(employee.canBookOn(command.workDate()), "employee.cannot_book_on_date");

        // Het dagmaximum geldt over alle weekstaten heen, niet alleen deze.
        BigDecimal bookedThatDay = timesheets.totalHoursOn(employee.employeeId(), command.workDate());
        BusinessRuleViolation.requireState(
                bookedThatDay.add(hours.value()).compareTo(MAX_HOURS_PER_DAY) <= 0, "day.max_hours");

        boolean blocked = absences.findByEmployee(employee.employeeId()).stream()
                .anyMatch(absence -> absence.blocksFullDay(command.workDate()));
        BusinessRuleViolation.requireState(!blocked, "absence.full_day");

        timesheet.book(
                task.taskId(),
                task.projectId(),
                command.workDate(),
                hours,
                EntryType.parse(command.entryType()),
                command.description(),
                command.billable() == null ? task.billable() : command.billable(),
                employee.contractHours());

        return view(timesheets.save(timesheet));
    }

    public void removeEntry(UUID entryId) {
        Timesheet timesheet = timesheets.findByEntryId(entryId)
                .orElseThrow(() -> BusinessRuleViolation.notFound("entry.missing"));
        timesheet.removeEntry(entryId);
        timesheets.save(timesheet);
    }

    public TimesheetView submit(UUID timesheetId, SubmitTimesheet command) {
        Timesheet timesheet = load(timesheetId);
        EmployeeDirectory.EmployeeSnapshot employee = requireEmployee(timesheet.employeeId());

        BigDecimal absenceHours = absences.findByEmployee(employee.employeeId()).stream()
                .map(absence -> absence.hoursWithin(timesheet.week().firstDay(), timesheet.week().lastDay()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        timesheet.submit(employee.contractHours(), absenceHours, command == null ? null : command.comment());
        return view(timesheets.save(timesheet));
    }

    public TimesheetView approve(UUID timesheetId, ApproveTimesheet command) {
        Timesheet timesheet = load(timesheetId);
        EmployeeDirectory.EmployeeSnapshot employee = requireEmployee(timesheet.employeeId());
        BusinessRuleViolation.require(command != null && command.approvedBy() != null, "approver.missing");
        employees.find(command.approvedBy())
                .orElseThrow(() -> BusinessRuleViolation.invalid("approver.missing"));

        boolean isManager = command.approvedBy().equals(employee.managerId());
        Set<UUID> projectIds = new LinkedHashSet<>(timesheet.entries().stream().map(TimeEntry::projectId).toList());
        boolean isLead = projects.isLeadOnAnyProject(command.approvedBy(), projectIds);

        timesheet.approve(command.approvedBy(), isManager || isLead);
        return view(timesheets.save(timesheet));
    }

    public TimesheetView reject(UUID timesheetId, SubmitTimesheet command) {
        Timesheet timesheet = load(timesheetId);
        timesheet.reject(command == null ? null : command.comment());
        return view(timesheets.save(timesheet));
    }

    @Transactional(readOnly = true)
    public List<TimesheetView> findAll(UUID employeeId) {
        List<Timesheet> found = employeeId == null ? timesheets.findAll() : timesheets.findByEmployee(employeeId);
        return found.stream()
                .sorted((a, b) -> b.week().toString().compareTo(a.week().toString()))
                .map(this::view)
                .toList();
    }

    @Transactional(readOnly = true)
    public TimesheetView findById(UUID id) {
        return view(load(id));
    }

    private Timesheet load(UUID id) {
        return timesheets.findById(id).orElseThrow(() -> BusinessRuleViolation.notFound("timesheet.missing"));
    }

    private EmployeeDirectory.EmployeeSnapshot requireEmployee(UUID employeeId) {
        return employees.find(employeeId)
                .orElseThrow(() -> BusinessRuleViolation.invalid("employee.missing"));
    }

    private TimesheetView view(Timesheet timesheet) {
        String employeeCode = employees.find(timesheet.employeeId())
                .map(EmployeeDirectory.EmployeeSnapshot::employeeCode)
                .orElse(null);
        List<TimesheetView.EntryView> entries = timesheet.entries().stream()
                .sorted((a, b) -> a.workDate().compareTo(b.workDate()))
                .map(entry -> projects.findTask(entry.taskId())
                        .map(task -> TimesheetView.entry(entry, task.projectCode(), task.taskName()))
                        .orElseGet(() -> TimesheetView.entry(entry, null, null)))
                .toList();
        return TimesheetView.from(timesheet, employeeCode, entries);
    }

    /** Verlof heeft geen eigen applicatieservice nodig; het hoort bij dezelfde week. */
    public record RequestAbsence(UUID employeeId, String absenceType, LocalDate startDate, LocalDate endDate,
                                 BigDecimal hoursPerDay, Boolean approved, String reason) {
    }

    public AbsenceView requestAbsence(RequestAbsence command) {
        EmployeeDirectory.EmployeeSnapshot employee = requireEmployee(command.employeeId());
        BusinessRuleViolation.requireState(employee.active(), "employee.inactive");
        BusinessRuleViolation.require(command.startDate() != null && command.endDate() != null, "date.missing");

        boolean overlap = absences.findByEmployee(employee.employeeId()).stream()
                .anyMatch(existing -> existing.overlaps(command.startDate(), command.endDate()));
        BusinessRuleViolation.requireState(!overlap, "absence.overlap");

        Absence absence = Absence.request(employee.employeeId(), Absence.Type.parse(command.absenceType()),
                command.startDate(), command.endDate(), command.hoursPerDay(), command.approved(), command.reason());
        return AbsenceView.from(absences.save(absence), employee.employeeCode());
    }

    @Transactional(readOnly = true)
    public List<AbsenceView> findAbsences(UUID employeeId) {
        List<Absence> found = employeeId == null ? absences.findAll() : absences.findByEmployee(employeeId);
        return found.stream()
                .map(absence -> AbsenceView.from(absence, employees.find(absence.employeeId())
                        .map(EmployeeDirectory.EmployeeSnapshot::employeeCode).orElse(null)))
                .toList();
    }

    public void deleteAbsence(UUID id) {
        Absence absence = absences.findById(id)
                .orElseThrow(() -> BusinessRuleViolation.notFound("absence.missing"));
        absences.delete(absence);
    }
}
