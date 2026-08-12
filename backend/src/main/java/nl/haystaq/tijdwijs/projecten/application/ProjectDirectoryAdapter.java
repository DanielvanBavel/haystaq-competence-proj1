package nl.haystaq.tijdwijs.projecten.application;

import nl.haystaq.tijdwijs.projecten.domain.Project;
import nl.haystaq.tijdwijs.projecten.domain.ProjectRepository;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;
import nl.haystaq.tijdwijs.urenregistratie.domain.ProjectDirectory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProjectDirectoryAdapter implements ProjectDirectory {

    private final ProjectRepository projects;

    public ProjectDirectoryAdapter(ProjectRepository projects) {
        this.projects = projects;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BookableTask> findTask(UUID taskId) {
        if (taskId == null) {
            return Optional.empty();
        }
        return projects.findByTaskId(taskId)
                .flatMap(project -> project.task(taskId)
                        .map(task -> new BookableTask(
                                task.id(),
                                project.id(),
                                project.code().value(),
                                task.name(),
                                task.isArchived(),
                                task.isBillable() && project.isBillable())));
    }

    @Override
    @Transactional(readOnly = true)
    public void assertProjectBookableOn(UUID projectId, LocalDate date) {
        Project project = projects.findById(projectId)
                .orElseThrow(() -> BusinessRuleViolation.invalid("project.missing"));
        project.assertBookableOn(date);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMember(UUID projectId, UUID employeeId) {
        return projects.findById(projectId).map(p -> p.hasMember(employeeId)).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLeadOnAnyProject(UUID employeeId, Collection<UUID> projectIds) {
        return projectIds.stream()
                .map(projects::findById)
                .flatMap(Optional::stream)
                .anyMatch(project -> project.hasLead(employeeId));
    }
}
