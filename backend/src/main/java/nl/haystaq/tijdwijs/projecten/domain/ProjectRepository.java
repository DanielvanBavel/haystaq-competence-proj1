package nl.haystaq.tijdwijs.projecten.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {

    Project save(Project project);

    Optional<Project> findById(UUID id);

    Optional<Project> findByTaskId(UUID taskId);

    List<Project> findAllByOrderByCodeAsc();

    boolean existsByCode(ProjectCode code);
}
