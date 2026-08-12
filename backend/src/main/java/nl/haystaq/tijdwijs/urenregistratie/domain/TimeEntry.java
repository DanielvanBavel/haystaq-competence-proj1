package nl.haystaq.tijdwijs.urenregistratie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;
import nl.haystaq.tijdwijs.shared.domain.Hours;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Entiteit binnen het aggregate {@link Timesheet}. */
@Entity
@Table(name = "time_entry")
public class TimeEntry {

    private static final BigDecimal MAX_HOURS_PER_ENTRY = new BigDecimal("12.00");

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "timesheet_id", nullable = false)
    private Timesheet timesheet;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "hours", nullable = false)
    private Hours hours;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private EntryType entryType;

    @Column(name = "description")
    private String description;

    @Column(name = "billable", nullable = false)
    private boolean billable;

    protected TimeEntry() {
        // voor JPA
    }

    TimeEntry(Timesheet timesheet, UUID taskId, UUID projectId, LocalDate workDate, Hours hours,
              EntryType entryType, String description, boolean billable) {
        BusinessRuleViolation.require(taskId != null, "task_id.missing");
        BusinessRuleViolation.require(workDate != null, "work_date.missing");
        BusinessRuleViolation.require(hours.value().compareTo(MAX_HOURS_PER_ENTRY) <= 0, "hours.max");
        BusinessRuleViolation.require(description == null || description.length() <= 500, "description.length");
        // Niet-declarabele uren moeten worden toegelicht.
        BusinessRuleViolation.require(billable || (description != null && description.trim().length() >= 3),
                "description.required_non_billable");
        this.id = UUID.randomUUID();
        this.timesheet = timesheet;
        this.taskId = taskId;
        this.projectId = projectId;
        this.workDate = workDate;
        this.hours = hours;
        this.entryType = entryType;
        this.description = description;
        this.billable = billable;
    }

    public UUID id() {
        return id;
    }

    public UUID taskId() {
        return taskId;
    }

    public UUID projectId() {
        return projectId;
    }

    public LocalDate workDate() {
        return workDate;
    }

    public Hours hours() {
        return hours;
    }

    public EntryType entryType() {
        return entryType;
    }

    public String description() {
        return description;
    }

    public boolean isBillable() {
        return billable;
    }
}
