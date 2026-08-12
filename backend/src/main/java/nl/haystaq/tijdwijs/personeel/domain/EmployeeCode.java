package nl.haystaq.tijdwijs.personeel.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;

import java.util.regex.Pattern;

/** Personeelsnummer, bijvoorbeeld {@code EMP-0042}. */
public record EmployeeCode(String value) {

    private static final Pattern PATTERN = Pattern.compile("^EMP-\\d{4}$");

    public EmployeeCode {
        BusinessRuleViolation.require(value != null && PATTERN.matcher(value).matches(), "employee_code.format");
    }

    @Override
    public String toString() {
        return value;
    }

    @Converter(autoApply = true)
    public static class JpaConverter implements AttributeConverter<EmployeeCode, String> {
        @Override
        public String convertToDatabaseColumn(EmployeeCode attribute) {
            return attribute == null ? null : attribute.value();
        }

        @Override
        public EmployeeCode convertToEntityAttribute(String dbData) {
            return dbData == null ? null : new EmployeeCode(dbData);
        }
    }
}
