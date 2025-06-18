package plantime.ru.API.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_tree")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TaskTree {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_task_tree")
    private Integer idTaskTree;

    @Column(name = "id_task", nullable = false)
    private Integer idTask;

    @Column(name = "id_project", nullable = false)
    private Integer idProject;

    @Column(name = "level")
    private Integer level;

    @Column(name = "sublevel")
    private Integer sublevel;
}