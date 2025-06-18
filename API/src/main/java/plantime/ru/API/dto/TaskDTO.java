package plantime.ru.API.dto;

import java.math.BigDecimal;

public class TaskDTO {
    private Integer idTask;
    private String taskName;
    private String guidExecutor;
    private String description;
    private String dateCreate;
    private String timeCreate;
    private String dateCompletion;
    private String timeCompletion;
    private Integer idTaskStatus;
    private BigDecimal taskPrice;
    private Integer idTaskType;
    private Integer idOrganization;

    // Getters and Setters
    public Integer getIdTask() { return idTask; }
    public void setIdTask(Integer idTask) { this.idTask = idTask; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getGuidExecutor() { return guidExecutor; }
    public void setGuidExecutor(String guidExecutor) { this.guidExecutor = guidExecutor; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDateCreate() { return dateCreate; }
    public void setDateCreate(String dateCreate) { this.dateCreate = dateCreate; }

    public String getTimeCreate() { return timeCreate; }
    public void setTimeCreate(String timeCreate) { this.timeCreate = timeCreate; }

    public String getDateCompletion() { return dateCompletion; }
    public void setDateCompletion(String dateCompletion) { this.dateCompletion = dateCompletion; }

    public String getTimeCompletion() { return timeCompletion; }
    public void setTimeCompletion(String timeCompletion) { this.timeCompletion = timeCompletion; }

    public Integer getIdTaskStatus() { return idTaskStatus; }
    public void setIdTaskStatus(Integer idTaskStatus) { this.idTaskStatus = idTaskStatus; }

    public BigDecimal getTaskPrice() { return taskPrice; }
    public void setTaskPrice(BigDecimal taskPrice) { this.taskPrice = taskPrice; }

    public Integer getIdTaskType() { return idTaskType; }
    public void setIdTaskType(Integer idTaskType) { this.idTaskType = idTaskType; }

    public Integer getIdOrganization() { return idOrganization; }
    public void setIdOrganization(Integer idOrganization) { this.idOrganization = idOrganization; }
}