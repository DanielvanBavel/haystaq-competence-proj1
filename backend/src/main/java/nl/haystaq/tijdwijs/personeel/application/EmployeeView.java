package nl.haystaq.tijdwijs.personeel.application;

import nl.haystaq.tijdwijs.personeel.domain.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Leesmodel. Het domeinobject verlaat de applicatielaag niet. */
public record EmployeeView(
        UUID id,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        LocalDate birthDate,
        LocalDate hireDate,
        LocalDate endDate,
        String contractType,
        BigDecimal contractHours,
        BigDecimal hourlyRate,
        String iban,
        String phone,
        UUID managerId,
        boolean active) {

    public static EmployeeView from(Employee employee) {
        return new EmployeeView(
                employee.id(),
                employee.employeeCode().value(),
                employee.firstName(),
                employee.lastName(),
                employee.email().value(),
                employee.birthDate(),
                employee.employmentPeriod().hireDate(),
                employee.employmentPeriod().endDate(),
                employee.contractType().name(),
                employee.contractHours().value(),
                employee.hourlyRate().amount(),
                employee.iban().value(),
                employee.phone(),
                employee.managerId(),
                employee.isActive());
    }
}
