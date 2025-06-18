package plantime.ru.API.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

/**
 * DTO для запроса шифрования пароля в REST API.
 * Содержит пароль в открытом виде, который будет зашифрован сервером.
 */
public class HashPasswordRequest {

    /**
     * Пароль в открытом виде для шифрования.
     *
     * Должен содержать: как минимум одну букву, одну цифру, один специальный символ.
     */
    @NotNull(message = "Пароль не может быть пустым")
    @Size(min = 8, max = 50, message = "Пароль должен содержать от 8 до 50 символов")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&._,]{8,50}$",
            message = "Пароль должен содержать как минимум одну букву, одну цифру и один специальный символ (@, $, !, %, *, #, ?, &)")
    private String password;

    /**
     * Возвращает пароль в открытом виде.
     *
     * @return Пароль в открытом виде.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Устанавливает пароль в открытом виде.
     *
     * @param password Пароль в открытом виде.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}