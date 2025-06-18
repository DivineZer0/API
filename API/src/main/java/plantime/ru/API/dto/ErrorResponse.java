package plantime.ru.API.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO для представления ответа об ошибке в REST API.
 * Используется для передачи стандартизированной информации об ошибке в ответах API.
 * Возвращается в ResponseEntity<ErrorResponse> при обработке исключений в GlobalExceptionHandler.
 */
public class ErrorResponse {

    /**
     * Подробное сообщение об ошибке.
     * Содержит конкретную информацию о проблеме.
     * Не может быть null.
     */
    @NotNull(message = "Подробное сообщение об ошибке не может быть пустым")
    private final String detail;

    /**
     * Название или категория ошибки.
     * Краткое описание типа ошибки.
     * Не может быть null.
     */
    @NotNull(message = "Название ошибки не может быть пустым")
    private final String title;

    /**
     * HTTP-код состояния, связанный с ошибкой.
     * Должен быть валидным HTTP-кодом (100–599), например, 400 (Bad Request) или 500 (Internal Server Error).
     */
    @Min(value = 100, message = "HTTP-код состояния должен быть не менее 100")
    @Max(value = 599, message = "HTTP-код состояния должен быть не более 599")
    private final int status;

    /**
     * Конструктор для создания ответа об ошибке.
     *
     * @param detail Подробное сообщение об ошибке.
     * @param title  Название или категория ошибки.
     * @param status HTTP-код состояния.
     */
    public ErrorResponse(String detail, String title, int status) {
        this.detail = detail;
        this.title = title;
        this.status = status;
    }

    /**
     * Возвращает подробное сообщение об ошибке.
     *
     * @return Подробное сообщение об ошибке.
     */
    public String getDetail() {
        return detail;
    }

    /**
     * Возвращает название или категорию ошибки.
     *
     * @return Название или категория ошибки.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Возвращает HTTP-код состояния ошибки.
     *
     * @return HTTP-код состояния.
     */
    public int getStatus() {
        return status;
    }
}