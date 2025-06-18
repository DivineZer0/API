package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import plantime.ru.API.entity.TaskStatus;

import java.util.List;

/**
 * Репозиторий для работы с сущностью TaskStatus.
 * Предоставляет методы для поиска, сортировки и проверки уникальности статусов задач.
 */
public interface TaskStatusRepository extends JpaRepository<TaskStatus, Integer> {

    /**
     * Проверяет существование статуса задачи с указанным названием.
     *
     * @param status Название статуса задачи.
     * @return true, если статус существует, иначе false.
     */
    boolean existsByStatusIgnoreCase(String status);

    /**
     * Получает все статусы задач с указанной сортировкой.
     *
     * @param sort Параметры сортировки.
     * @return Список всех статусов задач.
     */
    List<TaskStatus> findAll(Sort sort);
}