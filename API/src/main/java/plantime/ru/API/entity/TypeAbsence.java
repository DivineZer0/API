package plantime.ru.API.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Сущность, представляющая тип отсутствия сотрудника в системе PlanTime.
 * Используется для хранения информации о типах отсутствия.
 */
@Entity
@Table(name = "type_absence")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "idTypeAbsence")
public class TypeAbsence {

    /**
     * Уникальный идентификатор типа отсутствия.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_absence", nullable = false)
    private Integer idTypeAbsence;

    /**
     * Название типа отсутствия.
     */
    @Column(name = "type_of_absence", nullable = false, length = 25)
    @NotNull(message = "Название типа отсутствия обязательно для заполнения.")
    @Size(min = 3, max = 25, message = "Название типа отсутствия должно содержать от 3 до 25 символов.")
    private String typeOfAbsence;
}