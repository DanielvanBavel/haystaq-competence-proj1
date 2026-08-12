package nl.haystaq.tijdwijs.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Objects;

/** Een ISO-8601 week: week 1 is de week waarin 4 januari valt. */
@Embeddable
public class IsoWeek {

    private static final WeekFields ISO = WeekFields.ISO;

    @Column(name = "iso_year", nullable = false)
    private short year;

    @Column(name = "iso_week", nullable = false)
    private short week;

    protected IsoWeek() {
        // voor JPA
    }

    public IsoWeek(int year, int week) {
        BusinessRuleViolation.require(year >= 2000 && year <= 2100, "iso_year.range");
        BusinessRuleViolation.require(week >= 1 && week <= 53, "iso_week.range");
        BusinessRuleViolation.require(week <= weeksInYear(year), "iso_week.not_in_year");
        this.year = (short) year;
        this.week = (short) week;
    }

    public static IsoWeek of(LocalDate date) {
        return new IsoWeek(date.get(ISO.weekBasedYear()), date.get(ISO.weekOfWeekBasedYear()));
    }

    private static int weeksInYear(int year) {
        return LocalDate.of(year, 12, 28).get(ISO.weekOfWeekBasedYear());
    }

    public LocalDate firstDay() {
        return LocalDate.of(year, 1, 4)
                .with(ISO.weekOfWeekBasedYear(), week)
                .with(DayOfWeek.MONDAY);
    }

    public LocalDate lastDay() {
        return firstDay().plusDays(6);
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(firstDay()) && !date.isAfter(lastDay());
    }

    public int year() {
        return year;
    }

    public int week() {
        return week;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IsoWeek other)) {
            return false;
        }
        return year == other.year && week == other.week;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, week);
    }

    @Override
    public String toString() {
        return "%d-W%02d".formatted(year, week);
    }
}
