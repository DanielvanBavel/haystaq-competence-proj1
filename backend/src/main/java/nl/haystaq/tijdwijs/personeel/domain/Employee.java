package nl.haystaq.tijdwijs.personeel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;
import nl.haystaq.tijdwijs.shared.domain.Hours;
import nl.haystaq.tijdwijs.shared.domain.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Aggregate root van het context personeel.
 * <p>
 * Alle invarianten zitten in deze klasse. Er zijn geen setters: wijzigen kan
 * alleen via gedrag dat de invarianten opnieuw bewaakt.
 */
@Entity
@Table(name = "employee")
public class Employee {

    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9][0-9 ]{7,19}$");
    private static final BigDecimal MAX_CONTRACT_HOURS = new BigDecimal("40.00");
    private static final BigDecimal HALF = new BigDecimal("0.5");
    private static final int MINIMUM_AGE_AT_HIRE = 16;
    private static final int MAXIMUM_AGE = 70;

    @Id
    private UUID id;

    @Column(name = "employee_code", nullable = false, unique = true)
    private EmployeeCode employeeCode;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private EmailAddress email;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Embedded
    private EmploymentPeriod employmentPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false)
    private ContractType contractType;

    @Column(name = "contract_hours", nullable = false)
    private Hours contractHours;

    @Column(name = "hourly_rate", nullable = false)
    private Money hourlyRate;

    @Column(name = "iban", nullable = false)
    private Iban iban;

    @Column(name = "phone")
    private String phone;

    @Column(name = "manager_id")
    private UUID managerId;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Employee() {
        // voor JPA
    }

    private Employee(UUID id, EmployeeCode employeeCode, String firstName, String lastName, EmailAddress email,
                     LocalDate birthDate, EmploymentPeriod employmentPeriod, ContractType contractType,
                     Hours contractHours, Money hourlyRate, Iban iban, String phone, UUID managerId, boolean active) {
        this.id = id;
        this.employeeCode = employeeCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.employmentPeriod = employmentPeriod;
        this.contractType = contractType;
        this.contractHours = contractHours;
        this.hourlyRate = hourlyRate;
        this.iban = iban;
        this.phone = phone;
        this.managerId = managerId;
        this.active = active;
    }

    public static Employee register(EmployeeCode code, String firstName, String lastName, EmailAddress email,
                                    LocalDate birthDate, EmploymentPeriod period, ContractType contractType,
                                    Hours contractHours, Money hourlyRate, Iban iban, String phone,
                                    UUID managerId, boolean active) {
        validateName(firstName, "first_name");
        validateName(lastName, "last_name");
        validateBirthDate(birthDate, period);
        validateContractHours(contractHours);
        validatePhone(phone);
        contractType.validateRate(hourlyRate);
        return new Employee(UUID.randomUUID(), code, firstName.trim(), lastName.trim(), email, birthDate, period,
                contractType, contractHours, hourlyRate, iban, phone, managerId, active);
    }

    public void changeContract(ContractType contractType, Hours contractHours, Money hourlyRate) {
        validateContractHours(contractHours);
        contractType.validateRate(hourlyRate);
        this.contractType = contractType;
        this.contractHours = contractHours;
        this.hourlyRate = hourlyRate;
    }

    public void changeContactDetails(EmailAddress email, String phone, Iban iban) {
        validatePhone(phone);
        this.email = email;
        this.phone = phone;
        this.iban = iban;
    }

    public void assignManager(UUID managerId) {
        BusinessRuleViolation.requireState(managerId == null || !managerId.equals(id), "manager.self");
        this.managerId = managerId;
    }

    public void endEmployment(LocalDate endDate) {
        this.employmentPeriod = new EmploymentPeriod(employmentPeriod.hireDate(), endDate);
        if (endDate != null && !endDate.isAfter(LocalDate.now())) {
            this.active = false;
        }
    }

    public void reactivate() {
        BusinessRuleViolation.requireState(employmentPeriod.endDate() == null
                || employmentPeriod.endDate().isAfter(LocalDate.now()), "employee.employment_ended");
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    /** Mag deze medewerker op deze datum uren boeken? */
    public void assertCanBookOn(LocalDate date) {
        BusinessRuleViolation.requireState(active, "employee.inactive");
        BusinessRuleViolation.requireState(!date.isBefore(employmentPeriod.hireDate()), "work_date.before_hire");
        BusinessRuleViolation.requireState(employmentPeriod.endDate() == null
                || !date.isAfter(employmentPeriod.endDate()), "work_date.after_employment");
    }

    public boolean isManagedBy(UUID candidate) {
        return managerId != null && managerId.equals(candidate);
    }

    private static void validateName(String value, String field) {
        BusinessRuleViolation.require(value != null && !value.isBlank() && value.trim().length() <= 60, field);
    }

    private static void validateBirthDate(LocalDate birthDate, EmploymentPeriod period) {
        BusinessRuleViolation.require(birthDate != null, "birth_date.missing");
        BusinessRuleViolation.require(
                Period.between(birthDate, period.hireDate()).getYears() >= MINIMUM_AGE_AT_HIRE, "age.minimum");
        BusinessRuleViolation.require(
                Period.between(birthDate, LocalDate.now()).getYears() <= MAXIMUM_AGE, "age.maximum");
    }

    private static void validateContractHours(Hours contractHours) {
        BusinessRuleViolation.require(contractHours != null, "contract_hours.missing");
        BusinessRuleViolation.require(contractHours.value().compareTo(MAX_CONTRACT_HOURS) <= 0, "contract_hours.max");
        BusinessRuleViolation.require(
                contractHours.value().remainder(HALF).compareTo(BigDecimal.ZERO) == 0, "contract_hours.step");
    }

    private static void validatePhone(String phone) {
        BusinessRuleViolation.require(phone == null || phone.isBlank() || PHONE.matcher(phone).matches(), "phone.format");
    }

    public UUID id() {
        return id;
    }

    public EmployeeCode employeeCode() {
        return employeeCode;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public EmailAddress email() {
        return email;
    }

    public LocalDate birthDate() {
        return birthDate;
    }

    public EmploymentPeriod employmentPeriod() {
        return employmentPeriod;
    }

    public ContractType contractType() {
        return contractType;
    }

    public Hours contractHours() {
        return contractHours;
    }

    public Money hourlyRate() {
        return hourlyRate;
    }

    public Iban iban() {
        return iban;
    }

    public String phone() {
        return phone;
    }

    public UUID managerId() {
        return managerId;
    }

    public boolean isActive() {
        return active;
    }
}
