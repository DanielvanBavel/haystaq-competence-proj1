package nl.haystaq.tijdwijs.projecten.application;

import nl.haystaq.tijdwijs.projecten.domain.Client;
import nl.haystaq.tijdwijs.projecten.domain.ClientRepository;
import nl.haystaq.tijdwijs.projecten.domain.Project;
import nl.haystaq.tijdwijs.projecten.domain.ProjectCode;
import nl.haystaq.tijdwijs.projecten.domain.ProjectMember;
import nl.haystaq.tijdwijs.projecten.domain.ProjectRepository;
import nl.haystaq.tijdwijs.projecten.domain.ProjectStatus;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;
import nl.haystaq.tijdwijs.shared.domain.Hours;
import nl.haystaq.tijdwijs.shared.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProjectenService {

    private final ProjectRepository projects;
    private final ClientRepository clients;

    public ProjectenService(ProjectRepository projects, ClientRepository clients) {
        this.projects = projects;
        this.clients = clients;
    }

    public record RegisterClient(String name, String contactEmail, String vatNumber, String country,
                                 Integer paymentTermDays, Boolean active) {
    }

    public record StartProject(UUID clientId, String code, String name, String status, LocalDate startDate,
                               LocalDate endDate, BigDecimal budgetHours, Boolean billable, BigDecimal defaultRate) {
    }

    public record ChangeProject(String status, LocalDate startDate, LocalDate endDate, BigDecimal budgetHours,
                                Boolean billable, BigDecimal defaultRate) {
    }

    public record AddTask(String name, Boolean billable, BigDecimal rateOverride) {
    }

    public record AssignMember(UUID employeeId, String role) {
    }

    // -- clients -----------------------------------------------------------

    public ClientView registerClient(RegisterClient command) {
        BusinessRuleViolation.requireState(
                command.name() == null || !clients.existsByName(command.name().trim()), "client.duplicate");
        Client client = Client.register(command.name(), command.contactEmail(), command.vatNumber(),
                command.country(), command.paymentTermDays(), command.active());
        return ClientView.from(clients.save(client));
    }

    @Transactional(readOnly = true)
    public List<ClientView> findAllClients() {
        return clients.findAllByOrderByNameAsc().stream().map(ClientView::from).toList();
    }

    // -- projects ----------------------------------------------------------

    public ProjectView start(StartProject command) {
        ProjectCode code = new ProjectCode(command.code());
        BusinessRuleViolation.requireState(!projects.existsByCode(code), "code.duplicate");

        Client client = clients.findById(command.clientId() == null ? new UUID(0, 0) : command.clientId())
                .orElseThrow(() -> BusinessRuleViolation.invalid("client.missing"));
        BusinessRuleViolation.requireState(client.isActive(), "client.inactive");

        Project project = Project.start(
                client.id(),
                code,
                command.name(),
                ProjectStatus.parse(command.status()),
                command.startDate(),
                command.endDate(),
                command.budgetHours() == null ? null : new Hours(command.budgetHours()),
                command.billable() == null || command.billable(),
                command.defaultRate() == null ? null : Money.of(command.defaultRate()));

        return ProjectView.from(projects.save(project), client.name());
    }

    public ProjectView change(UUID projectId, ChangeProject command) {
        Project project = load(projectId);
        if (command.startDate() != null || command.endDate() != null) {
            project.reschedule(
                    command.startDate() == null ? project.startDate() : command.startDate(),
                    command.endDate() == null ? project.endDate() : command.endDate());
        }
        if (command.budgetHours() != null || command.billable() != null || command.defaultRate() != null) {
            project.changeCommercials(
                    command.budgetHours() == null ? project.budgetHours() : new Hours(command.budgetHours()),
                    command.billable() == null ? project.isBillable() : command.billable(),
                    command.defaultRate() == null ? project.defaultRate() : Money.of(command.defaultRate()));
        }
        if (command.status() != null) {
            project.changeStatus(ProjectStatus.parse(command.status()));
        }
        return view(projects.save(project));
    }

    public ProjectView addTask(UUID projectId, AddTask command) {
        Project project = load(projectId);
        project.addTask(command.name(), command.billable() == null || command.billable(),
                command.rateOverride() == null ? null : Money.of(command.rateOverride()));
        return view(projects.save(project));
    }

    public ProjectView assignMember(UUID projectId, AssignMember command) {
        Project project = load(projectId);
        BusinessRuleViolation.require(command.employeeId() != null, "employee_id.missing");
        project.assignMember(command.employeeId(), ProjectMember.Role.parse(command.role()));
        return view(projects.save(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectView> findAll() {
        return projects.findAllByOrderByCodeAsc().stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public ProjectView findById(UUID id) {
        return view(load(id));
    }

    private Project load(UUID id) {
        return projects.findById(id).orElseThrow(() -> BusinessRuleViolation.notFound("project.missing"));
    }

    private ProjectView view(Project project) {
        String clientName = clients.findById(project.clientId()).map(Client::name).orElse(null);
        return ProjectView.from(project, clientName);
    }
}
