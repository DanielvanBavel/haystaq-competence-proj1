package nl.haystaq.tijdwijs.projecten.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository {

    Client save(Client client);

    Optional<Client> findById(UUID id);

    List<Client> findAllByOrderByNameAsc();

    boolean existsByName(String name);
}
