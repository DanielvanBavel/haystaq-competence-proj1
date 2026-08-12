package nl.haystaq.tijdwijs.personeel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;

import java.time.LocalDate;

/** Dienstverband: startdatum met optionele einddatum. */
@Embeddable
public class EmploymentPeriod {

    private static final LocalDate EARLIEST = LocalDate.of(1990, 1, 1);

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    protected EmploymentPeriod() {
        // voor JPA
    }

    public EmploymentPeriod(LocalDate hireDate, LocalDate endDate) {
        BusinessRuleViolation.require(hireDate != null, "hire_date.missing");
        BusinessRuleViolation.require(!hireDate.isBefore(EARLIEST), "hire_date.range");
        BusinessRuleViolation.require(!hireDate.isAfter(LocalDate.now().plusDays(365)), "hire_date.future");
        BusinessRuleViolation.require(endDate == null || endDate.isAfter(hireDate), "end_date.order");
        this.hireDate = hireDate;
        this.endDate = endDate;
    }

    public boolean covers(LocalDate date) {
        return !date.isBefore(hireDate) && (endDate == null || !date.isAfter(endDate));
    }

    public LocalDate hireDate() {
        return hireDate;
    }

    public LocalDate endDate() {
        return endDate;
    }
}
