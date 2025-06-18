package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import plantime.ru.API.entity.EmployeeDepartment;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для доступа к данным отделов сотрудников.
 * Предоставляет методы для поиска, сортировки и проверки уникальности названий отделов.
 */
public interface EmployeeDepartmentRepository extends JpaRepository<EmployeeDepartment, Integer> {

    /**
     * Проверяет существование отдела с указанным названием.
     *
     * @param department Название отдела.
     * @return true, если отдел с таким названием уже существует.
     */
    boolean existsByDepartmentIgnoreCase(String department);

    /**
     * Получает все отделы с указанной сортировкой.
     *
     * @param sort Параметры сортировки.
     * @return Список всех отделов.
     */
    List<EmployeeDepartment> findAll(Sort sort);

    /**
     * Получает отдел по наименованию.
     *
     * @param department Название отдела.
     * @return Отдел.
     */
    Optional<EmployeeDepartment> findByDepartment(String department);
}