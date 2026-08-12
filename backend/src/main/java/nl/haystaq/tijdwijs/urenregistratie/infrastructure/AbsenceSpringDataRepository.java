package nl.haystaq.tijdwijs.urenregistratie.infrastructure;

import nl.haystaq.tijdwijs.urenregistratie.domain.Absence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AbsenceSpringDataRepository extends JpaRepository<Absence, UUID> {

    List<Absence> findByEmployeeIdOrderByStartDateDesc(UUID employeeId);

    List<Absence> findAllByOrderByStartDateDesc();
}
