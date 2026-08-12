package nl.haystaq.tijdwijs.projecten.api;

import nl.haystaq.tijdwijs.projecten.application.ClientView;
import nl.haystaq.tijdwijs.projecten.application.ProjectView;
import nl.haystaq.tijdwijs.projecten.application.ProjectenService;
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
@RequestMapping("/api")
public class ProjectController {

    private final ProjectenService projecten;

    public ProjectController(ProjectenService projecten) {
        this.projecten = projecten;
    }

    @GetMapping("/clients")
    public List<ClientView> clients() {
        return projecten.findAllClients();
    }

    @PostMapping("/clients")
    public ResponseEntity<ClientView> registerClient(@RequestBody ProjectenService.RegisterClient command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projecten.registerClient(command));
    }

    @GetMapping("/projects")
    public List<ProjectView> projects() {
        return projecten.findAll();
    }

    @GetMapping("/projects/{id}")
    public ProjectView project(@PathVariable UUID id) {
        return projecten.findById(id);
    }

    @PostMapping("/projects")
    public ResponseEntity<ProjectView> start(@RequestBody ProjectenService.StartProject command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projecten.start(command));
    }

    @PatchMapping("/projects/{id}")
    public ProjectView change(@PathVariable UUID id, @RequestBody ProjectenService.ChangeProject command) {
        return projecten.change(id, command);
    }

    @PostMapping("/projects/{id}/tasks")
    public ResponseEntity<ProjectView> addTask(@PathVariable UUID id,
                                               @RequestBody ProjectenService.AddTask command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projecten.addTask(id, command));
    }

    @PostMapping("/projects/{id}/members")
    public ResponseEntity<ProjectView> assignMember(@PathVariable UUID id,
                                                    @RequestBody ProjectenService.AssignMember command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projecten.assignMember(id, command));
    }
}
