package plantime.ru.API.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO для ответа на запрос шифрования пароля.
 * Содержит зашифрованный пароль, возвращаемый клиенту в ответ на запрос к /hash-password.
 */
public class HashPasswordResponse {

    /**
     * Зашифрованный пароль.
     * Не может быть null.
     */
    @NotNull(message = "Хешированный пароль не может быть пустым")
    private final String hashedPassword;

    /**
     * Конструктор для создания ответа с зашифрованным паролем.
     *
     * @param hashedPassword Зашифрованный пароль.
     */
    public HashPasswordResponse(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    /**
     * Возвращает зашифрованный пароль.
     *
     * @return Зашифрованный пароль.
     */
    public String getHashedPassword() {
        return hashedPassword;
    }
}