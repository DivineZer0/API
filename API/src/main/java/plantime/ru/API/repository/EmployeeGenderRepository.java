package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import plantime.ru.API.entity.EmployeeGender;

import java.util.List;

/**
 * Репозиторий для доступа к данным гендеров сотрудников.
 * Предоставляет методы для поиска, сортировки и проверки уникальности названий гендеров.
 */
public interface EmployeeGenderRepository extends JpaRepository<EmployeeGender, Integer> {

    /**
     * Проверяет существование гендера с указанным названием.
     *
     * @param gender Название гендера.
     * @return true, если гендер с таким названием уже существует.
     */
    boolean existsByGenderIgnoreCase(String gender);

    /**
     * Получает все гендеры с указанной сортировкой.
     *
     * @param sort Параметры сортировки.
     * @return Список всех гендеров.
     */
    List<EmployeeGender> findAll(Sort sort);
}