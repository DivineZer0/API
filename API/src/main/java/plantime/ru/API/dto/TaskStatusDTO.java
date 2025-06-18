package plantime.ru.API.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для передачи данных о статусе задачи.
 * Используется для передачи данных между слоями приложения и валидации входящего запроса.
 */
public class TaskStatusDTO {

    /**
     * Уникальный идентификатор статуса задачи.
     */
    private Integer idTaskStatus;

    /**
     * Название статуса задачи.
     */
    @NotBlank(message = "Название статуса задачи обязательно для заполнения.")
    @Size(min = 2, max = 40, message = "Название статуса задачи должно содержать от 2 до 40 символов.")
    private String status;

    /**
     * Конструктор по умолчанию.
     */
    public TaskStatusDTO() {
    }

    /**
     * Конструктор с параметрами.
     *
     * @param idTaskStatus Идентификатор статуса задачи.
     * @param status       Название статуса задачи.
     */
    public TaskStatusDTO(Integer idTaskStatus, String status) {
        this.idTaskStatus = idTaskStatus;
        this.status = status;
    }

    /**
     * Получить идентификатор статуса задачи.
     *
     * @return idTaskStatus Идентификатор статуса задачи.
     */
    public Integer getIdTaskStatus() {
        return idTaskStatus;
    }

    /**
     * Установить идентификатор статуса задачи.
     *
     * @param idTaskStatus Идентификатор статуса задачи.
     */
    public void setIdTaskStatus(Integer idTaskStatus) {
        this.idTaskStatus = idTaskStatus;
    }

    /**
     * Получить название статуса задачи.
     *
     * @return status Название статуса задачи.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Установить название статуса задачи.
     *
     * @param status Название статуса задачи.
     */
    public void setStatus(String status) {
        this.status = status;
    }
}