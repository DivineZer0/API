package plantime.ru.API.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Сущность, представляющая статус проекта в системе PlanTime.
 * Используется для хранения информации о статусах проектов.
 */
@Entity
@Table(name = "project_status")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "idProjectStatus")
public class ProjectStatus {

    /**
     * Уникальный идентификатор статуса проекта.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_project_status", nullable = false)
    private Integer idProjectStatus;

    /**
     * Название статуса проекта.
     */
    @Column(name = "status", nullable = false, length = 40)
    @NotNull(message = "Название статуса проекта обязательно для заполнения.")
    @Size(min = 2, max = 40, message = "Название статуса проекта должно содержать от 2 до 40 символов.")
    private String status;
}