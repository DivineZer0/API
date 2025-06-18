package plantime.ru.API.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "project")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_project")
    private Integer idProject;

    @Column(name = "project_name", length = 100)
    private String projectName;

    @Column(name = "guid_executor", length = 36, nullable = false)
    private String guidExecutor;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "date_create")
    private LocalDate dateCreate;

    @Column(name = "time_create")
    private LocalTime timeCreate;

    @Column(name = "date_completion")
    private LocalDate dateCompletion;

    @Column(name = "time_completion")
    private LocalTime timeCompletion;

    @Column(name = "id_customer", nullable = false)
    private Integer idCustomer;

    @Column(name = "id_contract", nullable = false)
    private Integer idContract;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_project_status", nullable = false)
    private ProjectStatus projectStatus;

    @Column(name = "project_price", precision = 10, scale = 2)
    private BigDecimal projectPrice;
}