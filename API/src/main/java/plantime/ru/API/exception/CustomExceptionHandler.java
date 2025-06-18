package plantime.ru.API.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений для API PlanTime.
 */
@ControllerAdvice
public class CustomExceptionHandler {

    /**
     * Обрабатывает исключения типа {@link IllegalStateException}.
     * Возникает, когда операция не может быть выполнена из-за зависимостей (например, удаление используемого отдела).
     *
     * @param ex Исключение, выброшенное из-за недопустимого состояния.
     * @return Ответ с кодом 400 и описанием ошибки.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("title", "Невозможно выполнить операцию");
        response.put("detail", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает исключения типа {@link RuntimeException}.
     * Определяет HTTP-статус на основе содержимого сообщения об ошибке.
     *
     * @param ex Исключение, выброшенное во время выполнения.
     * @return Ответ с кодом ошибки и описанием (404, 401 или 400).
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();
        String message = ex.getMessage();

        if (message.contains("не найден") || message.contains("Сессия не найдена")) {
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("title", "Не найдено");
            response.put("detail", message);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } else if (message.contains("Недействительный") || message.contains("истёк")) {
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            response.put("title", "Неавторизован");
            response.put("detail", message);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        } else {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("title", "Неверный запрос");
            response.put("detail", message);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Обрабатывает исключения типа {@link MethodArgumentNotValidException}.
     * Возвращает список ошибок валидации для полей запроса.
     *
     * @param ex Исключение, возникшее при валидации входных данных.
     * @return Ответ с кодом 400 и списком ошибок валидации.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("title", "Ошибка валидации");

        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает исключения типа {@link HttpMessageNotReadableException}.
     * Возникает, когда тело запроса не может быть прочитано.
     *
     * @param ex Исключение, возникшее при чтении тела запроса.
     * @return Ответ с кодом 400 и описанием ошибки.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("title", "Неверный запрос");
        response.put("detail", "Тело запроса невалидно или не может быть прочитано");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Преобразует ошибку валидации поля в объект с информацией об ошибке.
     *
     * @param error Ошибка валидации поля.
     * @return Карта с именем поля и сообщением об ошибке.
     */
    private Map<String, String> mapFieldError(FieldError error) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("field", error.getField());
        errorDetails.put("message", error.getDefaultMessage());
        return errorDetails;
    }
}