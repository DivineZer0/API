package plantime.ru.API.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "list_performers")
public class ListPerformer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_list_performers")
    private Integer idListPerformers;

    @Column(name = "guid_performer", nullable = false, length = 36)
    private String guidPerformer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_task", nullable = false)
    private Task task;

    @Column(name = "time_work")
    private Integer timeWork;

    @Column(name = "price_work", precision = 10, scale = 2)
    private BigDecimal priceWork;

    // getters and setters

    public Integer getIdListPerformers() { return idListPerformers; }
    public void setIdListPerformers(Integer idListPerformers) { this.idListPerformers = idListPerformers; }

    public String getGuidPerformer() { return guidPerformer; }
    public void setGuidPerformer(String guidPerformer) { this.guidPerformer = guidPerformer; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public Integer getTimeWork() { return timeWork; }
    public void setTimeWork(Integer timeWork) { this.timeWork = timeWork; }

    public BigDecimal getPriceWork() { return priceWork; }
    public void setPriceWork(BigDecimal priceWork) { this.priceWork = priceWork; }
}