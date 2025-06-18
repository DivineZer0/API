package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import plantime.ru.API.entity.Contract;
import plantime.ru.API.entity.PaymentStatus;

public interface ContractRepository extends JpaRepository<Contract, Integer> {
    boolean existsByPaymentStatus(PaymentStatus paymentStatus);
}