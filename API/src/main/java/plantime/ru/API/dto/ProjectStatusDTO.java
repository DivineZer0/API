package plantime.ru.API.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для передачи данных о статусе проекта.
 * Используется для передачи данных между слоями приложения и валидации входящего запроса.
 */
public class ProjectStatusDTO {

    /**
     * Уникальный идентификатор статуса проекта.
     */
    private Integer idProjectStatus;

    /**
     * Название статуса проекта.
     */
    @NotBlank(message = "Название статуса проекта обязательно для заполнения.")
    @Size(min = 2, max = 40, message = "Название статуса проекта должно содержать от 2 до 40 символов.")
    private String status;

    /**
     * Конструктор по умолчанию.
     */
    public ProjectStatusDTO() {
    }

    /**
     * Конструктор с параметрами.
     *
     * @param idProjectStatus Идентификатор статуса проекта.
     * @param status          Название статуса проекта.
     */
    public ProjectStatusDTO(Integer idProjectStatus, String status) {
        this.idProjectStatus = idProjectStatus;
        this.status = status;
    }

    /**
     * Получить идентификатор статуса проекта.
     *
     * @return idProjectStatus Идентификатор статуса проекта.
     */
    public Integer getIdProjectStatus() {
        return idProjectStatus;
    }

    /**
     * Установить идентификатор статуса проекта.
     *
     * @param idProjectStatus Идентификатор статуса проекта.
     */
    public void setIdProjectStatus(Integer idProjectStatus) {
        this.idProjectStatus = idProjectStatus;
    }

    /**
     * Получить название статуса проекта.
     *
     * @return status Название статуса проекта.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Установить название статуса проекта.
     *
     * @param status Название статуса проекта.
     */
    public void setStatus(String status) {
        this.status = status;
    }
}