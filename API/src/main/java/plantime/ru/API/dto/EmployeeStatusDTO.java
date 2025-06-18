package plantime.ru.API.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для передачи информации о статусе сотрудников.
 * Используется для передачи данных между слоями приложения и валидации входящего запроса.
 */
public class EmployeeStatusDTO {

    /**
     * Уникальный идентификатор статуса.
     */
    private Integer idEmployeeStatus;

    /**
     * Название статуса.
     */
    @NotBlank(message = "Название статуса обязательно для заполнения.")
    @Size(min = 3, max = 20, message = "Название статуса должно содержать от 3 до 20 символов.")
    private String status;

    /**
     * Конструктор по умолчанию.
     */
    public EmployeeStatusDTO() {
    }

    /**
     * Конструктор с параметрами.
     *
     * @param idEmployeeStatus Идентификатор статуса.
     * @param status           Название статуса.
     */
    public EmployeeStatusDTO(Integer idEmployeeStatus, String status) {
        this.idEmployeeStatus = idEmployeeStatus;
        this.status = status;
    }

    /**
     * Получить идентификатор статуса.
     *
     * @return idEmployeeStatus Идентификатор статуса.
     */
    public Integer getIdEmployeeStatus() {
        return idEmployeeStatus;
    }

    /**
     * Установить идентификатор статуса.
     *
     * @param idEmployeeStatus Идентификатор статуса.
     */
    public void setIdEmployeeStatus(Integer idEmployeeStatus) {
        this.idEmployeeStatus = idEmployeeStatus;
    }

    /**
     * Получить название статуса.
     *
     * @return status Название статуса.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Установить название статуса.
     *
     * @param status Название статуса.
     */
    public void setStatus(String status) {
        this.status = status;
    }
}