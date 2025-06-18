package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import plantime.ru.API.entity.ProjectStatus;

import java.util.List;

/**
 * Репозиторий для работы с сущностью ProjectStatus.
 * Предоставляет методы для поиска, сортировки и проверки уникальности статусов проектов.
 */
public interface ProjectStatusRepository extends JpaRepository<ProjectStatus, Integer> {

    /**
     * Проверяет существование статуса проекта с указанным названием.
     *
     * @param status Название статуса проекта.
     * @return true, если статус существует, иначе false.
     */
    boolean existsByStatusIgnoreCase(String status);

    /**
     * Получает все статусы проектов с указанной сортировкой.
     *
     * @param sort Параметры сортировки.
     * @return Список всех статусов проектов.
     */
    List<ProjectStatus> findAll(Sort sort);
}