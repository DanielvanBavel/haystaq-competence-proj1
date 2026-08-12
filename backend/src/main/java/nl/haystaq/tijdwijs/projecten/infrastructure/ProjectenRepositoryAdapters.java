package nl.haystaq.tijdwijs.projecten.infrastructure;

import nl.haystaq.tijdwijs.projecten.domain.Client;
import nl.haystaq.tijdwijs.projecten.domain.ClientRepository;
import nl.haystaq.tijdwijs.projecten.domain.Project;
import nl.haystaq.tijdwijs.projecten.domain.ProjectCode;
import nl.haystaq.tijdwijs.projecten.domain.ProjectRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adapters die de Spring Data repositories achter de domeinpoorten verbergen. */
@Configuration
public class ProjectenRepositoryAdapters {

    @Bean
    ProjectRepository projectRepository(ProjectSpringDataRepository delegate) {
        return new ProjectRepository() {
            @Override
            public Project save(Project project) {
                return delegate.save(project);
            }

            @Override
            public Optional<Project> findById(UUID id) {
                return delegate.findById(id);
            }

            @Override
            public Optional<Project> findByTaskId(UUID taskId) {
                return delegate.findByTaskId(taskId);
            }

            @Override
            public List<Project> findAllByOrderByCodeAsc() {
                return delegate.findAllByOrderByCodeAsc();
            }

            @Override
            public boolean existsByCode(ProjectCode code) {
                return delegate.existsByCode(code);
            }
        };
    }

    @Bean
    ClientRepository clientRepository(ClientSpringDataRepository delegate) {
        return new ClientRepository() {
            @Override
            public Client save(Client client) {
                return delegate.save(client);
            }

            @Override
            public Optional<Client> findById(UUID id) {
                return delegate.findById(id);
            }

            @Override
            public List<Client> findAllByOrderByNameAsc() {
                return delegate.findAllByOrderByNameAsc();
            }

            @Override
            public boolean existsByName(String name) {
                return delegate.existsByNameIgnoreCase(name);
            }
        };
    }
}
