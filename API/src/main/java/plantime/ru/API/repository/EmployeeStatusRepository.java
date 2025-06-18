package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import plantime.ru.API.entity.EmployeeStatus;

import java.util.List;

/**
 * Репозиторий для доступа к данным статусов сотрудников.
 * Предоставляет методы для поиска, сортировки и проверки уникальности названий статусов.
 */
public interface EmployeeStatusRepository extends JpaRepository<EmployeeStatus, Integer> {

    /**
     * Проверяет существование статуса с указанным названием.
     *
     * @param status Название статуса.
     * @return true, если статус с таким названием уже существует.
     */
    boolean existsByStatusIgnoreCase(String status);

    /**
     * Получает все статусы с указанной сортировкой.
     *
     * @param sort Параметры сортировки.
     * @return Список всех статусов.
     */
    List<EmployeeStatus> findAll(Sort sort);
}