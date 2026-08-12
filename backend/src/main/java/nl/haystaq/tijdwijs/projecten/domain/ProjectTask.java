package nl.haystaq.tijdwijs.projecten.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;
import nl.haystaq.tijdwijs.shared.domain.Money;

import java.util.UUID;

/** Entiteit binnen het aggregate {@link Project}. Nooit los te benaderen. */
@Entity
@Table(name = "project_task")
public class ProjectTask {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "billable", nullable = false)
    private boolean billable;

    @Column(name = "rate_override")
    private Money rateOverride;

    @Column(name = "archived", nullable = false)
    private boolean archived;

    protected ProjectTask() {
        // voor JPA
    }

    ProjectTask(Project project, String name, boolean billable, Money rateOverride) {
        BusinessRuleViolation.require(name != null && !name.isBlank() && name.trim().length() <= 80, "name");
        BusinessRuleViolation.require(billable || rateOverride == null, "rate_override.not_billable");
        this.id = UUID.randomUUID();
        this.project = project;
        this.name = name.trim();
        this.billable = billable;
        this.rateOverride = rateOverride;
        this.archived = false;
    }

    public void archive() {
        this.archived = true;
    }

    public UUID id() {
        return id;
    }

    public Project project() {
        return project;
    }

    public String name() {
        return name;
    }

    public boolean isBillable() {
        return billable;
    }

    public Money rateOverride() {
        return rateOverride;
    }

    public boolean isArchived() {
        return archived;
    }
}
