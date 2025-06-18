package plantime.ru.API.dto;

public class ChecklistDTO {
    private Integer idChecklist;
    private Integer idTask;
    private String content;
    private Byte status;

    // Getters and Setters
    public Integer getIdChecklist() { return idChecklist; }
    public void setIdChecklist(Integer idChecklist) { this.idChecklist = idChecklist; }

    public Integer getIdTask() { return idTask; }
    public void setIdTask(Integer idTask) { this.idTask = idTask; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Byte getStatus() { return status; }
    public void setStatus(Byte status) { this.status = status; }
}