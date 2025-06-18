package plantime.ru.API.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для передачи данных о типе отсутствия сотрудника.
 * Используется для передачи данных между слоями приложения и валидации входящего запроса.
 */
public class TypeAbsenceDTO {

    /**
     * Уникальный идентификатор типа отсутствия.
     */
    private Integer idTypeAbsence;

    /**
     * Название типа отсутствия.
     */
    @NotBlank(message = "Название типа отсутствия обязательно для заполнения.")
    @Size(min = 3, max = 25, message = "Название типа отсутствия должно содержать от 3 до 25 символов.")
    private String typeOfAbsence;

    /**
     * Конструктор по умолчанию.
     */
    public TypeAbsenceDTO() {
    }

    /**
     * Конструктор с параметрами.
     *
     * @param idTypeAbsence Идентификатор типа отсутствия.
     * @param typeOfAbsence Название типа отсутствия.
     */
    public TypeAbsenceDTO(Integer idTypeAbsence, String typeOfAbsence) {
        this.idTypeAbsence = idTypeAbsence;
        this.typeOfAbsence = typeOfAbsence;
    }

    /**
     * Получить идентификатор типа отсутствия.
     */
    public Integer getIdTypeAbsence() {
        return idTypeAbsence;
    }

    /**
     * Установить идентификатор типа отсутствия.
     */
    public void setIdTypeAbsence(Integer idTypeAbsence) {
        this.idTypeAbsence = idTypeAbsence;
    }

    /**
     * Получить название типа отсутствия.
     */
    public String getTypeOfAbsence() {
        return typeOfAbsence;
    }

    /**
     * Установить название типа отсутствия.
     */
    public void setTypeOfAbsence(String typeOfAbsence) {
        this.typeOfAbsence = typeOfAbsence;
    }
}