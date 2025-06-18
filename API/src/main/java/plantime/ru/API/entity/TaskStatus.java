package plantime.ru.API.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Сущность, представляющая статус задачи в системе PlanTime.
 * Используется для хранения информации о статусах задач.
 */
@Entity
@Table(name = "task_status")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "idTaskStatus")
public class TaskStatus {

    /**
     * Уникальный идентификатор статуса задачи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_task_status", nullable = false)
    private Integer idTaskStatus;

    /**
     * Название статуса задачи.
     */
    @Column(name = "status", nullable = false, length = 40)
    @NotNull(message = "Название статуса задачи обязательно для заполнения.")
    @Size(min = 2, max = 40, message = "Название статуса задачи должно содержать от 2 до 40 символов.")
    private String status;
}