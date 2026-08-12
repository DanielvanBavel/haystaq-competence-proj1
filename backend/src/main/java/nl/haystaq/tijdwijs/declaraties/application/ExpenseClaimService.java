package nl.haystaq.tijdwijs.declaraties.application;

import nl.haystaq.tijdwijs.declaraties.domain.EmployeeLookup;
import nl.haystaq.tijdwijs.declaraties.domain.ExpenseClaim;
import nl.haystaq.tijdwijs.declaraties.domain.ExpenseClaimRepository;
import nl.haystaq.tijdwijs.shared.domain.BusinessRuleViolation;
import nl.haystaq.tijdwijs.shared.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ExpenseClaimService {

    private final ExpenseClaimRepository claims;
    private final EmployeeLookup employees;

    public ExpenseClaimService(ExpenseClaimRepository claims, EmployeeLookup employees) {
        this.claims = claims;
        this.employees = employees;
    }

    public record FileClaim(UUID employeeId, UUID projectId, String category, BigDecimal amount, String currency,
                            BigDecimal vatRate, LocalDate expenseDate, String receiptReference, String description) {
    }

    public record DecideClaim(String action) {
    }

    public ExpenseClaimView file(FileClaim command) {
        String employeeCode = employees.employeeCode(command.employeeId())
                .orElseThrow(() -> BusinessRuleViolation.invalid("employee.missing"));
        BusinessRuleViolation.require(command.amount() != null, "amount.missing");

        ExpenseClaim claim = ExpenseClaim.file(
                command.employeeId(),
                command.projectId(),
                ExpenseClaim.Category.parse(command.category()),
                Money.of(command.amount()),
                command.currency(),
                command.vatRate(),
                command.expenseDate(),
                command.receiptReference(),
                command.description());

        return ExpenseClaimView.from(claims.save(claim), employeeCode);
    }

    public ExpenseClaimView decide(UUID id, DecideClaim command) {
        ExpenseClaim claim = claims.findById(id)
                .orElseThrow(() -> BusinessRuleViolation.notFound("claim.missing"));
        String action = command == null || command.action() == null ? "" : command.action().toLowerCase();
        switch (action) {
            case "submit" -> claim.submit();
            case "approve" -> claim.decide(true);
            case "reject" -> claim.decide(false);
            case "pay" -> claim.markPaid();
            default -> throw BusinessRuleViolation.invalid("action.unknown");
        }
        return view(claims.save(claim));
    }

    @Transactional(readOnly = true)
    public List<ExpenseClaimView> findAll(UUID employeeId) {
        List<ExpenseClaim> found = employeeId == null ? claims.findAll() : claims.findByEmployee(employeeId);
        return found.stream().map(this::view).toList();
    }

    private ExpenseClaimView view(ExpenseClaim claim) {
        return ExpenseClaimView.from(claim, employees.employeeCode(claim.employeeId()).orElse(null));
    }
}
