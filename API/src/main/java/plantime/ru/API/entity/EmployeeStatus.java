package plantime.ru.API.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Сущность, представляющая статус сотрудника в системе PlanTime.
 * Используется для хранения информации о статусах сотрудников.
 */
@Entity
@Table(name = "employee_status")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "idEmployeeStatus")
public class EmployeeStatus {

    /**
     * Уникальный идентификатор статуса сотрудника.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employee_status", nullable = false)
    private Integer idEmployeeStatus;

    /**
     * Название статуса сотрудника.
     */
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Название статуса обязательно для заполнения.")
    @Size(min = 3, max = 20, message = "Название статуса должно содержать от 3 до 20 символов.")
    private String status;
}