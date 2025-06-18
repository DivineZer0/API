package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.entity.PasswordResetToken;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link PasswordResetToken} в базе данных.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {

    /**
     * Находит токен сброса пароля по его значению.
     *
     * @param token Уникальный токен сброса пароля.
     * @return {@link Optional} с найденным токеном или пустой, если токен не найден.
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Удаляет все токены сброса пароля, связанные с указанным сотрудником.
     *
     * @param employee Уникальный идентификатор сотрудника.
     */
    void deleteByEmployee(Employee employee);
}