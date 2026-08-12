package nl.haystaq.tijdwijs.shared.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Bedrag in hele centen. Negatieve bedragen bestaan niet in dit domein. */
public record Money(BigDecimal amount) {

    public static final Money ZERO = new Money(BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY));

    public Money {
        BusinessRuleViolation.require(amount != null, "money.missing");
        BusinessRuleViolation.require(amount.scale() <= 2, "money.scale");
        BusinessRuleViolation.require(amount.signum() >= 0, "money.negative");
        amount = amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static Money of(BigDecimal value) {
        BusinessRuleViolation.require(value != null, "money.missing");
        return new Money(value.setScale(2, RoundingMode.HALF_UP));
    }

    public boolean isGreaterThan(Money other) {
        return amount.compareTo(other.amount) > 0;
    }

    @Converter(autoApply = true)
    public static class JpaConverter implements AttributeConverter<Money, BigDecimal> {
        @Override
        public BigDecimal convertToDatabaseColumn(Money attribute) {
            return attribute == null ? null : attribute.amount();
        }

        @Override
        public Money convertToEntityAttribute(BigDecimal dbData) {
            return dbData == null ? null : new Money(dbData);
        }
    }
}
