package plantime.ru.API.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskRecurrenceDTO {
    private Integer idTaskRecurrence;

    @NotBlank(message = "Шаблон периодичности обязателен")
    @Size(min = 2, max = 40, message = "Название шаблона должно быть от 2 до 40 символов")
    private String recurrencePattern;
}