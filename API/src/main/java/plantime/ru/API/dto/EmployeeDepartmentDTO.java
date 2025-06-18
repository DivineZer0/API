package plantime.ru.API.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для передачи информации об отделе сотрудников.
 * Используется для передачи данных между слоями приложения и валидации входящего запроса.
 */
public class EmployeeDepartmentDTO {

    /**
     * Уникальный идентификатор отдела.
     */
    private Integer idEmployeeDepartment;

    /**
     * Название отдела.
     */
    @NotBlank(message = "Название отдела обязательно для заполнения.")
    @Size(min = 3, max = 60, message = "Название отдела должно содержать от 3 до 60 символов.")
    private String department;

    /**
     * Конструктор по умолчанию.
     */
    public EmployeeDepartmentDTO() {
    }

    /**
     * Конструктор с параметрами.
     *
     * @param idEmployeeDepartment Идентификатор отдела.
     * @param department           Название отдела.
     */
    public EmployeeDepartmentDTO(Integer idEmployeeDepartment, String department) {
        this.idEmployeeDepartment = idEmployeeDepartment;
        this.department = department;
    }

    /**
     * Получить идентификатор отдела.
     *
     * @return idEmployeeDepartment Идентификатор отдела.
     */
    public Integer getIdEmployeeDepartment() {
        return idEmployeeDepartment;
    }

    /**
     * Установить идентификатор отдела.
     *
     * @param idEmployeeDepartment Идентификатор отдела.
     */
    public void setIdEmployeeDepartment(Integer idEmployeeDepartment) {
        this.idEmployeeDepartment = idEmployeeDepartment;
    }

    /**
     * Получить название отдела.
     *
     * @return department Название отдела.
     */
    public String getDepartment() {
        return department;
    }

    /**
     * Установить название отдела.
     *
     * @param department Название отдела.
     */
    public void setDepartment(String department) {
        this.department = department;
    }
}