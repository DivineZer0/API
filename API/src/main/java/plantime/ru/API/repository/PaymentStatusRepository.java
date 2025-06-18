package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import plantime.ru.API.entity.PaymentStatus;

import java.util.List;

/**
 * Репозиторий для работы с сущностью PaymentStatus.
 * Предоставляет методы для поиска, сортировки и проверки уникальности статусов оплат.
 */
public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Integer> {

    /**
     * Проверяет существование статуса оплаты с указанным названием.
     *
     * @param status Название статуса оплаты.
     * @return true, если статус оплаты существует, иначе false.
     */
    boolean existsByStatusIgnoreCase(String status);

    /**
     * Получает все статусы оплат с указанной сортировкой.
     *
     * @param sort Параметры сортировки.
     * @return Список всех статусов оплат.
     */
    List<PaymentStatus> findAll(Sort sort);
}