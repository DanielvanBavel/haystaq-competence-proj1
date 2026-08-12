package nl.haystaq.tijdwijs.declaraties.infrastructure;

import nl.haystaq.tijdwijs.declaraties.domain.ExpenseClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExpenseClaimSpringDataRepository extends JpaRepository<ExpenseClaim, UUID> {

    List<ExpenseClaim> findAllByOrderByExpenseDateDesc();

    List<ExpenseClaim> findByEmployeeIdOrderByExpenseDateDesc(UUID employeeId);
}
