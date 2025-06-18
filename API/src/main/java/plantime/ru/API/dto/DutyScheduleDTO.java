package plantime.ru.API.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO для передачи и валидации данных расписания дежурств и отсутствий.
 * Используется для обмена данными между слоями приложения и на входе API.
 */
public class DutyScheduleDTO {

    /**
     * Уникальный идентификатор записи расписания.
     */
    private Long idDutySchedule;

    /**
     * ФИО сотрудника.
     */
    @NotBlank(message = "ФИО сотрудника обязательно")
    private String employeeName;

    /**
     * Дата начала периода.
     */
    @NotNull(message = "Дата начала обязательна")
    private LocalDate dateStart;

    /**
     * Дата окончания периода.
     */
    @NotNull(message = "Дата окончания обязательна")
    private LocalDate dateEnd;

    /**
     * Тип отсутствия.
     */
    @NotBlank(message = "Тип отсутствия обязателен")
    private String typeOfAbsence;

    /**
     * Описание или комментарий к записи.
     */
    private String description;

    /**
     * Конструктор по умолчанию.
     */
    public DutyScheduleDTO() {}

    /**
     * Конструктор для создания объекта без описания.
     *
     * @param idDutySchedule  Идентификатор записи
     * @param employeeName    ФИО сотрудника
     * @param dateStart       Дата начала периода
     * @param dateEnd         Дата окончания периода
     * @param typeOfAbsence   Тип отсутствия
     */
    public DutyScheduleDTO(Long idDutySchedule, String employeeName, LocalDate dateStart, LocalDate dateEnd, String typeOfAbsence) {
        this(idDutySchedule, employeeName, dateStart, dateEnd, typeOfAbsence, null);
    }

    /**
     * Конструктор для создания объекта с описанием.
     *
     * @param idDutySchedule  Идентификатор записи
     * @param employeeName    ФИО сотрудника
     * @param dateStart       Дата начала периода
     * @param dateEnd         Дата окончания периода
     * @param typeOfAbsence   Тип отсутствия
     * @param description     Описание/комментарий
     */
    public DutyScheduleDTO(Long idDutySchedule, String employeeName, LocalDate dateStart, LocalDate dateEnd, String typeOfAbsence, String description) {
        this.idDutySchedule = idDutySchedule;
        this.employeeName = employeeName;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.typeOfAbsence = typeOfAbsence;
        this.description = description;
    }

    /**
     * @return Идентификатор записи расписания
     */
    public Long getIdDutySchedule() { return idDutySchedule; }

    /**
     * @param idDutySchedule Идентификатор записи расписания
     */
    public void setIdDutySchedule(Long idDutySchedule) { this.idDutySchedule = idDutySchedule; }

    /**
     * @return ФИО сотрудника
     */
    public String getEmployeeName() { return employeeName; }

    /**
     * @param employeeName ФИО сотрудника
     */
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    /**
     * @return Дата начала периода
     */
    public LocalDate getDateStart() { return dateStart; }

    /**
     * @param dateStart Дата начала периода
     */
    public void setDateStart(LocalDate dateStart) { this.dateStart = dateStart; }

    /**
     * @return Дата окончания периода
     */
    public LocalDate getDateEnd() { return dateEnd; }

    /**
     * @param dateEnd Дата окончания периода
     */
    public void setDateEnd(LocalDate dateEnd) { this.dateEnd = dateEnd; }

    /**
     * @return Тип отсутствия
     */
    public String getTypeOfAbsence() { return typeOfAbsence; }

    /**
     * @param typeOfAbsence Тип отсутствия
     */
    public void setTypeOfAbsence(String typeOfAbsence) { this.typeOfAbsence = typeOfAbsence; }

    /**
     * @return Описание/комментарий
     */
    public String getDescription() { return description; }

    /**
     * @param description Описание/комментарий
     */
    public void setDescription(String description) { this.description = description; }
}