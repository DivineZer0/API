package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import plantime.ru.API.entity.TypeAbsence;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для доступа к данным типов отсутствия сотрудников.
 * Предоставляет методы для поиска, сортировки и проверки уникальности названий типов отсутствия.
 */
public interface TypeAbsenceRepository extends JpaRepository<TypeAbsence, Integer> {

    /**
     * Проверяет существование типа отсутствия с указанным названием.
     *
     * @param typeOfAbsence Название типа отсутствия.
     * @return true, если тип отсутствия с таким названием уже существует.
     */
    boolean existsByTypeOfAbsenceIgnoreCase(String typeOfAbsence);

    /**
     * Получает все типы отсутствия с указанной сортировкой.
     *
     * @param sort Параметры сортировки.
     * @return Список всех типов отсутствия.
     */
    List<TypeAbsence> findAll(Sort sort);

    /**
     * Ищет тип отсутствия по названию.
     *
     * @param typeOfAbsence Название типа отсутствия.
     * @return Optional с найденным типом отсутствия или пустой Optional, если не найден.
     */
    Optional<TypeAbsence> findByTypeOfAbsenceIgnoreCase(String typeOfAbsence);
}