package plantime.ru.API.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import plantime.ru.API.dto.ErrorResponse;
import plantime.ru.API.entity.*;
import plantime.ru.API.repository.LogRepository;

import java.lang.NoSuchMethodError;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Глобальный обработчик исключений для REST-контроллеров.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final LogRepository logRepository;

    private static final Employee employee = new Employee(
            "00000000-0000-0000-0000-000000000000",
            "",
            "",
            "",
            "",
            "",
            null,
            "no_photo.jpg",
            null,
            LocalDateTime.of(1920, 1, 1, 0, 0),
            "",
            BigDecimal.ZERO,
            null,
            new EmployeePost(null, "Default", new EmployeePermission()),
            new EmployeeStatus(null, "Default"),
            new EmployeeDepartment(null, "Default"),
            new EmployeeGender(null, "Default")
    );

    /**
     * Конструктор для инициализации обработчика исключений.
     *
     * @param logRepository Репозиторий для сохранения логов исключений.
     */
    public GlobalExceptionHandler(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * Создает и сохраняет запись в логе для исключения или ошибки.
     *
     * @param throwable Исключение или ошибка, вызвавшая проблему.
     * @param message   Сообщение для логирования.
     */
    private void logError(Throwable throwable, String message) {
        try {
            String logMessage = String.format("%s: %s (%s)", message, throwable.getMessage(), throwable.getClass().getSimpleName());
            Log log = new Log(employee, logMessage, LocalDateTime.now());
            logRepository.save(log);
        } catch (Exception logException) {
            System.err.println("Ошибка при сохранении лога: " + logException.getMessage());
        }
    }

    /**
     * Обрабатывает исключения типа IllegalArgumentException.
     * Возвращает ошибку 400 при неверных аргументах запроса.
     *
     * @param e Исключение IllegalArgumentException.
     * @return Ответ с информацией об ошибке.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        logError(e, "Неверный запрос");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage(), "Неверный запрос", 400));
    }

    /**
     * Обрабатывает исключения типа MissingServletRequestParameterException.
     * Возвращает ошибку 400 при отсутствии обязательных параметров запроса.
     *
     * @param e Исключение MissingServletRequestParameterException.
     * @return Ответ с информацией об ошибке.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        logError(e, "Отсутствует обязательный параметр");
        String message = String.format("Отсутствует обязательный параметр: %s", e.getParameterName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(message, "Неверный запрос", 400));
    }

    /**
     * Обрабатывает исключения типа HttpRequestMethodNotSupportedException.
     * Возвращает ошибку 405 при использовании неподдерживаемого HTTP-метода.
     *
     * @param e Исключение HttpRequestMethodNotSupportedException.
     * @return Ответ с информацией об ошибке.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        logError(e, "Неподдерживаемый метод");
        String message = String.format("HTTP-метод не поддерживается: %s", e.getMethod());
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse(message, "Неподдерживаемый метод", 405));
    }

    /**
     * Обрабатывает ошибки типа NoSuchMethodError.
     * Возвращает ошибку 500 при несовместимости библиотек или отсутствии метода.
     *
     * @param e Ошибка NoSuchMethodError.
     * @return Ответ с информацией об ошибке.
     */
    @ExceptionHandler(NoSuchMethodError.class)
    public ResponseEntity<ErrorResponse> handleNoSuchMethodError(NoSuchMethodError e) {
        logError(e, "Несовместимость библиотеки");
        String message = String.format("Внутренняя ошибка сервера: %s", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(message, "Ошибка сервера", 500));
    }

    /**
     * Обрабатывает все необработанные исключения.
     * Возвращает ошибку 500 для неожиданных ошибок.
     *
     * @param e Исключение Exception.
     * @return Ответ с информацией об ошибке.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        logError(e, "Неожиданная ошибка");
        String message = String.format("Неожиданная ошибка: %s", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(message, "Внутренняя ошибка сервера", 500));
    }
}