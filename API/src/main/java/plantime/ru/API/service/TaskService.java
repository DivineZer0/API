package plantime.ru.API.service;

import plantime.ru.API.dto.*;
import plantime.ru.API.entity.*;
import plantime.ru.API.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private ChecklistRepository checklistRepository;
    @Autowired
    private ListPerformerRepository performerRepository;
    @Autowired
    private ListAttachedFilesRepository filesRepository;
    @Autowired
    private ListOfSoftwareRepository softwareRepository;
    @Autowired
    private SoftwareRepository softwareDictRepository;
    @Autowired
    private ListServicesRepository listServicesRepository;
    @Autowired
    private ServiceRepository serviceDictRepository;

    // --- 1. Поиск по GUID сотрудника ---
    public List<TaskDTO> getTasksByExecutor(String guidExecutor) {
        return taskRepository.findAll().stream()
                .filter(t -> t.getGuidExecutor() != null && t.getGuidExecutor().equals(guidExecutor))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // --- 1. Поиск по ID организации ---
    public List<TaskDTO> getTasksByOrganization(Integer idOrganization) {
        return taskRepository.findAll().stream()
                .filter(t -> Objects.equals(t.getIdOrganization(), idOrganization))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // --- 2. Фильтрация ---
    public List<TaskDTO> filterTasks(Integer idOrganization, Integer idTaskType, String counterparty,
                                     Integer idTaskStatus, String dateFrom, String dateTo) {
        return taskRepository.findAll().stream()
                .filter(t -> idOrganization == null || Objects.equals(t.getIdOrganization(), idOrganization))
                .filter(t -> idTaskType == null || (t.getTaskType() != null && Objects.equals(t.getTaskType().getIdTaskType(), idTaskType)))
                .filter(t -> counterparty == null || (t.getGuidExecutor() != null && t.getGuidExecutor().equals(counterparty)))
                .filter(t -> idTaskStatus == null || (t.getTaskStatus() != null && Objects.equals(t.getTaskStatus().getIdTaskStatus(), idTaskStatus)))
                .filter(t -> {
                    if (dateFrom == null && dateTo == null) return true;
                    LocalDate dateStart = null, dateEnd = null;
                    try {
                        if (dateFrom != null) dateStart = LocalDate.parse(dateFrom);
                        if (dateTo != null) dateEnd = LocalDate.parse(dateTo);
                    } catch (DateTimeParseException ignore) {}
                    LocalDate taskDate = t.getDateCompletion();
                    if (taskDate == null) return false;
                    if (dateStart != null && taskDate.isBefore(dateStart)) return false;
                    if (dateEnd != null && taskDate.isAfter(dateEnd)) return false;
                    return true;
                })
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // --- 3. Генерация Excel ---
    public byte[] exportTasksToExcel(Integer idOrganization, Integer idTaskType, String counterparty,
                                     Integer idTaskStatus, String dateFrom, String dateTo) {
        List<TaskDTO> tasks = filterTasks(idOrganization, idTaskType, counterparty, idTaskStatus, dateFrom, dateTo);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Tasks");
            int rowIdx = 0;
            Row header = sheet.createRow(rowIdx++);
            String[] headers = {"ID", "Название", "Исполнитель", "Описание", "Дата создания", "Дата завершения", "Статус", "Тип", "Организация", "Стоимость"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            for (TaskDTO t : tasks) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getIdTask() != null ? t.getIdTask() : 0);
                row.createCell(1).setCellValue(t.getTaskName() == null ? "" : t.getTaskName());
                row.createCell(2).setCellValue(t.getGuidExecutor() == null ? "" : t.getGuidExecutor());
                row.createCell(3).setCellValue(t.getDescription() == null ? "" : t.getDescription());
                row.createCell(4).setCellValue(t.getDateCreate() == null ? "" : t.getDateCreate());
                row.createCell(5).setCellValue(t.getDateCompletion() == null ? "" : t.getDateCompletion());
                row.createCell(6).setCellValue(t.getIdTaskStatus() == null ? "" : t.getIdTaskStatus().toString());
                row.createCell(7).setCellValue(t.getIdTaskType() == null ? "" : t.getIdTaskType().toString());
                row.createCell(8).setCellValue(t.getIdOrganization() == null ? "" : t.getIdOrganization().toString());
                row.createCell(9).setCellValue(t.getTaskPrice() == null ? 0 : t.getTaskPrice().doubleValue());
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка генерации Excel: " + ex.getMessage(), ex);
        }
    }

    // --- ПО (software) ---
    public List<SoftwareDTO> getSoftwareByTaskId(int taskId) {
        List<ListOfSoftware> list = softwareRepository.findByIdProject(taskId);
        List<Integer> ids = list.stream().map(ListOfSoftware::getIdSoftware).collect(Collectors.toList());
        List<Software> allSoft = softwareDictRepository.findAllById(ids);
        return allSoft.stream().map(SoftwareDTO::fromEntity).collect(Collectors.toList());
    }

    public List<SoftwareDTO> addSoftwareToTask(int taskId, List<Integer> softwareIds) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        for (Integer softId : softwareIds) {
            if (!softwareRepository.existsByIdProjectAndIdSoftware(taskId, softId)) {
                ListOfSoftware entity = new ListOfSoftware();
                entity.setIdProject(taskId);
                entity.setIdSoftware(softId);
                softwareRepository.save(entity);
            }
        }
        return getSoftwareByTaskId(taskId);
    }

    public void deleteSoftwareFromTask(int taskId, int idSoftware) {
        ListOfSoftware los = softwareRepository.findByIdProjectAndIdSoftware(taskId, idSoftware).orElse(null);
        if (los != null) softwareRepository.delete(los);
    }

    // --- Исполнители (multi) ---
    public List<ListPerformerDTO> addPerformersToTask(int taskId, List<ListPerformerDTO> dtos) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        List<ListPerformerDTO> result = new ArrayList<>();
        for (ListPerformerDTO dto : dtos) {
            ListPerformer performer = new ListPerformer();
            performer.setGuidPerformer(dto.getGuidPerformer());
            performer.setTask(task);
            performer.setTimeWork(dto.getTimeWork());
            performer.setPriceWork(dto.getPriceWork());
            performerRepository.save(performer);
            result.add(toDTO(performer));
        }
        return result;
    }

    public void deletePerformerFromTask(int idListPerformers) {
        performerRepository.deleteById(idListPerformers);
    }

    // --- Notes (multipart, чат) ---
    public NoteDTO addNoteToTaskWithFiles(int taskId, String content, String guidEmployee, List<MultipartFile> files) throws IOException {
        if (content == null || content.trim().isEmpty()) throw new IllegalArgumentException("Содержимое заметки не должно быть пустым");
        Task task = taskRepository.findById(taskId).orElseThrow();
        Note note = new Note();
        note.setContent(content);
        note.setGuidEmployee(guidEmployee);
        note.setTask(task);
        note.setDateAddition(LocalDate.now());
        note.setTimeAddition(LocalTime.now());
        Note saved = noteRepository.save(note);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                // save file to storage and get path
                String path = saveNoteFile(file, saved.getIdNote());
                ListAttachedFiles laf = new ListAttachedFiles();
                laf.setNote(saved);
                laf.setPathFile(path);
                filesRepository.save(laf);
            }
        }
        return toDTO(saved);
    }

    private String saveNoteFile(MultipartFile file, Integer noteId) throws IOException {
        // Пример: сохраняем файл на диск, возвращаем путь
        String uploadDir = "uploaded_files/notes/" + noteId + "/";
        java.io.File dir = new java.io.File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        String filePath = uploadDir + file.getOriginalFilename();
        file.transferTo(new java.io.File(filePath));
        return filePath;
    }

    // --- Checklist ---
    public ChecklistDTO addChecklistItem(int taskId, ChecklistDTO dto) {
        if (dto == null || dto.getContent() == null || dto.getContent().trim().isEmpty())
            throw new IllegalArgumentException("Текст чеклиста не должен быть пустым");
        Task task = taskRepository.findById(taskId).orElseThrow();
        Checklist entity = toEntity(dto);
        entity.setTask(task);
        Checklist saved = checklistRepository.save(entity);
        return toDTO(saved);
    }

    public List<ChecklistDTO> getChecklistByTaskId(int taskId) {
        return checklistRepository.findByTask_IdTask(taskId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ChecklistDTO updateChecklistItem(int checklistId, ChecklistDTO dto) {
        Optional<Checklist> found = checklistRepository.findById(checklistId);
        if (!found.isPresent()) throw new IllegalArgumentException("Чеклист с id " + checklistId + " не найден");
        if (dto.getContent() == null || dto.getContent().trim().isEmpty())
            throw new IllegalArgumentException("Текст чеклиста не должен быть пустым");
        Checklist entity = found.get();
        entity.setContent(dto.getContent());
        entity.setStatus(dto.getStatus());
        Checklist saved = checklistRepository.save(entity);
        return toDTO(saved);
    }

    public void deleteChecklistItem(int checklistId) {
        if (!checklistRepository.existsById(checklistId)) {
            throw new IllegalArgumentException("Чеклист с id " + checklistId + " не найден");
        }
        checklistRepository.deleteById(checklistId);
    }

    // --- Performers (single, legacy) ---
    public ListPerformerDTO addPerformerToTask(int taskId, ListPerformerDTO dto) {
        if (dto == null || dto.getGuidPerformer() == null || dto.getGuidPerformer().trim().isEmpty())
            throw new IllegalArgumentException("guidPerformer не может быть пустым");
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Задача с id " + taskId + " не найдена"));

        boolean alreadyExists = performerRepository.findByTask_IdTask(taskId).stream()
                .anyMatch(p -> p.getGuidPerformer().equals(dto.getGuidPerformer()));
        if (alreadyExists)
            throw new IllegalArgumentException("Такой исполнитель уже добавлен к задаче");

        ListPerformer entity = toEntity(dto);
        entity.setTask(task);
        ListPerformer saved = performerRepository.save(entity);
        return toDTO(saved);
    }

    public List<ListPerformerDTO> getPerformersByTaskId(int taskId) {
        return performerRepository.findByTask_IdTask(taskId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    // --- FILES (к заметке) ---
    public ListAttachedFilesDTO addFileToNote(int noteId, MultipartFile file) throws IOException {
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new IllegalArgumentException("Заметка с id " + noteId + " не найдена"));
        String path = saveNoteFile(file, noteId);
        ListAttachedFiles laf = new ListAttachedFiles();
        laf.setNote(note);
        laf.setPathFile(path);
        filesRepository.save(laf);
        return toDTO(laf);
    }

    public List<ListAttachedFilesDTO> getFilesByNoteId(int noteId) {
        return filesRepository.findByNote_IdNote(noteId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    // --- Services (list_services) ---
    public List<ServiceDTO> getServicesByTaskId(int taskId) {
        List<ListServices> list = listServicesRepository.findByIdTask(taskId);
        List<Integer> ids = list.stream().map(ListServices::getIdService).collect(Collectors.toList());
        List<plantime.ru.API.entity.Service> dict = serviceDictRepository.findAllById(ids);
        Map<Integer, plantime.ru.API.entity.Service> dictMap = dict.stream().collect(Collectors.toMap(plantime.ru.API.entity.Service::getIdService, s -> s));
        List<ServiceDTO> result = new ArrayList<>();
        for (ListServices ls : list) {
            plantime.ru.API.entity.Service s = dictMap.get(ls.getIdService());
            if (s != null) {
                result.add(new ServiceDTO(ls.getIdListServices(), s.getIdService(), s.getService(), ls.getCount(), s.getPrice()));
            }
        }
        return result;
    }

    public ServiceDTO addServiceToTask(int taskId, ServiceDTO dto) {
        ListServices ls = new ListServices();
        ls.setIdTask(taskId);
        ls.setIdService(dto.getIdService());
        ls.setCount(dto.getCount());
        listServicesRepository.save(ls);
        plantime.ru.API.entity.Service s = serviceDictRepository.findById(dto.getIdService()).orElseThrow();
        return new ServiceDTO(ls.getIdListServices(), s.getIdService(), s.getService(), ls.getCount(), s.getPrice());
    }

    public ServiceDTO updateServiceOfTask(int taskId, int idListServices, ServiceDTO dto) {
        ListServices ls = listServicesRepository.findById(idListServices).orElseThrow();
        ls.setIdService(dto.getIdService());
        ls.setCount(dto.getCount());
        listServicesRepository.save(ls);
        plantime.ru.API.entity.Service s = serviceDictRepository.findById(dto.getIdService()).orElseThrow();
        return new ServiceDTO(ls.getIdListServices(), s.getIdService(), s.getService(), ls.getCount(), s.getPrice());
    }

    public void deleteServiceFromTask(int taskId, int idListServices) {
        listServicesRepository.deleteById(idListServices);
    }

    // --- CRUD, валидация, маппинг (оставьте как у вас, +обновите под новые поля) ---

    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public TaskDTO getTaskById(int id) {
        return taskRepository.findById(id).map(this::toDTO).orElse(null);
    }

    public TaskDTO createTask(TaskDTO dto) {
        validateTaskDTO(dto, true);
        checkDuplicateTask(dto);
        Task entity = toEntity(dto);
        Task saved;
        try {
            saved = taskRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Ошибка целостности данных при создании задачи: " + ex.getMessage());
        }
        return toDTO(saved);
    }

    public TaskDTO updateTaskIfAllowed(int id, TaskDTO dto, String guidExecutor) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Задача не найдена"));
        if (task.getGuidExecutor() == null || !task.getGuidExecutor().equals(guidExecutor)) {
            throw new IllegalArgumentException("Нет прав на изменение задачи");
        }
        return updateTask(id, dto);
    }

    public void deleteTaskIfAllowed(int id, String guidExecutor) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Задача не найдена"));
        if (task.getGuidExecutor() == null || !task.getGuidExecutor().equals(guidExecutor)) {
            throw new IllegalArgumentException("Нет прав на удаление задачи");
        }
        deleteTask(id);
    }

    public TaskDTO updateTask(int id, TaskDTO dto) {
        Optional<Task> found = taskRepository.findById(id);
        if (!found.isPresent()) throw new IllegalArgumentException("Задача с id " + id + " не найдена");
        validateTaskDTO(dto, false);
        if (dto.getTaskName() != null && dto.getGuidExecutor() != null) {
            Task duplicate = taskRepository.findAll().stream()
                    .filter(t -> t.getTaskName().equals(dto.getTaskName())
                            && t.getGuidExecutor().equals(dto.getGuidExecutor())
                            && !t.getIdTask().equals(id))
                    .findFirst().orElse(null);
            if (duplicate != null) {
                throw new IllegalArgumentException("Задача с таким названием и исполнителем уже существует");
            }
        }

        Task entity = found.get();
        entity.setTaskName(dto.getTaskName());
        entity.setGuidExecutor(dto.getGuidExecutor());
        entity.setDescription(dto.getDescription());
        entity.setTaskPrice(dto.getTaskPrice());
        entity.setIdOrganization(dto.getIdOrganization());
        if (dto.getDateCreate() != null) entity.setDateCreate(parseDate(dto.getDateCreate()));
        if (dto.getTimeCreate() != null) entity.setTimeCreate(parseTime(dto.getTimeCreate()));
        if (dto.getDateCompletion() != null) entity.setDateCompletion(parseDate(dto.getDateCompletion()));
        if (dto.getTimeCompletion() != null) entity.setTimeCompletion(parseTime(dto.getTimeCompletion()));
        Task saved;
        try {
            saved = taskRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Ошибка целостности данных при обновлении задачи: " + ex.getMessage());
        }
        return toDTO(saved);
    }

    public void deleteTask(int id) {
        if (!taskRepository.existsById(id)) {
            throw new IllegalArgumentException("Задача с id " + id + " не найдена");
        }
        taskRepository.deleteById(id);
    }

    // NOTES (без multipart, для совместимости)
    public NoteDTO addNoteToTask(int taskId, NoteDTO noteDto) {
        if (noteDto == null || noteDto.getContent() == null || noteDto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Содержимое заметки не должно быть пустым");
        }
        Task task = taskRepository.findById(taskId).orElseThrow();
        Note entity = toEntity(noteDto);
        entity.setTask(task);
        Note saved = noteRepository.save(entity);
        return toDTO(saved);
    }

    public List<NoteDTO> getNotesByTaskId(int taskId) {
        return noteRepository.findByTask_IdTask(taskId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ==== ВАЛИДАЦИЯ ====
    private void validateTaskDTO(TaskDTO dto, boolean isCreate) {
        if (dto == null) throw new IllegalArgumentException("Данные задачи не могут быть пустыми");
        if (dto.getTaskName() == null || dto.getTaskName().trim().isEmpty())
            throw new IllegalArgumentException("Название задачи обязательно");
        if (dto.getTaskName().length() > 100)
            throw new IllegalArgumentException("Название задачи не должно превышать 100 символов");
        if (dto.getGuidExecutor() == null || dto.getGuidExecutor().trim().isEmpty())
            throw new IllegalArgumentException("guidExecutor обязателен");
        if (dto.getGuidExecutor().length() > 36)
            throw new IllegalArgumentException("guidExecutor не должен превышать 36 символов");
        if (dto.getDescription() != null && dto.getDescription().length() > 200)
            throw new IllegalArgumentException("Описание не должно превышать 200 символов");
        if (dto.getIdTaskStatus() == null)
            throw new IllegalArgumentException("idTaskStatus обязателен");
        if (dto.getIdTaskType() == null)
            throw new IllegalArgumentException("idTaskType обязателен");
        if (dto.getIdOrganization() == null)
            throw new IllegalArgumentException("idOrganization обязателен");
        if (dto.getTaskPrice() != null && dto.getTaskPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Цена задачи не может быть отрицательной");
    }

    private void checkDuplicateTask(TaskDTO dto) {
        List<Task> all = taskRepository.findAll();
        boolean exists = all.stream().anyMatch(t ->
                t.getTaskName().equals(dto.getTaskName())
                        && t.getGuidExecutor().equals(dto.getGuidExecutor())
        );
        if (exists)
            throw new IllegalArgumentException("Задача с таким названием и исполнителем уже существует");
    }

    // ==== Маппинг DTO <-> Entity ====
    private TaskDTO toDTO(Task t) {
        TaskDTO dto = new TaskDTO();
        dto.setIdTask(t.getIdTask());
        dto.setTaskName(t.getTaskName());
        dto.setGuidExecutor(t.getGuidExecutor());
        dto.setDescription(t.getDescription());
        if (t.getDateCreate() != null) dto.setDateCreate(t.getDateCreate().toString());
        if (t.getTimeCreate() != null) dto.setTimeCreate(t.getTimeCreate().toString());
        if (t.getDateCompletion() != null) dto.setDateCompletion(t.getDateCompletion().toString());
        if (t.getTimeCompletion() != null) dto.setTimeCompletion(t.getTimeCompletion().toString());
        dto.setTaskPrice(t.getTaskPrice());
        dto.setIdOrganization(t.getIdOrganization());
        dto.setIdTaskStatus(t.getTaskStatus() != null ? t.getTaskStatus().getIdTaskStatus() : null);
        dto.setIdTaskType(t.getTaskType() != null ? t.getTaskType().getIdTaskType() : null);
        return dto;
    }

    private Task toEntity(TaskDTO dto) {
        Task t = new Task();
        t.setIdTask(dto.getIdTask());
        t.setTaskName(dto.getTaskName());
        t.setGuidExecutor(dto.getGuidExecutor());
        t.setDescription(dto.getDescription());
        t.setTaskPrice(dto.getTaskPrice());
        t.setIdOrganization(dto.getIdOrganization());
        if (dto.getDateCreate() != null) t.setDateCreate(parseDate(dto.getDateCreate()));
        if (dto.getTimeCreate() != null) t.setTimeCreate(parseTime(dto.getTimeCreate()));
        if (dto.getDateCompletion() != null) t.setDateCompletion(parseDate(dto.getDateCompletion()));
        if (dto.getTimeCompletion() != null) t.setTimeCompletion(parseTime(dto.getTimeCompletion()));
        return t;
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (Exception e) {
            throw new IllegalArgumentException("Некорректный формат даты: " + date);
        }
    }

    private LocalTime parseTime(String time) {
        try {
            return LocalTime.parse(time);
        } catch (Exception e) {
            throw new IllegalArgumentException("Некорректный формат времени: " + time);
        }
    }

    private NoteDTO toDTO(Note n) {
        NoteDTO dto = new NoteDTO();
        dto.setIdNote(n.getIdNote());
        dto.setGuidEmployee(n.getGuidEmployee());
        dto.setContent(n.getContent());
        if (n.getDateAddition() != null) dto.setDateAddition(n.getDateAddition().toString());
        if (n.getTimeAddition() != null) dto.setTimeAddition(n.getTimeAddition().toString());
        dto.setIdTask(n.getTask() != null ? n.getTask().getIdTask() : null);
        // files
        if (n.getAttachedFiles() != null) {
            dto.setFiles(n.getAttachedFiles().stream().map(this::toDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private Note toEntity(NoteDTO dto) {
        Note n = new Note();
        n.setIdNote(dto.getIdNote());
        n.setGuidEmployee(dto.getGuidEmployee());
        n.setContent(dto.getContent());
        return n;
    }

    private ChecklistDTO toDTO(Checklist c) {
        ChecklistDTO dto = new ChecklistDTO();
        dto.setIdChecklist(c.getIdChecklist());
        dto.setIdTask(c.getTask() != null ? c.getTask().getIdTask() : null);
        dto.setContent(c.getContent());
        dto.setStatus(c.getStatus());
        return dto;
    }

    private Checklist toEntity(ChecklistDTO dto) {
        Checklist c = new Checklist();
        c.setIdChecklist(dto.getIdChecklist());
        c.setContent(dto.getContent());
        c.setStatus(dto.getStatus());
        return c;
    }

    private ListPerformerDTO toDTO(ListPerformer p) {
        ListPerformerDTO dto = new ListPerformerDTO();
        dto.setIdListPerformers(p.getIdListPerformers());
        dto.setGuidPerformer(p.getGuidPerformer());
        dto.setIdTask(p.getTask() != null ? p.getTask().getIdTask() : null);
        dto.setTimeWork(p.getTimeWork());
        dto.setPriceWork(p.getPriceWork());
        return dto;
    }

    private ListPerformer toEntity(ListPerformerDTO dto) {
        ListPerformer p = new ListPerformer();
        p.setIdListPerformers(dto.getIdListPerformers());
        p.setGuidPerformer(dto.getGuidPerformer());
        p.setTimeWork(dto.getTimeWork());
        p.setPriceWork(dto.getPriceWork());
        return p;
    }

    private ListAttachedFilesDTO toDTO(ListAttachedFiles f) {
        ListAttachedFilesDTO dto = new ListAttachedFilesDTO();
        dto.setIdListAttachedFiles(f.getIdListAttachedFiles());
        dto.setIdNote(f.getNote() != null ? f.getNote().getIdNote() : null);
        dto.setPathFile(f.getPathFile());
        return dto;
    }
}