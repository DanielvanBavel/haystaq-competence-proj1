package nl.haystaq.tijdwijs.projecten.infrastructure;

import nl.haystaq.tijdwijs.projecten.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClientSpringDataRepository extends JpaRepository<Client, UUID> {

    List<Client> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}
