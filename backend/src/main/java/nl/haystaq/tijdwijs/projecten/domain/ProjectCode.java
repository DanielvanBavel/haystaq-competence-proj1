package nl.haystaq.tijdwijs.projecten.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Projectcode in de vorm {@code PRJ-2026-001}. */
public record ProjectCode(String value) {

    private static final Pattern PATTERN = Pattern.compile("^PRJ-(\\d{4})-\\d{3}$");

    public ProjectCode {
        BusinessRuleViolation.require(value != null && PATTERN.matcher(value).matches(), "code.format");
    }

    /** Het jaartal in de code hoort bij het jaar waarin het project start. */
    public int year() {
        Matcher matcher = PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw BusinessRuleViolation.invalid("code.format");
        }
        return Integer.parseInt(matcher.group(1));
    }

    @Override
    public String toString() {
        return value;
    }

    @Converter(autoApply = true)
    public static class JpaConverter implements AttributeConverter<ProjectCode, String> {
        @Override
        public String convertToDatabaseColumn(ProjectCode attribute) {
            return attribute == null ? null : attribute.value();
        }

        @Override
        public ProjectCode convertToEntityAttribute(String dbData) {
            return dbData == null ? null : new ProjectCode(dbData);
        }
    }
}
