package nl.haystaq.tijdwijs.projecten.infrastructure;

import nl.haystaq.tijdwijs.projecten.domain.Project;
import nl.haystaq.tijdwijs.projecten.domain.ProjectCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectSpringDataRepository extends JpaRepository<Project, UUID> {

    List<Project> findAllByOrderByCodeAsc();

    boolean existsByCode(ProjectCode code);

    @Query("select p from Project p join p.tasks t where t.id = :taskId")
    Optional<Project> findByTaskId(UUID taskId);
}
