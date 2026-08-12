package nl.haystaq.tijdwijs.projecten.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;
import nl.haystaq.tijdwijs.shared.domain.Hours;
import nl.haystaq.tijdwijs.shared.domain.Money;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate root van het context projecten. Taken en teamleden horen bij het
 * project en worden alleen via deze root gewijzigd.
 */
@Entity
@Table(name = "project")
public class Project {

    @Id
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "code", nullable = false, unique = true)
    private ProjectCode code;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "budget_hours")
    private Hours budgetHours;

    @Column(name = "billable", nullable = false)
    private boolean billable;

    @Column(name = "default_rate")
    private Money defaultRate;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProjectTask> tasks = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_member", joinColumns = @JoinColumn(name = "project_id"))
    private List<ProjectMember> members = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Project() {
        // voor JPA
    }

    public static Project start(UUID clientId, ProjectCode code, String name, ProjectStatus status,
                                LocalDate startDate, LocalDate endDate, Hours budgetHours,
                                boolean billable, Money defaultRate) {
        BusinessRuleViolation.require(clientId != null, "client_id.missing");
        BusinessRuleViolation.require(name != null && !name.isBlank() && name.trim().length() <= 120, "name");
        BusinessRuleViolation.require(startDate != null, "start_date.missing");
        BusinessRuleViolation.require(endDate == null || !endDate.isBefore(startDate), "end_date.order");
        BusinessRuleViolation.require(code.year() == startDate.getYear(), "code.year_mismatch");
        BusinessRuleViolation.require(!billable || defaultRate != null, "billable.rate_required");

        Project project = new Project();
        project.id = UUID.randomUUID();
        project.clientId = clientId;
        project.code = code;
        project.name = name.trim();
        project.status = status == null ? ProjectStatus.DRAFT : status;
        project.startDate = startDate;
        project.endDate = endDate;
        project.budgetHours = budgetHours;
        project.billable = billable;
        project.defaultRate = defaultRate;
        return project;
    }

    public ProjectTask addTask(String taskName, boolean taskBillable, Money rateOverride) {
        BusinessRuleViolation.requireState(status != ProjectStatus.CLOSED, "project.closed");
        BusinessRuleViolation.requireState(
                tasks.stream().noneMatch(t -> t.name().equalsIgnoreCase(taskName == null ? "" : taskName.trim())),
                "task.duplicate");
        ProjectTask task = new ProjectTask(this, taskName, taskBillable, rateOverride);
        tasks.add(task);
        return task;
    }

    public void assignMember(UUID employeeId, ProjectMember.Role role) {
        BusinessRuleViolation.requireState(status != ProjectStatus.CLOSED, "project.closed");
        members.removeIf(m -> m.employeeId().equals(employeeId));
        members.add(new ProjectMember(employeeId, role));
    }

    public void changeStatus(ProjectStatus next) {
        BusinessRuleViolation.requireState(status.allowedNext().contains(next), "status.transition");
        if (next == ProjectStatus.ACTIVE) {
            BusinessRuleViolation.requireState(!tasks.isEmpty(), "project.no_tasks");
        }
        this.status = next;
    }

    public void reschedule(LocalDate newStart, LocalDate newEnd) {
        BusinessRuleViolation.require(newStart != null, "start_date.missing");
        BusinessRuleViolation.require(newEnd == null || !newEnd.isBefore(newStart), "end_date.order");
        BusinessRuleViolation.require(code.year() == newStart.getYear(), "code.year_mismatch");
        this.startDate = newStart;
        this.endDate = newEnd;
    }

    public void changeCommercials(Hours newBudget, boolean newBillable, Money newDefaultRate) {
        BusinessRuleViolation.require(!newBillable || newDefaultRate != null, "billable.rate_required");
        this.budgetHours = newBudget;
        this.billable = newBillable;
        this.defaultRate = newDefaultRate;
    }

    /** Kernregel: op welke datums mag er op dit project geboekt worden? */
    public void assertBookableOn(LocalDate date) {
        BusinessRuleViolation.requireState(status.allowsBooking(), "project.not_active");
        BusinessRuleViolation.requireState(!date.isBefore(startDate), "work_date.before_project");
        BusinessRuleViolation.requireState(endDate == null || !date.isAfter(endDate), "work_date.after_project");
    }

    public boolean hasMember(UUID employeeId) {
        return members.stream().anyMatch(m -> m.employeeId().equals(employeeId));
    }

    public boolean hasLead(UUID employeeId) {
        return members.stream()
                .anyMatch(m -> m.employeeId().equals(employeeId) && m.role() == ProjectMember.Role.LEAD);
    }

    public Optional<ProjectTask> task(UUID taskId) {
        return tasks.stream().filter(t -> t.id().equals(taskId)).findFirst();
    }

    public UUID id() {
        return id;
    }

    public UUID clientId() {
        return clientId;
    }

    public ProjectCode code() {
        return code;
    }

    public String name() {
        return name;
    }

    public ProjectStatus status() {
        return status;
    }

    public LocalDate startDate() {
        return startDate;
    }

    public LocalDate endDate() {
        return endDate;
    }

    public Hours budgetHours() {
        return budgetHours;
    }

    public boolean isBillable() {
        return billable;
    }

    public Money defaultRate() {
        return defaultRate;
    }

    public List<ProjectTask> tasks() {
        return List.copyOf(tasks);
    }

    public List<ProjectMember> members() {
        return List.copyOf(members);
    }
}
