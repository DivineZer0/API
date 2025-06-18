package plantime.ru.API.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "checklist")
public class Checklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_checklist")
    private Integer idChecklist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_task", nullable = false)
    private Task task;

    @Column(name = "content", length = 120)
    private String content;

    @Column(name = "status")
    private Byte status;

    // getters and setters

    public Integer getIdChecklist() { return idChecklist; }
    public void setIdChecklist(Integer idChecklist) { this.idChecklist = idChecklist; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Byte getStatus() { return status; }
    public void setStatus(Byte status) { this.status = status; }
}