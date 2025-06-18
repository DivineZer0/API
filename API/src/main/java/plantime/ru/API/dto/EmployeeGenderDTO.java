package plantime.ru.API.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для передачи информации о гендере сотрудника.
 * Используется для передачи данных между слоями приложения и валидации входящего запроса.
 */
public class EmployeeGenderDTO {

    /**
     * Уникальный идентификатор гендера.
     */
    private Integer idEmployeeGender;

    /**
     * Название гендера.
     */
    @NotBlank(message = "Название гендера обязательно для заполнения.")
    @Size(min = 3, max = 10, message = "Название гендера должно содержать от 3 до 10 символов.")
    private String gender;

    /**
     * Конструктор по умолчанию.
     */
    public EmployeeGenderDTO() {
    }

    /**
     * Конструктор с параметрами.
     *
     * @param idEmployeeGender Идентификатор гендера.
     * @param gender           Название гендера.
     */
    public EmployeeGenderDTO(Integer idEmployeeGender, String gender) {
        this.idEmployeeGender = idEmployeeGender;
        this.gender = gender;
    }

    /**
     * Получить идентификатор гендера.
     *
     * @return idEmployeeGender Идентификатор гендера.
     */
    public Integer getIdEmployeeGender() {
        return idEmployeeGender;
    }

    /**
     * Установить идентификатор гендера.
     *
     * @param idEmployeeGender Идентификатор гендера.
     */
    public void setIdEmployeeGender(Integer idEmployeeGender) {
        this.idEmployeeGender = idEmployeeGender;
    }

    /**
     * Получить название гендера.
     *
     * @return gender Название гендера.
     */
    public String getGender() {
        return gender;
    }

    /**
     * Установить название гендера.
     *
     * @param gender Название гендера.
     */
    public void setGender(String gender) {
        this.gender = gender;
    }
}