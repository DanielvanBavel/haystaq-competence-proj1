package nl.haystaq.tijdwijs.personeel.infrastructure;

import nl.haystaq.tijdwijs.personeel.domain.EmailAddress;
import nl.haystaq.tijdwijs.personeel.domain.Employee;
import nl.haystaq.tijdwijs.personeel.domain.EmployeeCode;
import nl.haystaq.tijdwijs.personeel.domain.EmployeeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class EmployeeRepositoryAdapter implements EmployeeRepository {

    private final EmployeeSpringDataRepository delegate;

    public EmployeeRepositoryAdapter(EmployeeSpringDataRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Employee save(Employee employee) {
        return delegate.save(employee);
    }

    @Override
    public Optional<Employee> findById(UUID id) {
        return delegate.findById(id);
    }

    @Override
    public List<Employee> findAllByOrderByEmployeeCodeAsc() {
        return delegate.findAllByOrderByEmployeeCodeAsc();
    }

    @Override
    public boolean existsByEmployeeCode(EmployeeCode employeeCode) {
        return delegate.existsByEmployeeCode(employeeCode);
    }

    @Override
    public boolean existsByEmail(EmailAddress email) {
        return delegate.existsByEmail(email);
    }

    @Override
    public long count() {
        return delegate.count();
    }
}
