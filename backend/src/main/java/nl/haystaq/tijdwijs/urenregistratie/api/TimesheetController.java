package nl.haystaq.tijdwijs.urenregistratie.api;

import nl.haystaq.tijdwijs.urenregistratie.application.AbsenceView;
import nl.haystaq.tijdwijs.urenregistratie.application.TimesheetService;
import nl.haystaq.tijdwijs.urenregistratie.application.TimesheetView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TimesheetController {

    private final TimesheetService timesheets;

    public TimesheetController(TimesheetService timesheets) {
        this.timesheets = timesheets;
    }

    @GetMapping("/timesheets")
    public List<TimesheetView> list(@RequestParam(required = false) UUID employeeId) {
        return timesheets.findAll(employeeId);
    }

    @GetMapping("/timesheets/{id}")
    public TimesheetView byId(@PathVariable UUID id) {
        return timesheets.findById(id);
    }

    @PostMapping("/timesheets")
    public ResponseEntity<TimesheetView> open(@RequestBody TimesheetService.OpenTimesheet command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timesheets.open(command));
    }

    @PostMapping("/timesheets/{id}/entries")
    public ResponseEntity<TimesheetView> book(@PathVariable UUID id,
                                              @RequestBody TimesheetService.BookTime command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timesheets.book(id, command));
    }

    @DeleteMapping("/time-entries/{entryId}")
    public ResponseEntity<Void> removeEntry(@PathVariable UUID entryId) {
        timesheets.removeEntry(entryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/timesheets/{id}/submit")
    public TimesheetView submit(@PathVariable UUID id,
                                @RequestBody(required = false) TimesheetService.SubmitTimesheet command) {
        return timesheets.submit(id, command);
    }

    @PostMapping("/timesheets/{id}/approve")
    public TimesheetView approve(@PathVariable UUID id,
                                 @RequestBody TimesheetService.ApproveTimesheet command) {
        return timesheets.approve(id, command);
    }

    @PostMapping("/timesheets/{id}/reject")
    public TimesheetView reject(@PathVariable UUID id,
                                @RequestBody TimesheetService.SubmitTimesheet command) {
        return timesheets.reject(id, command);
    }

    @GetMapping("/absences")
    public List<AbsenceView> absences(@RequestParam(required = false) UUID employeeId) {
        return timesheets.findAbsences(employeeId);
    }

    @PostMapping("/absences")
    public ResponseEntity<AbsenceView> requestAbsence(@RequestBody TimesheetService.RequestAbsence command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timesheets.requestAbsence(command));
    }

    @DeleteMapping("/absences/{id}")
    public ResponseEntity<Void> deleteAbsence(@PathVariable UUID id) {
        timesheets.deleteAbsence(id);
        return ResponseEntity.noContent().build();
    }
}
