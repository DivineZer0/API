package plantime.ru.API.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO для ответа на запрос входа в систему.
 */
public class LoginResponse {

    /**
     * JWT-токен для авторизации.
     */
    @NotNull(message = "Токен не может быть пустым")
    private final String token;

    /**
     * Дата и время истечения срока действия сессии.
     */
    @NotNull(message = "Дата истечения обязательна")
    private LocalDateTime expiresAt;

    /**
     * Уровень прав доступа сотрудника, определяемая из EmployeePermission.
     */
    @NotNull(message = "Уровень прав доступа обязателен")
    private final String role;

    /**
     * Ссылка на фотографию профиля сотрудника.
     */
    @NotNull(message = "Ссылка на фотографию обязательна")
    private final String profilePicture;


    /**
     * Конструктор для создания ответа с JWT-токеном.
     *
     * @param token        JWT-токен.
     * @param expiresAt      Дата и время истечения срока действия сессии.
     * @param role           Уровень прав доступа сотрудника.
     * @param profilePicture Ссылка на фотографию профиля.
     */
    public LoginResponse(String token, LocalDateTime expiresAt, String role, String profilePicture) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.role = role;
        this.profilePicture = profilePicture;
    }

    /**
     * Возвращает JWT-токен.
     *
     * @return JWT-токен.
     */
    public String getToken() {
        return token;
    }

    /**
     * Возвращает дату и время истечения срока действия сессии.
     *
     * @return Дата и время истечения срока действия сессии.
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Возвращает уровень прав доступа сотрудника.
     *
     * @return Уровень прав доступа.
     */
    public String getRole() {
        return role;
    }

    /**
     * Возвращает ссылку на фотографию профиля сотрудника.
     *
     * @return Ссылка на фотографию профиля.
     */
    public String getProfilePicture() {
        return profilePicture;
    }
}