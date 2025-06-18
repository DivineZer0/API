package plantime.ru.API.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Сущность, представляющая запись в журнале логов системы PlanTime.
 */
@Entity
@Table(name = "log")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "idLog")
public class Log {

    /**
     * Уникальный идентификатор записи лога.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log", nullable = false)
    private Integer idLog;

    /**
     * Сотрудник, связанный с действием.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guid_employee", nullable = false)
    @NotNull(message = "Сущность сотрудника обязательна")
    private Employee employee;

    /**
     * Описание действия, выполненного в системе.
     */
    @Column(name = "action", columnDefinition = "TEXT")
    @NotNull(message = "Действие обязательно")
    private String action;

    /**
     * Дата и время создания записи лога.
     * Обязательное поле.
     */
    @Column(name = "created_at", nullable = false)
    @NotNull(message = "Дата создания обязательна")
    private LocalDateTime createdAt;

    /**
     * Конструктор для создания записи лога с сотрудником, действием и временем.
     *
     * @param employee   Сотрудник, связанный с действием.
     * @param action     Описание действия.
     * @param createdAt  Время создания записи.
     */
    public Log(Employee employee, String action, LocalDateTime createdAt) {
        this.employee = employee;
        this.action = action;
        this.createdAt = createdAt;
    }
}