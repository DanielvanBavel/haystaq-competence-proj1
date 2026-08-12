package nl.haystaq.tijdwijs.shared.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Uren, altijd in stappen van een kwartier. */
public record Hours(BigDecimal value) implements Comparable<Hours> {

    private static final BigDecimal QUARTER = new BigDecimal("0.25");

    public Hours {
        BusinessRuleViolation.require(value != null, "hours.missing");
        BusinessRuleViolation.require(value.signum() > 0, "hours.positive");
        BusinessRuleViolation.require(value.remainder(QUARTER).compareTo(BigDecimal.ZERO) == 0, "hours.step");
        value = value.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static Hours of(String value) {
        return new Hours(new BigDecimal(value));
    }

    public Hours plus(Hours other) {
        return new Hours(value.add(other.value));
    }

    public boolean isGreaterThan(Hours other) {
        return value.compareTo(other.value) > 0;
    }

    @Override
    public int compareTo(Hours other) {
        return value.compareTo(other.value);
    }

    @Converter(autoApply = true)
    public static class JpaConverter implements AttributeConverter<Hours, BigDecimal> {
        @Override
        public BigDecimal convertToDatabaseColumn(Hours attribute) {
            return attribute == null ? null : attribute.value();
        }

        @Override
        public Hours convertToEntityAttribute(BigDecimal dbData) {
            return dbData == null ? null : new Hours(dbData);
        }
    }
}
