package nl.haystaq.tijdwijs.personeel.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domeinpoort. De implementatie zit in de infrastructuurlaag; het domein kent
 * alleen deze interface.
 */
public interface EmployeeRepository {

    Employee save(Employee employee);

    Optional<Employee> findById(UUID id);

    List<Employee> findAllByOrderByEmployeeCodeAsc();

    boolean existsByEmployeeCode(EmployeeCode employeeCode);

    boolean existsByEmail(EmailAddress email);

    long count();
}
