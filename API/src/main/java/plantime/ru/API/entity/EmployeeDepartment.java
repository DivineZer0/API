package plantime.ru.API.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Сущность, представляющая отдел сотрудников в системе PlanTime.
 * Используется для хранения информации о подразделениях компании.
 */
@Entity
@Table(name = "employee_department")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "idEmployeeDepartment")
public class EmployeeDepartment {

    /**
     * Уникальный идентификатор отдела сотрудников.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employee_department", nullable = false)
    private Integer idEmployeeDepartment;

    /**
     * Название отдела сотрудников.
     */
    @Column(name = "department", nullable = false, length = 60)
    @NotNull(message = "Название отдела обязательно для заполнения.")
    @Size(min = 3, max = 60, message = "Название отдела должно содержать от 3 до 60 символов.")
    private String department;
}