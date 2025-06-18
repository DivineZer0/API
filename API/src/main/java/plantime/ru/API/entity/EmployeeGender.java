package plantime.ru.API.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Сущность, представляющая гендер сотрудника в системе PlanTime.
 * Используется для хранения информации о возможных гендерах.
 */
@Entity
@Table(name = "employee_gender")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "idEmployeeGender")
public class EmployeeGender {

    /**
     * Уникальный идентификатор гендера сотрудника.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employee_gender", nullable = false)
    private Integer idEmployeeGender;

    /**
     * Название гендера.
     */
    @Column(name = "gender", nullable = false, length = 10)
    @NotNull(message = "Название гендера обязательно для заполнения.")
    @Size(min = 3, max = 10, message = "Название гендера должно содержать от 3 до 10 символов.")
    private String gender;
}