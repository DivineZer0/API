package plantime.ru.API.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для передачи информации о правах доступа сотрудников.
 * Используется для передачи данных между слоями приложения и валидации входящего запроса.
 */
public class EmployeePermissionDTO {

    /**
     * Уникальный идентификатор уровня прав доступа.
     */
    private Integer idEmployeePermission;

    /**
     * Название уровня прав доступа.
     */
    @NotBlank(message = "Название уровня прав доступа обязательно для заполнения.")
    @Size(min = 3, max = 40, message = "Название уровня прав доступа должно содержать от 3 до 40 символов.")
    private String permission;

    /**
     * Конструктор по умолчанию.
     */
    public EmployeePermissionDTO() {
    }

    /**
     * Конструктор с параметрами.
     *
     * @param idEmployeePermission Идентификатор уровня прав доступа.
     * @param permission           Название уровня прав доступа.
     */
    public EmployeePermissionDTO(Integer idEmployeePermission, String permission) {
        this.idEmployeePermission = idEmployeePermission;
        this.permission = permission;
    }

    /**
     * Получить идентификатор уровня прав доступа.
     *
     * @return idEmployeePermission Идентификатор уровня прав доступа.
     */
    public Integer getIdEmployeePermission() {
        return idEmployeePermission;
    }

    /**
     * Установить идентификатор уровня прав доступа.
     *
     * @param idEmployeePermission Идентификатор уровня прав доступа.
     */
    public void setIdEmployeePermission(Integer idEmployeePermission) {
        this.idEmployeePermission = idEmployeePermission;
    }

    /**
     * Получить название уровня прав доступа.
     *
     * @return permission Название уровня прав доступа.
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Установить название уровня прав доступа.
     *
     * @param permission Название уровня прав доступа.
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }
}