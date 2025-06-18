package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.entity.Session;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Session} в базе данных.
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, String> {

    /**
     * Находит сессию по значению JWT-токена.
     *
     * @param token Уникальный JWT-токен сессии.
     * @return {@link Optional} с найденной сессией или пустой, если сессия не найдена.
     */
    Optional<Session> findByToken(String token);

    /**
     * Удаляет все сессии, связанные с указанным сотрудником.
     *
     * @param employee Сущность сотрудника.
     */
    void deleteByEmployee(Employee employee);
}