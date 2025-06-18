package plantime.ru.API.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO для запроса входа в систему.
 * Содержит логин (или email) и пароль пользователя для аутентификации.
 */
public class LoginRequest {

    /**
     * Логин или email пользователя.
     */
    @NotNull(message = "Логин или email обязателен")
    @Size(min = 1, max = 255, message = "Логин или email должен содержать от 1 до 255 символов")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$|^[A-Za-z0-9_-]+$",
            message = "Логин или email должен быть валидным email или содержать только буквы, цифры, дефис и подчеркивание")
    private String login;

    /**
     * Пароль пользователя.
     */
    @NotNull(message = "Пароль обязателен")
    @Size(min = 8, max = 50, message = "Пароль должен содержать от 8 до 50 символов")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&._,]{8,50}$",
            message = "Пароль должен содержать как минимум одну букву, одну цифру и один специальный символ (@, $, !, %, *, #, ?, &)")
    private String password;

    /**
     * Возвращает логин или email пользователя.
     *
     * @return Логин или email.
     */
    public String getLogin() {
        return login;
    }

    /**
     * Устанавливает логин или email пользователя.
     *
     * @param login Логин или email.
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Возвращает пароль пользователя.
     *
     * @return Пароль.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Устанавливает пароль пользователя.
     *
     * @param password Пароль.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}