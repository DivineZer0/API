package plantime.ru.API.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import jakarta.validation.constraints.Pattern;
import org.apache.logging.log4j.util.Chars;

/**
 * Сущность, представляющая сотрудника в системе PlanTime.
 */
@Entity
@Table(name = "employee")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "guidEmployee")
public class Employee {

    /**
     * Уникальный идентификатор сотрудника.
     */
    @Id
    @Column(name = "guid_employee", nullable = false, length = 36)
    @NotNull(message = "Идентификатор сотрудника обязателен")
    @Size(min = 36, max = 36, message = "Идентификатор сотрудника должен быть длиной 36 символов")
    private String guidEmployee;

    /**
     * Логин сотрудника для авторизации в системе.
     */
    @Column(name = "login", nullable = false, length = 40)
    @NotNull(message = "Логин обязателен")
    @Size(max = 40, message = "Логин не должен превышать 40 символов")
    private String login;

    /**
     * Электронная почта сотрудника.
     */
    @Column(name = "email", length = 120, nullable = false)
    @NotNull(message = "Электронная почта обязательна")
    @Size(max = 120, message = "Электронная почта не должна превышать 120 символов")
    private String email;

    /**
     * Зашифрованный пароль сотрудника.
     */
    @Column(name = "password", length = 255, nullable = false)
    @NotNull(message = "Пароль обязателен")
    @Size(max = 255, message = "Пароль не должен превышать 255 символов")
    private String password;

    /**
     * Фамилия сотрудника.
     */
    @Column(name = "surname", length = 40, nullable = false)
    @NotNull(message = "Фамилия обязательна")
    @Size(max = 40, message = "Фамилия не должна превышать 40 символов")
    private String surname;

    /**
     * Имя сотрудника.
     */
    @Column(name = "first_name", length = 20, nullable = false)
    @NotNull(message = "Имя обязательно")
    @Size(max = 20, message = "Имя не должно превышать 20 символов")
    private String firstName;

    /**
     * Отчество сотрудника.
     */
    @Column(name = "patronymic", length = 25)
    @Size(max = 25, message = "Отчество не должно превышать 25 символов")
    private String patronymic;

    /**
     * Назфание файла фотографии профиля сотрудника.
     */
    @Column(name = "profile_picture", length = 255, nullable = false)
    @NotNull(message = "Название файла обязательно")
    @Size(max = 255, message = "Название файла фотографии не должна превышать 255 символов")
    private String profilePicture;

    /**
     * Дата и время последней авторизации сотрудника.
     */
    @Column(name = "last_authorization")
    private LocalDateTime lastAuthorization;

    /**
     * Дата рождения сотрудника.
     */
    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    /**
     * Номер телефона сотрудника.
     */
    @Column(name = "phone_number", length = 16, nullable = false)
    @NotNull(message = "Номер телефона обязателен")
    @Size(max = 16, message = "Номер телефона не должен превышать 16 символов")
    @Pattern(regexp = "\\+7\\(\\d{3}\\)\\d{3}-\\d{2}-\\d{2}", message = "Номер телефона должен соответствовать формату +7(XXX)XXX-XX-XX")
    private String phoneNumber;

    /**
     * Почасовая ставка сотрудника.
     */
    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    /**
     * Примечание о сотруднике.
     */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    /**
     * Должность сотрудника.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employee_post", nullable = false)
    @NotNull(message = "Идентификатор должности сотрудника обязателен")
    private EmployeePost employeePost;

    /**
     * Статус сотрудника.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employee_status", nullable = false)
    @NotNull(message = "Сущность статуса сотрудника обязательна")
    private EmployeeStatus employeeStatus;

    /**
     * Отдел организации, к которому относится сотрудник.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employee_department", nullable = false)
    @NotNull(message = "Сущность отдела организации обязательна")
    private EmployeeDepartment employeeDepartment;

    /**
     * Пол сотрудника.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employee_gender", nullable = false)
    @NotNull(message = "Сущность пола сотрудника обязательна")
    private EmployeeGender employeeGender;
}