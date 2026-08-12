package nl.haystaq.tijdwijs.urenregistratie.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AbsenceRepository {

    Absence save(Absence absence);

    Optional<Absence> findById(UUID id);

    List<Absence> findAll();

    List<Absence> findByEmployee(UUID employeeId);

    void delete(Absence absence);
}
