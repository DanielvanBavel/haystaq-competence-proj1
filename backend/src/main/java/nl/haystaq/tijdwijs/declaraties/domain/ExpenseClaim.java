package nl.haystaq.tijdwijs.declaraties.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;
import nl.haystaq.tijdwijs.shared.domain.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/** Aggregate root van het context declaraties. */
@Entity
@Table(name = "expense_claim")
public class ExpenseClaim {

    private static final Pattern RECEIPT = Pattern.compile("^RCP-\\d{6}$");
    private static final List<String> CURRENCIES = List.of("EUR", "USD", "GBP");
    private static final List<BigDecimal> ALLOWED_VAT_RATES =
            List.of(new BigDecimal("0"), new BigDecimal("9"), new BigDecimal("21"));
    private static final Money RECEIPT_THRESHOLD = Money.of(new BigDecimal("25.00"));
    private static final Money STANDARD_LIMIT = Money.of(new BigDecimal("5000.00"));
    private static final Money HARDWARE_LIMIT = Money.of(new BigDecimal("10000.00"));
    private static final int MAX_AGE_DAYS = 90;

    public enum Category {
        TRAVEL,
        MEALS,
        HARDWARE,
        SOFTWARE,
        OTHER;

        public static Category parse(String raw) {
            BusinessRuleViolation.require(raw != null, "category.missing");
            try {
                return valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw BusinessRuleViolation.invalid("category.unknown");
            }
        }

        Money limit() {
            return this == HARDWARE ? HARDWARE_LIMIT : STANDARD_LIMIT;
        }

        /** Vast btw-tarief per categorie. Staat niet in de functionele documentatie. */
        BigDecimal requiredVatRate() {
            return switch (this) {
                case MEALS -> new BigDecimal("9.00");
                case SOFTWARE -> new BigDecimal("21.00");
                default -> null;
            };
        }
    }

    public enum Status {
        DRAFT,
        SUBMITTED,
        APPROVED,
        REJECTED,
        PAID
    }

    @Id
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "project_id")
    private UUID projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "amount", nullable = false)
    private Money amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "vat_rate", nullable = false)
    private BigDecimal vatRate;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "receipt_reference")
    private String receiptReference;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected ExpenseClaim() {
        // voor JPA
    }

    public static ExpenseClaim file(UUID employeeId, UUID projectId, Category category, Money amount,
                                    String currency, BigDecimal vatRate, LocalDate expenseDate,
                                    String receiptReference, String description) {
        BusinessRuleViolation.require(employeeId != null, "employee_id.missing");
        BusinessRuleViolation.require(amount != null, "amount.missing");
        BusinessRuleViolation.require(expenseDate != null, "expense_date.missing");

        String resolvedCurrency = currency == null ? "EUR" : currency.toUpperCase(Locale.ROOT);
        BusinessRuleViolation.require(CURRENCIES.contains(resolvedCurrency), "currency.unknown");

        BigDecimal candidateVat = vatRate == null ? new BigDecimal("21") : vatRate;
        BusinessRuleViolation.require(
                ALLOWED_VAT_RATES.stream().anyMatch(allowed -> allowed.compareTo(candidateVat) == 0),
                "vat_rate.unknown");
        BigDecimal resolvedVat = candidateVat.setScale(2, RoundingMode.HALF_UP);

        BusinessRuleViolation.require(!amount.isGreaterThan(category.limit()), "amount.category_limit");
        BigDecimal required = category.requiredVatRate();
        BusinessRuleViolation.require(required == null || required.compareTo(resolvedVat) == 0, "vat_rate.category");

        if (amount.isGreaterThan(RECEIPT_THRESHOLD)) {
            BusinessRuleViolation.require(receiptReference != null && RECEIPT.matcher(receiptReference).matches(),
                    "receipt_reference.required");
        }
        if (!"EUR".equals(resolvedCurrency)) {
            BusinessRuleViolation.require(description != null && !description.isBlank(),
                    "description.foreign_currency");
        }
        BusinessRuleViolation.require(!expenseDate.isAfter(LocalDate.now()), "expense_date.future");
        BusinessRuleViolation.require(
                ChronoUnit.DAYS.between(expenseDate, LocalDate.now()) <= MAX_AGE_DAYS, "expense_date.stale");

        ExpenseClaim claim = new ExpenseClaim();
        claim.id = UUID.randomUUID();
        claim.employeeId = employeeId;
        claim.projectId = projectId;
        claim.category = category;
        claim.amount = amount;
        claim.currency = resolvedCurrency;
        claim.vatRate = resolvedVat;
        claim.expenseDate = expenseDate;
        claim.receiptReference = receiptReference;
        claim.description = description;
        claim.status = Status.DRAFT;
        return claim;
    }

    public void submit() {
        BusinessRuleViolation.requireState(status == Status.DRAFT || status == Status.REJECTED, "status.not_submittable");
        this.status = Status.SUBMITTED;
    }

    public void decide(boolean approved) {
        BusinessRuleViolation.requireState(status == Status.SUBMITTED, "status.not_decidable");
        this.status = approved ? Status.APPROVED : Status.REJECTED;
    }

    public void markPaid() {
        BusinessRuleViolation.requireState(status == Status.APPROVED, "status.not_payable");
        this.status = Status.PAID;
    }

    public UUID id() {
        return id;
    }

    public UUID employeeId() {
        return employeeId;
    }

    public UUID projectId() {
        return projectId;
    }

    public Category category() {
        return category;
    }

    public Money amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public BigDecimal vatRate() {
        return vatRate;
    }

    public LocalDate expenseDate() {
        return expenseDate;
    }

    public String receiptReference() {
        return receiptReference;
    }

    public String description() {
        return description;
    }

    public Status status() {
        return status;
    }
}
