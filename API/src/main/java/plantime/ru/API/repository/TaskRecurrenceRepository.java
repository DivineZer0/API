package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import plantime.ru.API.entity.TaskRecurrence;
import java.util.List;

public interface TaskRecurrenceRepository extends JpaRepository<TaskRecurrence, Integer> {

    // Проверка существования шаблона (без учета регистра)
    boolean existsByRecurrencePatternIgnoreCase(String pattern);

    // Проверка существования другого шаблона с таким же названием (для обновления)
    boolean existsByRecurrencePatternIgnoreCaseAndIdTaskRecurrenceNot(String pattern, Integer id);

    // Получение всех записей с сортировкой
    List<TaskRecurrence> findAll(Sort sort);
}