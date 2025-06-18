package plantime.ru.API.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_recurrence")
@Getter @Setter @NoArgsConstructor
@EqualsAndHashCode(of = "idTaskRecurrence")
public class TaskRecurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_task_recurrence", nullable = false)
    private Integer idTaskRecurrence;

    @Column(name = "recurrence_pattern", length = 40)
    @Size(min = 2, max = 40, message = "Название шаблона должно быть от 2 до 40 символов")
    private String recurrencePattern;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}