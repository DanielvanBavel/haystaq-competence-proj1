package nl.haystaq.tijdwijs.shared.api;

import org.flywaydb.core.Flyway;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Zet de database terug naar de minimale seed. Handig als een gegenereerde
 * dataset de omgeving onbruikbaar heeft gemaakt.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final Flyway flyway;

    public AdminController(Flyway flyway) {
        this.flyway = flyway;
    }

    @PostMapping("/reset")
    public Map<String, Object> reset() {
        flyway.clean();
        var result = flyway.migrate();
        return Map.of("status", "reset", "migrationsApplied", result.migrationsExecuted);
    }
}
