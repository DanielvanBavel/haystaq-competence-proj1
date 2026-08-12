package nl.haystaq.tijdwijs.declaraties.application;

import nl.haystaq.tijdwijs.declaraties.domain.ExpenseClaim;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseClaimView(
        UUID id,
        UUID employeeId,
        String employeeCode,
        UUID projectId,
        String category,
        BigDecimal amount,
        String currency,
        BigDecimal vatRate,
        LocalDate expenseDate,
        String receiptReference,
        String description,
        String status) {

    public static ExpenseClaimView from(ExpenseClaim claim, String employeeCode) {
        return new ExpenseClaimView(
                claim.id(),
                claim.employeeId(),
                employeeCode,
                claim.projectId(),
                claim.category().name(),
                claim.amount().amount(),
                claim.currency(),
                claim.vatRate(),
                claim.expenseDate(),
                claim.receiptReference(),
                claim.description(),
                claim.status().name());
    }
}
