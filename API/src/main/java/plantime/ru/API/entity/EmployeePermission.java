package plantime.ru.API.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Сущность, представляющая уровень прав доступа сотрудника в системе PlanTime.
 * Используется для хранения информации о возможных уровнях доступа.
 */
@Entity
@Table(name = "employee_permission")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "idEmployeePermission")
public class EmployeePermission {

    /**
     * Уникальный идентификатор уровня прав доступа сотрудника.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employee_permission", nullable = false)
    private Integer idEmployeePermission;

    /**
     * Название уровня прав доступа.
     */
    @Column(name = "permission", nullable = false, length = 40)
    @NotNull(message = "Название уровня прав доступа обязательно для заполнения.")
    @Size(min = 3, max = 40, message = "Название уровня прав доступа должно содержать от 3 до 40 символов.")
    private String permission;
}