package plantime.ru.API.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Сущность расписания дежурств/отсутствий сотрудника.
 * Хранит информацию о периодах, сотруднике, типе отсутствия и комментарии.
 */
@Entity
@Table(name = "duty_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DutySchedule {

    /** Уникальный идентификатор записи расписания */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_duty_schedule")
    private Long idDutySchedule;

    /** Сотрудник */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guid_employee", nullable = false)
    private Employee employee;

    /** Дата начала периода */
    @Column(name = "date_start", nullable = false)
    private LocalDate dateStart;

    /** Дата окончания периода */
    @Column(name = "date_end", nullable = false)
    private LocalDate dateEnd;

    /** Тип отсутствия */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_absence", nullable = false)
    private TypeAbsence typeOfAbsence;

    /** Описание */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}