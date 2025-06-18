package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import plantime.ru.API.entity.Log;

/**
 * Репозиторий для работы с сущностью {@link Log} в базе данных.
 */
public interface LogRepository extends JpaRepository<Log, Integer> {
}