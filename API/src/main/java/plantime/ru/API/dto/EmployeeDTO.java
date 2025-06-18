package plantime.ru.API.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для передачи данных о сотруднике.
 */
public class EmployeeDTO {

    @NotBlank(message = "Идентификатор сотрудника обязателен")
    @Size(min = 36, max = 36, message = "Идентификатор сотрудника должен быть длиной 36 символов")
    private String guidEmployee;

    @NotBlank(message = "Логин обязателен")
    @Size(max = 40, message = "Логин не должен превышать 40 символов")
    private String login;

    @NotBlank(message = "Электронная почта обязательна")
    @Size(max = 120, message = "Электронная почта не должна превышать 120 символов")
    @Email(message = "Недействительный формат электронной почты")
    private String email;

    @Size(max = 255, message = "Пароль не должен превышать 255 символов")
    private String password;

    @NotBlank(message = "Фамилия обязательна")
    @Size(max = 40, message = "Фамилия не должна превышать 40 символов")
    private String surname;

    @NotBlank(message = "Имя обязательно")
    @Size(max = 20, message = "Имя не должно превышать 20 символов")
    private String firstName;

    @Size(max = 25, message = "Отчество не должно превышать 25 символов")
    private String patronymic;

    @NotBlank(message = "Название файла фотографии обязательно")
    @Size(max = 255, message = "Название файла фотографии не должно превышать 255 символов")
    private String profilePicture;

    private LocalDateTime lastAuthorization;
    private LocalDateTime dateOfBirth;

    @NotBlank(message = "Номер телефона обязателен")
    @Size(max = 16, message = "Номер телефона не должен превышать 16 символов")
    @Pattern(regexp = "\\+7\\(\\d{3}\\)\\d{3}-\\d{2}-\\d{2}", message = "Номер телефона должен соответствовать формату +7(XXX)XXX-XX-XX")
    private String phoneNumber;

    @DecimalMin(value = "0.0", inclusive = false, message = "Почасовая ставка должна быть больше 0")
    private BigDecimal hourlyRate;

    private String note;

    @NotNull(message = "Идентификатор должности обязателен")
    private Integer idEmployeePost;

    @NotNull(message = "Идентификатор статуса обязателен")
    private Integer idEmployeeStatus;

    @NotNull(message = "Идентификатор отдела обязателен")
    private Integer idEmployeeDepartment;

    @NotNull(message = "Идентификатор пола обязателен")
    private Integer idEmployeeGender;

    // Новые поля для связанных сущностей (названия)
    private String departmentName;
    private String postName;
    private String statusName;
    private String genderName;

    public EmployeeDTO() {}

    public EmployeeDTO(
            String guidEmployee, String login, String email, String password, String surname,
            String firstName, String patronymic, String profilePicture, LocalDateTime lastAuthorization,
            LocalDateTime dateOfBirth, String phoneNumber, BigDecimal hourlyRate, String note,
            Integer idEmployeePost, Integer idEmployeeStatus, Integer idEmployeeDepartment,
            Integer idEmployeeGender
    ) {
        this.guidEmployee = guidEmployee;
        this.login = login;
        this.email = email;
        this.password = password;
        this.surname = surname;
        this.firstName = firstName;
        this.patronymic = patronymic;
        this.profilePicture = profilePicture;
        this.lastAuthorization = lastAuthorization;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.hourlyRate = hourlyRate;
        this.note = note;
        this.idEmployeePost = idEmployeePost;
        this.idEmployeeStatus = idEmployeeStatus;
        this.idEmployeeDepartment = idEmployeeDepartment;
        this.idEmployeeGender = idEmployeeGender;
    }

    // --- Геттеры и сеттеры ---
    public String getGuidEmployee() { return guidEmployee; }
    public void setGuidEmployee(String guidEmployee) { this.guidEmployee = guidEmployee; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getPatronymic() { return patronymic; }
    public void setPatronymic(String patronymic) { this.patronymic = patronymic; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public LocalDateTime getLastAuthorization() { return lastAuthorization; }
    public void setLastAuthorization(LocalDateTime lastAuthorization) { this.lastAuthorization = lastAuthorization; }

    public LocalDateTime getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDateTime dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Integer getIdEmployeePost() { return idEmployeePost; }
    public void setIdEmployeePost(Integer idEmployeePost) { this.idEmployeePost = idEmployeePost; }

    public Integer getIdEmployeeStatus() { return idEmployeeStatus; }
    public void setIdEmployeeStatus(Integer idEmployeeStatus) { this.idEmployeeStatus = idEmployeeStatus; }

    public Integer getIdEmployeeDepartment() { return idEmployeeDepartment; }
    public void setIdEmployeeDepartment(Integer idEmployeeDepartment) { this.idEmployeeDepartment = idEmployeeDepartment; }

    public Integer getIdEmployeeGender() { return idEmployeeGender; }
    public void setIdEmployeeGender(Integer idEmployeeGender) { this.idEmployeeGender = idEmployeeGender; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getPostName() { return postName; }
    public void setPostName(String postName) { this.postName = postName; }

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }

    public String getGenderName() { return genderName; }
    public void setGenderName(String genderName) { this.genderName = genderName; }
}