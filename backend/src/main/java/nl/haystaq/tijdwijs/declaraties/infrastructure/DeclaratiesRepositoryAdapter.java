package nl.haystaq.tijdwijs.declaraties.infrastructure;

import nl.haystaq.tijdwijs.declaraties.domain.ExpenseClaim;
import nl.haystaq.tijdwijs.declaraties.domain.ExpenseClaimRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Configuration
public class DeclaratiesRepositoryAdapter {

    @Bean
    ExpenseClaimRepository expenseClaimRepository(ExpenseClaimSpringDataRepository delegate) {
        return new ExpenseClaimRepository() {
            @Override
            public ExpenseClaim save(ExpenseClaim claim) {
                return delegate.save(claim);
            }

            @Override
            public Optional<ExpenseClaim> findById(UUID id) {
                return delegate.findById(id);
            }

            @Override
            public List<ExpenseClaim> findAll() {
                return delegate.findAllByOrderByExpenseDateDesc();
            }

            @Override
            public List<ExpenseClaim> findByEmployee(UUID employeeId) {
                return delegate.findByEmployeeIdOrderByExpenseDateDesc(employeeId);
            }
        };
    }
}
