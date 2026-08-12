package nl.haystaq.tijdwijs.personeel.application;

import nl.haystaq.tijdwijs.personeel.domain.Employee;
import nl.haystaq.tijdwijs.personeel.domain.EmployeeRepository;
import nl.haystaq.tijdwijs.urenregistratie.domain.EmployeeDirectory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Vertaalt het aggregate {@link Employee} naar het beeld dat andere contexten
 * nodig hebben. Zo lekt het personeelsmodel niet naar buiten.
 */
@Component
public class EmployeeDirectoryAdapter implements EmployeeDirectory {

    private final EmployeeRepository employees;

    public EmployeeDirectoryAdapter(EmployeeRepository employees) {
        this.employees = employees;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeSnapshot> find(UUID employeeId) {
        if (employeeId == null) {
            return Optional.empty();
        }
        return employees.findById(employeeId).map(employee -> new EmployeeSnapshot(
                employee.id(),
                employee.employeeCode().value(),
                employee.firstName() + " " + employee.lastName(),
                employee.contractHours(),
                employee.isActive(),
                employee.employmentPeriod().hireDate(),
                employee.employmentPeriod().endDate(),
                employee.managerId()));
    }
}
