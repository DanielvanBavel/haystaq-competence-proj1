package nl.haystaq.tijdwijs.declaraties.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseClaimRepository {

    ExpenseClaim save(ExpenseClaim claim);

    Optional<ExpenseClaim> findById(UUID id);

    List<ExpenseClaim> findAll();

    List<ExpenseClaim> findByEmployee(UUID employeeId);
}
