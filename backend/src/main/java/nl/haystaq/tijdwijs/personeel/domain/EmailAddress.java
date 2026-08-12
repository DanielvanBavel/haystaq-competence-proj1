package nl.haystaq.tijdwijs.personeel.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;

import java.util.Locale;
import java.util.regex.Pattern;

public record EmailAddress(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s.]+\\.[^@\\s]{2,}$");

    public EmailAddress {
        BusinessRuleViolation.require(value != null && value.length() <= 160, "email.length");
        BusinessRuleViolation.require(PATTERN.matcher(value).matches(), "email.format");
        value = value.toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return value;
    }

    @Converter(autoApply = true)
    public static class JpaConverter implements AttributeConverter<EmailAddress, String> {
        @Override
        public String convertToDatabaseColumn(EmailAddress attribute) {
            return attribute == null ? null : attribute.value();
        }

        @Override
        public EmailAddress convertToEntityAttribute(String dbData) {
            return dbData == null ? null : new EmailAddress(dbData);
        }
    }
}
