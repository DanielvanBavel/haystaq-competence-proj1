package nl.haystaq.tijdwijs.urenregistratie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Aggregate root: een aaneengesloten periode van verlof of verzuim. */
@Entity
@Table(name = "absence")
public class Absence {

    private static final BigDecimal FULL_DAY = new BigDecimal("8.00");
    private static final BigDecimal HALF = new BigDecimal("0.5");
    private static final int MAX_DURATION_DAYS = 60;
    private static final int MAX_RETROACTIVE_SICK_DAYS = 14;

    public enum Type {
        VACATION,
        SICK,
        PARENTAL,
        UNPAID,
        SPECIAL;

        public static Type parse(String raw) {
            BusinessRuleViolation.require(raw != null, "absence_type.missing");
            try {
                return valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw BusinessRuleViolation.invalid("absence_type.unknown");
            }
        }
    }

    @Id
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "absence_type", nullable = false)
    private Type absenceType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "hours_per_day", nullable = false)
    private BigDecimal hoursPerDay;

    @Column(name = "approved", nullable = false)
    private boolean approved;

    @Column(name = "reason")
    private String reason;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Absence() {
        // voor JPA
    }

    public static Absence request(UUID employeeId, Type type, LocalDate startDate, LocalDate endDate,
                                  BigDecimal hoursPerDay, Boolean approved, String reason) {
        BusinessRuleViolation.require(employeeId != null, "employee_id.missing");
        BusinessRuleViolation.require(startDate != null && endDate != null, "date.missing");
        BusinessRuleViolation.require(!endDate.isBefore(startDate), "date.order");
        BusinessRuleViolation.require(
                ChronoUnit.DAYS.between(startDate, endDate) <= MAX_DURATION_DAYS, "duration.max");

        BigDecimal perDay = hoursPerDay == null ? FULL_DAY : hoursPerDay;
        BusinessRuleViolation.require(perDay.signum() > 0 && perDay.compareTo(FULL_DAY) <= 0, "hours_per_day.range");
        BusinessRuleViolation.require(perDay.remainder(HALF).compareTo(BigDecimal.ZERO) == 0, "hours_per_day.step");

        if (type == Type.SICK) {
            BusinessRuleViolation.require(
                    ChronoUnit.DAYS.between(startDate, LocalDate.now()) <= MAX_RETROACTIVE_SICK_DAYS,
                    "sick.retroactive");
        }
        if (type == Type.SPECIAL) {
            BusinessRuleViolation.require(reason != null && !reason.isBlank(), "special.reason_required");
        }

        Absence absence = new Absence();
        absence.id = UUID.randomUUID();
        absence.employeeId = employeeId;
        absence.absenceType = type;
        absence.startDate = startDate;
        absence.endDate = endDate;
        absence.hoursPerDay = perDay;
        absence.approved = approved != null && approved;
        absence.reason = reason;
        return absence;
    }

    public void approve() {
        this.approved = true;
    }

    public boolean overlaps(LocalDate from, LocalDate to) {
        return !startDate.isAfter(to) && !endDate.isBefore(from);
    }

    /** Blokkeert deze afwezigheid een hele werkdag? */
    public boolean blocksFullDay(LocalDate date) {
        return approved && hoursPerDay.compareTo(FULL_DAY) >= 0 && overlaps(date, date);
    }

    /** Goedgekeurde verlofuren die binnen het opgegeven venster vallen. */
    public BigDecimal hoursWithin(LocalDate from, LocalDate to) {
        if (!approved || !overlaps(from, to)) {
            return BigDecimal.ZERO;
        }
        LocalDate first = startDate.isBefore(from) ? from : startDate;
        LocalDate last = endDate.isAfter(to) ? to : endDate;
        long days = ChronoUnit.DAYS.between(first, last) + 1;
        return hoursPerDay.multiply(BigDecimal.valueOf(days));
    }

    public UUID id() {
        return id;
    }

    public UUID employeeId() {
        return employeeId;
    }

    public Type absenceType() {
        return absenceType;
    }

    public LocalDate startDate() {
        return startDate;
    }

    public LocalDate endDate() {
        return endDate;
    }

    public BigDecimal hoursPerDay() {
        return hoursPerDay;
    }

    public boolean isApproved() {
        return approved;
    }

    public String reason() {
        return reason;
    }
}
