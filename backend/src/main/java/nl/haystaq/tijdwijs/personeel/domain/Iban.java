package nl.haystaq.tijdwijs.personeel.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;

import java.math.BigInteger;
import java.util.Locale;
import java.util.regex.Pattern;

/** IBAN met mod-97-controle volgens ISO 13616. */
public record Iban(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[A-Z]{2}\\d{2}[A-Z0-9]{10,30}$");

    public Iban {
        BusinessRuleViolation.require(value != null, "iban.missing");
        value = value.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        BusinessRuleViolation.require(PATTERN.matcher(value).matches(), "iban.format");
        BusinessRuleViolation.require(!value.startsWith("NL") || value.length() == 18, "iban.nl_length");
        BusinessRuleViolation.require(mod97(value) == 1, "iban.mod97");
    }

    private static int mod97(String iban) {
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder digits = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            digits.append(Character.isDigit(c) ? String.valueOf(c) : String.valueOf(c - 'A' + 10));
        }
        return new BigInteger(digits.toString()).mod(BigInteger.valueOf(97)).intValue();
    }

    @Override
    public String toString() {
        return value;
    }

    @Converter(autoApply = true)
    public static class JpaConverter implements AttributeConverter<Iban, String> {
        @Override
        public String convertToDatabaseColumn(Iban attribute) {
            return attribute == null ? null : attribute.value();
        }

        @Override
        public Iban convertToEntityAttribute(String dbData) {
            return dbData == null ? null : new Iban(dbData);
        }
    }
}
