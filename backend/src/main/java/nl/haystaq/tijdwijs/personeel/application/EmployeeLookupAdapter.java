package nl.haystaq.tijdwijs.personeel.application;

import nl.haystaq.tijdwijs.declaraties.domain.EmployeeLookup;
import nl.haystaq.tijdwijs.personeel.domain.Employee;
import nl.haystaq.tijdwijs.personeel.domain.EmployeeRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class EmployeeLookupAdapter implements EmployeeLookup {

    private final EmployeeRepository employees;

    public EmployeeLookupAdapter(EmployeeRepository employees) {
        this.employees = employees;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> employeeCode(UUID employeeId) {
        if (employeeId == null) {
            return Optional.empty();
        }
        return employees.findById(employeeId).map(Employee::employeeCode).map(code -> code.value());
    }
}
