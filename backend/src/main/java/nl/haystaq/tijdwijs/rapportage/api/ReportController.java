package nl.haystaq.tijdwijs.rapportage.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rapportage is een leesmodel: het gaat rechtstreeks naar de database en
 * omzeilt de aggregates bewust. Schrijven gebeurt hier nooit.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final JdbcTemplate jdbc;

    public ReportController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return jdbc.queryForMap("""
                select (select count(*) from employee where active)          as active_employees,
                       (select count(*) from project where status = 'ACTIVE') as active_projects,
                       (select count(*) from client)                          as clients,
                       (select count(*) from timesheet)                       as timesheets,
                       (select count(*) from time_entry)                      as time_entries,
                       (select coalesce(sum(hours), 0) from time_entry)       as total_hours,
                       (select count(*) from absence)                         as absences,
                       (select count(*) from expense_claim)                   as expense_claims
                """);
    }

    @GetMapping("/weekly")
    public List<Map<String, Object>> weekly(@RequestParam(required = false) Integer year,
                                            @RequestParam(required = false) Integer week) {
        StringBuilder sql = new StringBuilder("select * from v_weekly_totals where 1 = 1");
        List<Object> params = new ArrayList<>();
        if (year != null) {
            sql.append(" and iso_year = ?");
            params.add(year);
        }
        if (week != null) {
            sql.append(" and iso_week = ?");
            params.add(week);
        }
        sql.append(" order by iso_year desc, iso_week desc, employee_code");
        return jdbc.queryForList(sql.toString(), params.toArray());
    }
}
