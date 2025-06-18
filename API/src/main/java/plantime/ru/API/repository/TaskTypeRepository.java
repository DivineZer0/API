package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import plantime.ru.API.entity.TaskType;

import java.util.List;

/**
 * Репозиторий для работы с сущностью TaskType.
 * Предоставляет методы для поиска, сортировки и проверки уникальности названий типов задач.
 */
public interface TaskTypeRepository extends JpaRepository<TaskType, Integer> {

    /**
     * Проверяет существование типа задачи с указанным названием.
     *
     * @param type Название типа задачи.
     * @return true, если тип задачи существует, иначе false.
     */
    boolean existsByTypeIgnoreCase(String type);

    /**
     * Получает все типы задач с указанной сортировкой.
     *
     * @param sort Параметры сортировки.
     * @return Список всех типов задач.
     */
    List<TaskType> findAll(Sort sort);
}