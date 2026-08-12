package nl.haystaq.tijdwijs.projecten.application;

import nl.haystaq.tijdwijs.projecten.domain.Project;
import nl.haystaq.tijdwijs.projecten.domain.ProjectMember;
import nl.haystaq.tijdwijs.projecten.domain.ProjectTask;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectView(
        UUID id,
        UUID clientId,
        String clientName,
        String code,
        String name,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal budgetHours,
        boolean billable,
        BigDecimal defaultRate,
        List<TaskView> tasks,
        List<MemberView> members) {

    public record TaskView(UUID id, String name, boolean billable, BigDecimal rateOverride, boolean archived) {
        static TaskView from(ProjectTask task) {
            return new TaskView(task.id(), task.name(), task.isBillable(),
                    task.rateOverride() == null ? null : task.rateOverride().amount(), task.isArchived());
        }
    }

    public record MemberView(UUID employeeId, String role) {
        static MemberView from(ProjectMember member) {
            return new MemberView(member.employeeId(), member.role().name());
        }
    }

    public static ProjectView from(Project project, String clientName) {
        return new ProjectView(
                project.id(),
                project.clientId(),
                clientName,
                project.code().value(),
                project.name(),
                project.status().name(),
                project.startDate(),
                project.endDate(),
                project.budgetHours() == null ? null : project.budgetHours().value(),
                project.isBillable(),
                project.defaultRate() == null ? null : project.defaultRate().amount(),
                project.tasks().stream().map(TaskView::from).toList(),
                project.members().stream().map(MemberView::from).toList());
    }
}
