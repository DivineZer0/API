package plantime.ru.API.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProjectDTO {
    private Integer idProject;
    private String projectName;
    private String guidExecutor;
    private String description;
    private String dateCreate;
    private String timeCreate;
    private String dateCompletion;
    private String timeCompletion;
    private Integer idCustomer;
    private Integer idContract;
    private Integer idProjectStatus;
    private BigDecimal projectPrice;
}