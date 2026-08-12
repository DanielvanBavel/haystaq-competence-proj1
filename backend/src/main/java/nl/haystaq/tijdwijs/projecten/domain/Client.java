package nl.haystaq.tijdwijs.projecten.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/** Aggregate root: de opdrachtgever waarvoor projecten worden uitgevoerd. */
@Entity
@Table(name = "client")
public class Client {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s.]+\\.[^@\\s]{2,}$");
    private static final Pattern VAT = Pattern.compile("^[A-Z]{2}[0-9A-Z]{9,12}$");
    private static final Pattern COUNTRY = Pattern.compile("^[A-Z]{2}$");

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "vat_number")
    private String vatNumber;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "payment_term_days", nullable = false)
    private short paymentTermDays;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Client() {
        // voor JPA
    }

    public static Client register(String name, String contactEmail, String vatNumber, String country,
                                  Integer paymentTermDays, Boolean active) {
        BusinessRuleViolation.require(name != null && !name.isBlank() && name.trim().length() <= 120, "name");
        BusinessRuleViolation.require(contactEmail == null || EMAIL.matcher(contactEmail).matches(), "contact_email");
        BusinessRuleViolation.require(vatNumber == null || VAT.matcher(vatNumber).matches(), "vat_number");
        String resolvedCountry = country == null ? "NL" : country.toUpperCase(Locale.ROOT);
        BusinessRuleViolation.require(COUNTRY.matcher(resolvedCountry).matches(), "country");
        int term = paymentTermDays == null ? 30 : paymentTermDays;
        BusinessRuleViolation.require(term >= 0 && term <= 120, "payment_term_days");

        Client client = new Client();
        client.id = UUID.randomUUID();
        client.name = name.trim();
        client.contactEmail = contactEmail;
        client.vatNumber = vatNumber;
        client.country = resolvedCountry;
        client.paymentTermDays = (short) term;
        client.active = active == null || active;
        return client;
    }

    public void deactivate() {
        this.active = false;
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String contactEmail() {
        return contactEmail;
    }

    public String vatNumber() {
        return vatNumber;
    }

    public String country() {
        return country;
    }

    public short paymentTermDays() {
        return paymentTermDays;
    }

    public boolean isActive() {
        return active;
    }
}
