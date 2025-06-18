package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import plantime.ru.API.entity.EmployeePermission;

import java.util.List;

/**
 * Репозиторий для доступа к данным уровней прав доступа сотрудников.
 * Предоставляет методы для поиска, сортировки и проверки уникальности названий уровней прав.
 */
public interface EmployeePermissionRepository extends JpaRepository<EmployeePermission, Integer> {

    /**
     * Проверяет существование уровня прав доступа с указанным названием.
     *
     * @param permission Название уровня прав доступа.
     * @return true, если уровень прав с таким названием уже существует.
     */
    boolean existsByPermissionIgnoreCase(String permission);

    /**
     * Получает все уровни прав доступа с указанной сортировкой.
     *
     * @param sort Параметры сортировки.
     * @return Список всех уровней прав доступа.
     */
    List<EmployeePermission> findAll(Sort sort);
}