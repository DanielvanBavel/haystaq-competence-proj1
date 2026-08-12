package nl.haystaq.tijdwijs.shared.api;

import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.UUID;

/**
 * Vertaalt domeinfouten naar HTTP.
 * <p>
 * Bewust ontworpen zoals veel productiesystemen het doen: de client krijgt
 * alleen een categorie te zien, niet welk veld of welke regel het probleem is.
 * Zet {@code tijdwijs.debug-rules=true} om de interne codes in de logs te zien.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    private final boolean debugRules;

    public RestExceptionHandler(@Value("${tijdwijs.debug-rules:false}") boolean debugRules) {
        this.debugRules = debugRules;
    }

    @ExceptionHandler(BusinessRuleViolation.class)
    public ResponseEntity<Map<String, String>> handleDomain(BusinessRuleViolation exception) {
        if (debugRules) {
            log.warn("rejected: kind={} code={}", exception.kind(), exception.code());
        }
        return switch (exception.kind()) {
            case INVALID_INPUT -> ResponseEntity.badRequest().body(Map.of("error", "invalid input"));
            case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "conflict"));
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "not found"));
        };
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception exception) {
        if (debugRules) {
            log.warn("rejected: {}", exception.getMessage());
        }
        return ResponseEntity.badRequest().body(Map.of("error", "invalid input"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception exception) {
        String reference = UUID.randomUUID().toString().substring(0, 8);
        log.error("unexpected failure ref={} ", reference, exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "internal error", "ref", reference));
    }
}
