package nl.haystaq.tijdwijs.personeel.infrastructure;

import nl.haystaq.tijdwijs.personeel.domain.EmailAddress;
import nl.haystaq.tijdwijs.personeel.domain.Employee;
import nl.haystaq.tijdwijs.personeel.domain.EmployeeCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data is een infrastructuurdetail. Het domein praat met
 * {@link nl.haystaq.tijdwijs.personeel.domain.EmployeeRepository}.
 */
public interface EmployeeSpringDataRepository extends JpaRepository<Employee, UUID> {

    List<Employee> findAllByOrderByEmployeeCodeAsc();

    boolean existsByEmployeeCode(EmployeeCode employeeCode);

    boolean existsByEmail(EmailAddress email);
}
