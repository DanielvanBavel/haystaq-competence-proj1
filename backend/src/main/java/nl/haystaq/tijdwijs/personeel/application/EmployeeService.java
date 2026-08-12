package nl.haystaq.tijdwijs.personeel.application;

import nl.haystaq.tijdwijs.personeel.domain.ContractType;
import nl.haystaq.tijdwijs.personeel.domain.EmailAddress;
import nl.haystaq.tijdwijs.personeel.domain.Employee;
import nl.haystaq.tijdwijs.personeel.domain.EmployeeCode;
import nl.haystaq.tijdwijs.personeel.domain.EmployeeRepository;
import nl.haystaq.tijdwijs.personeel.domain.EmploymentPeriod;
import nl.haystaq.tijdwijs.personeel.domain.Iban;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;
import nl.haystaq.tijdwijs.shared.domain.Hours;
import nl.haystaq.tijdwijs.shared.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employees;

    public EmployeeService(EmployeeRepository employees) {
        this.employees = employees;
    }

    public record RegisterEmployee(
            String employeeCode,
            String firstName,
            String lastName,
            String email,
            LocalDate birthDate,
            LocalDate hireDate,
            LocalDate endDate,
            String contractType,
            BigDecimal contractHours,
            BigDecimal hourlyRate,
            String iban,
            String phone,
            UUID managerId,
            Boolean active) {
    }

    public record UpdateEmployee(
            String email,
            String phone,
            String iban,
            String contractType,
            BigDecimal contractHours,
            BigDecimal hourlyRate,
            LocalDate endDate,
            UUID managerId,
            Boolean active) {
    }

    public EmployeeView register(RegisterEmployee command) {
        EmployeeCode code = new EmployeeCode(command.employeeCode());
        EmailAddress email = new EmailAddress(command.email());
        BusinessRuleViolation.requireState(!employees.existsByEmployeeCode(code), "employee_code.duplicate");
        BusinessRuleViolation.requireState(!employees.existsByEmail(email), "email.duplicate");

        if (command.managerId() != null) {
            Employee manager = employees.findById(command.managerId())
                    .orElseThrow(() -> BusinessRuleViolation.invalid("manager.missing"));
            BusinessRuleViolation.requireState(manager.isActive(), "manager.inactive");
        }

        Employee employee = Employee.register(
                code,
                command.firstName(),
                command.lastName(),
                email,
                command.birthDate(),
                new EmploymentPeriod(command.hireDate(), command.endDate()),
                ContractType.parse(command.contractType()),
                new Hours(require(command.contractHours(), "contract_hours.missing")),
                Money.of(require(command.hourlyRate(), "hourly_rate.missing")),
                new Iban(command.iban()),
                command.phone(),
                command.managerId(),
                command.active() == null || command.active());

        return EmployeeView.from(employees.save(employee));
    }

    public EmployeeView update(UUID id, UpdateEmployee command) {
        Employee employee = load(id);

        if (command.email() != null || command.phone() != null || command.iban() != null) {
            employee.changeContactDetails(
                    command.email() != null ? new EmailAddress(command.email()) : employee.email(),
                    command.phone() != null ? command.phone() : employee.phone(),
                    command.iban() != null ? new Iban(command.iban()) : employee.iban());
        }
        if (command.contractType() != null || command.contractHours() != null || command.hourlyRate() != null) {
            employee.changeContract(
                    command.contractType() != null ? ContractType.parse(command.contractType()) : employee.contractType(),
                    command.contractHours() != null ? new Hours(command.contractHours()) : employee.contractHours(),
                    command.hourlyRate() != null ? Money.of(command.hourlyRate()) : employee.hourlyRate());
        }
        if (command.endDate() != null) {
            employee.endEmployment(command.endDate());
        }
        if (command.managerId() != null) {
            employees.findById(command.managerId())
                    .orElseThrow(() -> BusinessRuleViolation.invalid("manager.missing"));
            employee.assignManager(command.managerId());
        }
        if (command.active() != null) {
            if (command.active()) {
                employee.reactivate();
            } else {
                employee.deactivate();
            }
        }
        return EmployeeView.from(employees.save(employee));
    }

    @Transactional(readOnly = true)
    public List<EmployeeView> findAll() {
        return employees.findAllByOrderByEmployeeCodeAsc().stream().map(EmployeeView::from).toList();
    }

    @Transactional(readOnly = true)
    public EmployeeView findById(UUID id) {
        return EmployeeView.from(load(id));
    }

    private Employee load(UUID id) {
        return employees.findById(id).orElseThrow(() -> BusinessRuleViolation.notFound("employee.missing"));
    }

    private static BigDecimal require(BigDecimal value, String code) {
        BusinessRuleViolation.require(value != null, code);
        return value;
    }
}
