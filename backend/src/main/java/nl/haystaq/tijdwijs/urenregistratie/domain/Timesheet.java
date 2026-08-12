package nl.haystaq.tijdwijs.urenregistratie.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;
import nl.haystaq.tijdwijs.shared.domain.Hours;
import nl.haystaq.tijdwijs.shared.domain.IsoWeek;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root van het context urenregistratie: een weekstaat met urenregels.
 * <p>
 * De weekstaat bewaakt alles wat binnen de week te controleren is. Regels die
 * andere aggregates nodig hebben (project actief, medewerker in dienst, verlof)
 * worden gecoordineerd door de applicatielaag.
 */
@Entity
@Table(name = "timesheet")
public class Timesheet {

    private static final BigDecimal MAX_HOURS_PER_DAY = new BigDecimal("16.00");

    @Id
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Embedded
    private IsoWeek week;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TimesheetStatus status;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "comment")
    private String comment;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TimeEntry> entries = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Timesheet() {
        // voor JPA
    }

    public static Timesheet open(UUID employeeId, IsoWeek week) {
        BusinessRuleViolation.require(employeeId != null, "employee_id.missing");
        BusinessRuleViolation.require(week != null, "week.missing");
        Timesheet timesheet = new Timesheet();
        timesheet.id = UUID.randomUUID();
        timesheet.employeeId = employeeId;
        timesheet.week = week;
        timesheet.status = TimesheetStatus.DRAFT;
        return timesheet;
    }

    public TimeEntry book(UUID taskId, UUID projectId, LocalDate workDate, Hours hours, EntryType entryType,
                          String description, boolean billable, Hours contractHours) {
        BusinessRuleViolation.requireState(status.isEditable(), "timesheet.locked");
        BusinessRuleViolation.require(week.contains(workDate), "work_date.outside_week");
        BusinessRuleViolation.requireState(
                totalOn(workDate).add(hours.value()).compareTo(MAX_HOURS_PER_DAY) <= 0, "day.max_hours");
        if (entryType == EntryType.OVERTIME) {
            // Overwerk kan pas nadat de contracturen van de week vol zijn.
            BusinessRuleViolation.requireState(
                    totalHours().compareTo(contractHours.value()) >= 0, "overtime.before_contract_hours");
        }
        TimeEntry entry = new TimeEntry(this, taskId, projectId, workDate, hours, entryType, description, billable);
        entries.add(entry);
        return entry;
    }

    public void removeEntry(UUID entryId) {
        BusinessRuleViolation.requireState(status.isEditable(), "timesheet.locked");
        boolean removed = entries.removeIf(e -> e.id().equals(entryId));
        BusinessRuleViolation.requireState(removed, "entry.missing");
    }

    /**
     * Indienen kan pas als de week gedekt is: geboekte uren plus goedgekeurd
     * verlof moeten samen minimaal de contracturen halen.
     */
    public void submit(Hours contractHours, BigDecimal approvedAbsenceHours, String comment) {
        BusinessRuleViolation.requireState(status.isEditable(), "status.not_submittable");
        BusinessRuleViolation.requireState(!entries.isEmpty(), "submit.no_entries");
        BigDecimal covered = totalHours().add(approvedAbsenceHours);
        BusinessRuleViolation.requireState(covered.compareTo(contractHours.value()) >= 0, "submit.week_incomplete");
        this.status = TimesheetStatus.SUBMITTED;
        this.submittedAt = OffsetDateTime.now();
        this.comment = comment;
    }

    public void approve(UUID approver, boolean authorised) {
        BusinessRuleViolation.requireState(status == TimesheetStatus.SUBMITTED, "status.not_approvable");
        BusinessRuleViolation.require(approver != null, "approver.missing");
        BusinessRuleViolation.requireState(!approver.equals(employeeId), "approver.self");
        BusinessRuleViolation.requireState(authorised, "approver.not_authorised");
        this.status = TimesheetStatus.APPROVED;
        this.approvedAt = OffsetDateTime.now();
        this.approvedBy = approver;
    }

    public void reject(String reason) {
        BusinessRuleViolation.requireState(status == TimesheetStatus.SUBMITTED, "status.not_rejectable");
        BusinessRuleViolation.require(reason != null && reason.trim().length() >= 5, "comment.required");
        this.status = TimesheetStatus.REJECTED;
        this.comment = reason;
    }

    /** Totalen zijn geen {@link Hours}: een totaal mag nul zijn, een boeking niet. */
    public BigDecimal totalOn(LocalDate date) {
        return entries.stream()
                .filter(e -> e.workDate().equals(date))
                .map(e -> e.hours().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalHours() {
        return entries.stream()
                .map(e -> e.hours().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public UUID id() {
        return id;
    }

    public UUID employeeId() {
        return employeeId;
    }

    public IsoWeek week() {
        return week;
    }

    public TimesheetStatus status() {
        return status;
    }

    public OffsetDateTime submittedAt() {
        return submittedAt;
    }

    public OffsetDateTime approvedAt() {
        return approvedAt;
    }

    public UUID approvedBy() {
        return approvedBy;
    }

    public String comment() {
        return comment;
    }

    public List<TimeEntry> entries() {
        return List.copyOf(entries);
    }
}
