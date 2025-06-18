package plantime.ru.API.dto;

import jakarta.validation.constraints.*;

public class CustomerDTO {

    private Integer idCustomer;

    @NotBlank(message = "Имя обязательно")
    @Size(max = 20, message = "Имя не более 20 символов")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @Size(max = 40, message = "Фамилия не более 40 символов")
    private String surname;

    @Size(max = 25, message = "Отчество не более 25 символов")
    private String patronymic;

    @Email(message = "Некорректный email")
    @Size(max = 120, message = "Email не более 120 символов")
    private String email;

    @Pattern(regexp = "^\\+7\\(\\d{3}\\)\\d{3}-\\d{2}-\\d{2}$", message = "Телефон должен быть в формате +7(XXX)XXX-XX-XX")
    @Size(max = 16, message = "Телефон не более 16 символов")
    private String phoneNumber;

    @Size(max = 1000, message = "Примечание не более 1000 символов")
    private String note;

    @NotNull(message = "Организация обязательна")
    private Integer idOrganization;

    private String organizationShortName; // Для отображения

    public CustomerDTO() {}

    public CustomerDTO(Integer idCustomer, String firstName, String surname, String patronymic, String email,
                       String phoneNumber, String note, Integer idOrganization, String organizationShortName) {
        this.idCustomer = idCustomer;
        this.firstName = firstName;
        this.surname = surname;
        this.patronymic = patronymic;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.note = note;
        this.idOrganization = idOrganization;
        this.organizationShortName = organizationShortName;
    }

    public Integer getIdCustomer() {
        return idCustomer;
    }

    public void setIdCustomer(Integer idCustomer) {
        this.idCustomer = idCustomer;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getIdOrganization() {
        return idOrganization;
    }

    public void setIdOrganization(Integer idOrganization) {
        this.idOrganization = idOrganization;
    }

    public String getOrganizationShortName() {
        return organizationShortName;
    }

    public void setOrganizationShortName(String organizationShortName) {
        this.organizationShortName = organizationShortName;
    }
}