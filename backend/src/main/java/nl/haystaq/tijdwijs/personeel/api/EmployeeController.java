package nl.haystaq.tijdwijs.personeel.api;

import nl.haystaq.tijdwijs.personeel.application.EmployeeService;
import nl.haystaq.tijdwijs.personeel.application.EmployeeView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employees;

    public EmployeeController(EmployeeService employees) {
        this.employees = employees;
    }

    @GetMapping
    public List<EmployeeView> list() {
        return employees.findAll();
    }

    @GetMapping("/{id}")
    public EmployeeView byId(@PathVariable UUID id) {
        return employees.findById(id);
    }

    @PostMapping
    public ResponseEntity<EmployeeView> register(@RequestBody EmployeeService.RegisterEmployee command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employees.register(command));
    }

    @PatchMapping("/{id}")
    public EmployeeView update(@PathVariable UUID id, @RequestBody EmployeeService.UpdateEmployee command) {
        return employees.update(id, command);
    }
}
