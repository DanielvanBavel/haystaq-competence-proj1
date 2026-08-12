package nl.haystaq.tijdwijs.urenregistratie.application;

import nl.haystaq.tijdwijs.urenregistratie.domain.TimeEntry;
import nl.haystaq.tijdwijs.urenregistratie.domain.Timesheet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TimesheetView(
        UUID id,
        UUID employeeId,
        String employeeCode,
        int isoYear,
        int isoWeek,
        LocalDate weekStart,
        LocalDate weekEnd,
        String status,
        OffsetDateTime submittedAt,
        OffsetDateTime approvedAt,
        UUID approvedBy,
        String comment,
        BigDecimal totalHours,
        List<EntryView> entries) {

    public record EntryView(
            UUID id,
            UUID taskId,
            UUID projectId,
            String projectCode,
            String taskName,
            LocalDate workDate,
            BigDecimal hours,
            String entryType,
            String description,
            boolean billable) {
    }

    public static TimesheetView from(Timesheet timesheet, String employeeCode, List<EntryView> entries) {
        return new TimesheetView(
                timesheet.id(),
                timesheet.employeeId(),
                employeeCode,
                timesheet.week().year(),
                timesheet.week().week(),
                timesheet.week().firstDay(),
                timesheet.week().lastDay(),
                timesheet.status().name(),
                timesheet.submittedAt(),
                timesheet.approvedAt(),
                timesheet.approvedBy(),
                timesheet.comment(),
                timesheet.totalHours(),
                entries);
    }

    public static EntryView entry(TimeEntry entry, String projectCode, String taskName) {
        return new EntryView(
                entry.id(),
                entry.taskId(),
                entry.projectId(),
                projectCode,
                taskName,
                entry.workDate(),
                entry.hours().value(),
                entry.entryType().name(),
                entry.description(),
                entry.isBillable());
    }
}
