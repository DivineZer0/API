package plantime.ru.API.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Сущность, представляющая должность сотрудника в системе PlanTime.
 * Используется для хранения информации о должностях.
 */
@Entity
@Table(name = "employee_post")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "idEmployeePost")
public class EmployeePost {

    /**
     * Уникальный идентификатор должности сотрудника.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employee_post", nullable = false)
    private Integer idEmployeePost;

    /**
     * Название должности.
     */
    @Column(name = "post", length = 40, nullable = false)
    @NotNull(message = "Название должности не может быть пустым")
    @Size(min = 3, max = 40, message = "Название должности должно содержать от 3 до 40 символов.")
    private String post;

    /**
     * Уровень прав, связанный с должностью.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employee_permission", nullable = false)
    @NotNull(message = "Сущность уровня прав доступа обязательна.")
    private EmployeePermission employeePermission;
}