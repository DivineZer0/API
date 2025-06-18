package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import plantime.ru.API.entity.EmployeePost;
import plantime.ru.API.entity.EmployeePermission;

import java.util.List;

/**
 * Репозиторий для работы с сущностью EmployeePost.
 * Предоставляет методы для поиска, сортировки и проверки уникальности названий должностей.
 */
public interface EmployeePostRepository extends JpaRepository<EmployeePost, Integer> {

    /**
     * Проверяет существование должности с указанным названием.
     *
     * @param post Название должности.
     * @return true, если должность существует, иначе false.
     */
    boolean existsByPostIgnoreCase(String post);

    /**
     * Получает все должности с указанной сортировкой.
     *
     * @param sort Параметры сортировки.
     * @return Список всех должностей.
     */
    List<EmployeePost> findAll(Sort sort);

    /**
     * Получает все должности, связанные с указанным уровнем прав доступа, с указанной сортировкой.
     *
     * @param idEmployeePermission Идентификатор уровня прав доступа.
     * @param sort Параметры сортировки.
     * @return Список должностей, связанных с указанным уровнем прав доступа.
     */
    List<EmployeePost> findByEmployeePermissionIdEmployeePermission(Integer idEmployeePermission, Sort sort);

    /**
     * Проверяет, используется ли должность сотрудниками.
     *
     * @param permission Сущность уровня прав доступа.
     * @return true, если существует хотя бы один сотрудник с такой должностью.
     */
    boolean existsByEmployeePermission(EmployeePermission permission);
}