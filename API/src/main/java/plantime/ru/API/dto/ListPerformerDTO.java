package plantime.ru.API.dto;

import java.math.BigDecimal;

public class ListPerformerDTO {
    private Integer idListPerformers;
    private String guidPerformer;
    private Integer idTask;
    private Integer timeWork;
    private BigDecimal priceWork;

    // Getters and Setters
    public Integer getIdListPerformers() { return idListPerformers; }
    public void setIdListPerformers(Integer idListPerformers) { this.idListPerformers = idListPerformers; }

    public String getGuidPerformer() { return guidPerformer; }
    public void setGuidPerformer(String guidPerformer) { this.guidPerformer = guidPerformer; }

    public Integer getIdTask() { return idTask; }
    public void setIdTask(Integer idTask) { this.idTask = idTask; }

    public Integer getTimeWork() { return timeWork; }
    public void setTimeWork(Integer timeWork) { this.timeWork = timeWork; }

    public BigDecimal getPriceWork() { return priceWork; }
    public void setPriceWork(BigDecimal priceWork) { this.priceWork = priceWork; }
}