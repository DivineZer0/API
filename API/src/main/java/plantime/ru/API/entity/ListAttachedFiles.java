package plantime.ru.API.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "list_attached_files")
public class ListAttachedFiles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_list_attached_files")
    private Integer idListAttachedFiles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_note", nullable = false)
    private Note note;

    @Column(name = "path_file", length = 200)
    private String pathFile;

    // getters and setters

    public Integer getIdListAttachedFiles() { return idListAttachedFiles; }
    public void setIdListAttachedFiles(Integer idListAttachedFiles) { this.idListAttachedFiles = idListAttachedFiles; }

    public Note getNote() { return note; }
    public void setNote(Note note) { this.note = note; }

    public String getPathFile() { return pathFile; }
    public void setPathFile(String pathFile) { this.pathFile = pathFile; }
}