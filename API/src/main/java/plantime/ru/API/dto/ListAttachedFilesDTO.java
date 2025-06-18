package plantime.ru.API.dto;

public class ListAttachedFilesDTO {
    private Integer idListAttachedFiles;
    private Integer idNote;
    private String pathFile;

    // Getters and Setters
    public Integer getIdListAttachedFiles() { return idListAttachedFiles; }
    public void setIdListAttachedFiles(Integer idListAttachedFiles) { this.idListAttachedFiles = idListAttachedFiles; }

    public Integer getIdNote() { return idNote; }
    public void setIdNote(Integer idNote) { this.idNote = idNote; }

    public String getPathFile() { return pathFile; }
    public void setPathFile(String pathFile) { this.pathFile = pathFile; }
}