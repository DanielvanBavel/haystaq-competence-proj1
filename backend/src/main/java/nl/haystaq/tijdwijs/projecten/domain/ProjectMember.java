package nl.haystaq.tijdwijs.projecten.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;

import java.util.Objects;
import java.util.UUID;

/** Waarde-object binnen het aggregate {@link Project}. */
@Embeddable
public class ProjectMember {

    public enum Role {
        MEMBER,
        LEAD;

        public static Role parse(String raw) {
            if (raw == null) {
                return MEMBER;
            }
            try {
                return valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw BusinessRuleViolation.invalid("role.unknown");
            }
        }
    }

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    protected ProjectMember() {
        // voor JPA
    }

    public ProjectMember(UUID employeeId, Role role) {
        BusinessRuleViolation.require(employeeId != null, "employee_id.missing");
        this.employeeId = employeeId;
        this.role = role == null ? Role.MEMBER : role;
    }

    public UUID employeeId() {
        return employeeId;
    }

    public Role role() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProjectMember other)) {
            return false;
        }
        return Objects.equals(employeeId, other.employeeId) && role == other.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, role);
    }
}
