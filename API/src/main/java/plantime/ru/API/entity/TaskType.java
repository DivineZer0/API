package plantime.ru.API.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Сущность, представляющая тип задачи в системе PlanTime.
 * Используется для хранения информации о типах задач.
 */
@Entity
@Table(name = "task_type")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "idTaskType")
public class TaskType {

    /**
     * Уникальный идентификатор типа задачи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_task_type", nullable = false)
    private Integer idTaskType;

    /**
     * Название типа задачи.
     */
    @Column(name = "type", nullable = false, length = 20)
    @NotNull(message = "Название типа задачи обязательно для заполнения.")
    @Size(min = 2, max = 20, message = "Название типа задачи должно содержать от 2 до 20 символов.")
    private String type;
}