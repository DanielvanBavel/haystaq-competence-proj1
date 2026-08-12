package nl.haystaq.tijdwijs.declaraties.api;

import nl.haystaq.tijdwijs.declaraties.application.ExpenseClaimService;
import nl.haystaq.tijdwijs.declaraties.application.ExpenseClaimView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/expenses")
public class ExpenseClaimController {

    private final ExpenseClaimService claims;

    public ExpenseClaimController(ExpenseClaimService claims) {
        this.claims = claims;
    }

    @GetMapping
    public List<ExpenseClaimView> list(@RequestParam(required = false) UUID employeeId) {
        return claims.findAll(employeeId);
    }

    @PostMapping
    public ResponseEntity<ExpenseClaimView> file(@RequestBody ExpenseClaimService.FileClaim command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claims.file(command));
    }

    @PostMapping("/{id}/transitions")
    public ExpenseClaimView decide(@PathVariable UUID id, @RequestBody ExpenseClaimService.DecideClaim command) {
        return claims.decide(id, command);
    }
}
