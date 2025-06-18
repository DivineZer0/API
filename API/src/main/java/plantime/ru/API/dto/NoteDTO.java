package plantime.ru.API.dto;

import java.util.List;

public class NoteDTO {
    private Integer idNote;
    private String guidEmployee;
    private String content;
    private String dateAddition;
    private String timeAddition;
    private Integer idTask;
    private List<ListAttachedFilesDTO> files;

    // getters and setters
    public Integer getIdNote() { return idNote; }
    public void setIdNote(Integer idNote) { this.idNote = idNote; }

    public String getGuidEmployee() { return guidEmployee; }
    public void setGuidEmployee(String guidEmployee) { this.guidEmployee = guidEmployee; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDateAddition() { return dateAddition; }
    public void setDateAddition(String dateAddition) { this.dateAddition = dateAddition; }

    public String getTimeAddition() { return timeAddition; }
    public void setTimeAddition(String timeAddition) { this.timeAddition = timeAddition; }

    public Integer getIdTask() { return idTask; }
    public void setIdTask(Integer idTask) { this.idTask = idTask; }

    public List<ListAttachedFilesDTO> getFiles() { return files; }
    public void setFiles(List<ListAttachedFilesDTO> files) { this.files = files; }
}