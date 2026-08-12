package nl.haystaq.tijdwijs.declaraties.domain;

import java.util.Optional;
import java.util.UUID;

/** Poort naar het context personeel: declaraties hoeft alleen te weten of de medewerker bestaat. */
public interface EmployeeLookup {

    Optional<String> employeeCode(UUID employeeId);
}
