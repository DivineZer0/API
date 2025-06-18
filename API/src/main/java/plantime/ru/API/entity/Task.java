package plantime.ru.API.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_task")
    private Integer idTask;

    @NotBlank(message = "Название задачи обязательно")
    @Size(max = 100, message = "Название задачи не должно превышать 100 символов")
    @Column(name = "task_name", length = 100, nullable = false)
    private String taskName;

    @NotBlank(message = "Идентификатор исполнителя обязателен")
    @Column(name = "guid_executor", nullable = false, length = 36)
    private String guidExecutor;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Дата создания обязательна")
    @Column(name = "date_create", nullable = false)
    private LocalDate dateCreate;

    @NotNull(message = "Время создания обязательно")
    @Column(name = "time_create", nullable = false)
    private LocalTime timeCreate;

    @Column(name = "date_completion")
    private LocalDate dateCompletion;

    @Column(name = "time_completion")
    private LocalTime timeCompletion;

    @NotNull(message = "Статус задачи обязателен")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_task_status", nullable = false)
    private TaskStatus taskStatus;

    @Digits(integer = 10, fraction = 2, message = "Некорректный формат цены")
    @Column(name = "task_price", precision = 10, scale = 2)
    private BigDecimal taskPrice;

    @NotNull(message = "Организация обязательна")
    @Column(name = "id_organization", nullable = false)
    private Integer idOrganization;

    @NotNull(message = "Тип задачи обязателен")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_task_type", nullable = false)
    private TaskType taskType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_task_recurrence")
    private TaskRecurrence taskRecurrence;

    // --- Relations ---
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Note> notes = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Checklist> checklist = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListPerformer> performers = new ArrayList<>();

    // --- Constructors ---
    public Task() {
        this.dateCreate = LocalDate.now();
        this.timeCreate = LocalTime.now();
    }

    // --- Getters/Setters ---
    public Integer getIdTask() { return idTask; }
    public void setIdTask(Integer idTask) { this.idTask = idTask; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getGuidExecutor() { return guidExecutor; }
    public void setGuidExecutor(String guidExecutor) { this.guidExecutor = guidExecutor; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDateCreate() { return dateCreate; }
    public void setDateCreate(LocalDate dateCreate) { this.dateCreate = dateCreate; }

    public LocalTime getTimeCreate() { return timeCreate; }
    public void setTimeCreate(LocalTime timeCreate) { this.timeCreate = timeCreate; }

    public LocalDate getDateCompletion() { return dateCompletion; }
    public void setDateCompletion(LocalDate dateCompletion) { this.dateCompletion = dateCompletion; }

    public LocalTime getTimeCompletion() { return timeCompletion; }
    public void setTimeCompletion(LocalTime timeCompletion) { this.timeCompletion = timeCompletion; }

    public TaskStatus getTaskStatus() { return taskStatus; }
    public void setTaskStatus(TaskStatus taskStatus) { this.taskStatus = taskStatus; }

    public BigDecimal getTaskPrice() { return taskPrice; }
    public void setTaskPrice(BigDecimal taskPrice) { this.taskPrice = taskPrice; }

    public Integer getIdOrganization() { return idOrganization; }
    public void setIdOrganization(Integer idOrganization) { this.idOrganization = idOrganization; }

    public TaskType getTaskType() { return taskType; }
    public void setTaskType(TaskType taskType) { this.taskType = taskType; }

    public TaskRecurrence getTaskRecurrence() { return taskRecurrence; }
    public void setTaskRecurrence(TaskRecurrence taskRecurrence) { this.taskRecurrence = taskRecurrence; }

    public List<Note> getNotes() { return notes; }
    public void setNotes(List<Note> notes) { this.notes = notes; }

    public List<Checklist> getChecklist() { return checklist; }
    public void setChecklist(List<Checklist> checklist) { this.checklist = checklist; }

    public List<ListPerformer> getPerformers() { return performers; }
    public void setPerformers(List<ListPerformer> performers) { this.performers = performers; }

    // --- Utility methods ---
    public void addNote(Note note) {
        notes.add(note);
        note.setTask(this);
    }

    public void removeNote(Note note) {
        notes.remove(note);
        note.setTask(null);
    }

    public void addChecklistItem(Checklist item) {
        checklist.add(item);
        item.setTask(this);
    }

    public void addPerformer(ListPerformer performer) {
        performers.add(performer);
        performer.setTask(this);
    }

    @PrePersist
    private void onCreate() {
        if (dateCreate == null) {
            dateCreate = LocalDate.now();
        }
        if (timeCreate == null) {
            timeCreate = LocalTime.now();
        }
    }
}