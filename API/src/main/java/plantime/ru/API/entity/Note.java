package plantime.ru.API.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "note")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_note")
    private Integer idNote;

    @Column(name = "guid_employee", nullable = false, length = 36)
    private String guidEmployee;

    @Column(name = "content", length = 250)
    private String content;

    @Column(name = "date_addition")
    private LocalDate dateAddition;

    @Column(name = "time_addition")
    private LocalTime timeAddition;

    // --- Relations ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_task", nullable = false)
    private Task task;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListAttachedFiles> attachedFiles;

    // getters and setters

    public Integer getIdNote() { return idNote; }
    public void setIdNote(Integer idNote) { this.idNote = idNote; }

    public String getGuidEmployee() { return guidEmployee; }
    public void setGuidEmployee(String guidEmployee) { this.guidEmployee = guidEmployee; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDate getDateAddition() { return dateAddition; }
    public void setDateAddition(LocalDate dateAddition) { this.dateAddition = dateAddition; }

    public LocalTime getTimeAddition() { return timeAddition; }
    public void setTimeAddition(LocalTime timeAddition) { this.timeAddition = timeAddition; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public List<ListAttachedFiles> getAttachedFiles() { return attachedFiles; }
    public void setAttachedFiles(List<ListAttachedFiles> attachedFiles) { this.attachedFiles = attachedFiles; }
}