package plantime.ru.API.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "list_of_software")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ListOfSoftware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_list_of_software")
    private Integer idListOfSoftware;

    @Column(name = "id_project", nullable = false)
    private Integer idProject;

    @Column(name = "id_software", nullable = false)
    private Integer idSoftware;
}