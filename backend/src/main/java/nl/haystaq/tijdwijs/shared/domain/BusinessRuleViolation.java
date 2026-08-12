package nl.haystaq.tijdwijs.shared.domain;

/**
 * De enige uitzondering die het domein gooit.
 * <p>
 * De {@code code} beschrijft precies welke regel is overtreden, maar die code
 * verlaat de applicatie nooit: de REST-laag vertaalt hem naar een generieke
 * melding. Dat is een bewuste eigenschap van deze applicatie.
 */
public class BusinessRuleViolation extends RuntimeException {

    public enum Kind {
        /** Waarde voldoet niet aan het formaat of de grenzen. -> HTTP 400 */
        INVALID_INPUT,
        /** Waarde klopt op zichzelf, maar mag niet in deze toestand. -> HTTP 409 */
        CONFLICT,
        /** Verwezen aggregate bestaat niet. -> HTTP 404 */
        NOT_FOUND
    }

    private final Kind kind;
    private final String code;

    private BusinessRuleViolation(Kind kind, String code) {
        super(code);
        this.kind = kind;
        this.code = code;
    }

    public static BusinessRuleViolation invalid(String code) {
        return new BusinessRuleViolation(Kind.INVALID_INPUT, code);
    }

    public static BusinessRuleViolation conflict(String code) {
        return new BusinessRuleViolation(Kind.CONFLICT, code);
    }

    public static BusinessRuleViolation notFound(String code) {
        return new BusinessRuleViolation(Kind.NOT_FOUND, code);
    }

    public static void require(boolean condition, String code) {
        if (!condition) {
            throw invalid(code);
        }
    }

    public static void requireState(boolean condition, String code) {
        if (!condition) {
            throw conflict(code);
        }
    }

    public Kind kind() {
        return kind;
    }

    public String code() {
        return code;
    }
}
