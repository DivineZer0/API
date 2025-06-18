package plantime.ru.API.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TaskTreeDTO {
    private Integer idTaskTree;
    private Integer idTask;
    private Integer idProject;
    private Integer level;
    private Integer sublevel;
}