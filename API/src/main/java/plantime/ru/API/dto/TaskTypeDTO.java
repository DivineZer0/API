package plantime.ru.API.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для передачи данных о типе задачи.
 * Используется для передачи данных между слоями приложения и валидации входящего запроса.
 */
public class TaskTypeDTO {

    /**
     * Уникальный идентификатор типа задачи.
     */
    private Integer idTaskType;

    /**
     * Название типа задачи.
     */
    @NotBlank(message = "Название типа задачи обязательно для заполнения.")
    @Size(min = 2, max = 20, message = "Название типа задачи должно содержать от 2 до 20 символов.")
    private String type;

    /**
     * Конструктор по умолчанию.
     */
    public TaskTypeDTO() {
    }

    /**
     * Конструктор с параметрами.
     *
     * @param idTaskType Идентификатор типа задачи.
     * @param type       Название типа задачи.
     */
    public TaskTypeDTO(Integer idTaskType, String type) {
        this.idTaskType = idTaskType;
        this.type = type;
    }

    /**
     * Получить идентификатор типа задачи.
     *
     * @return idTaskType Идентификатор типа задачи.
     */
    public Integer getIdTaskType() {
        return idTaskType;
    }

    /**
     * Установить идентификатор типа задачи.
     *
     * @param idTaskType Идентификатор типа задачи.
     */
    public void setIdTaskType(Integer idTaskType) {
        this.idTaskType = idTaskType;
    }

    /**
     * Получить название типа задачи.
     *
     * @return type Название типа задачи.
     */
    public String getType() {
        return type;
    }

    /**
     * Установить название типа задачи.
     *
     * @param type Название типа задачи.
     */
    public void setType(String type) {
        this.type = type;
    }
}