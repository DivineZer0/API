package plantime.ru.API.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import plantime.ru.API.dto.*;
import plantime.ru.API.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // 1. Загрузка списка по GUID сотрудника или ID проекта (организации)
    @GetMapping("/by-executor/{guidExecutor}")
    public List<TaskDTO> getTasksByExecutor(@PathVariable String guidExecutor) {
        return taskService.getTasksByExecutor(guidExecutor);
    }

    @GetMapping("/by-organization/{idOrganization}")
    public List<TaskDTO> getTasksByOrganization(@PathVariable Integer idOrganization) {
        return taskService.getTasksByOrganization(idOrganization);
    }

    // 2. Фильтрация по организации, типу задачи, контрагенту, статусу, периоду выполнения
    @GetMapping("/filter")
    public List<TaskDTO> filterTasks(
            @RequestParam(required = false) Integer idOrganization,
            @RequestParam(required = false) Integer idTaskType,
            @RequestParam(required = false) String counterparty,
            @RequestParam(required = false) Integer idTaskStatus,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo
    ) {
        return taskService.filterTasks(idOrganization, idTaskType, counterparty, idTaskStatus, dateFrom, dateTo);
    }

    // 3. Генерация Excel-отчёта по задачам
    @GetMapping("/export/excel")
    public ResponseEntity<?> exportTasksToExcel(
            @RequestParam(required = false) Integer idOrganization,
            @RequestParam(required = false) Integer idTaskType,
            @RequestParam(required = false) String counterparty,
            @RequestParam(required = false) Integer idTaskStatus,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo
    ) {
        byte[] excelBytes = taskService.exportTasksToExcel(idOrganization, idTaskType, counterparty, idTaskStatus, dateFrom, dateTo);
        ByteArrayResource resource = new ByteArrayResource(excelBytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tasks.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(excelBytes.length)
                .body(resource);
    }

    // 4. Добавление, редактирование, удаление (контроль по GUID исполнителя)
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskDTO dto) {
        try {
            return ResponseEntity.ok(taskService.createTask(dto));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable int id, @RequestBody TaskDTO dto, @RequestParam String guidExecutor) {
        try {
            return ResponseEntity.ok(taskService.updateTaskIfAllowed(id, dto, guidExecutor));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable int id, @RequestParam String guidExecutor) {
        try {
            taskService.deleteTaskIfAllowed(id, guidExecutor);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // ПО (software) — список ПО задачи, добавить, удалить
    @GetMapping("/{id}/softwares")
    public ResponseEntity<?> getTaskSoftwares(@PathVariable int id) {
        try {
            return ResponseEntity.ok(taskService.getSoftwareByTaskId(id));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/softwares")
    public ResponseEntity<?> addTaskSoftwares(@PathVariable int id, @RequestBody List<Integer> softwareIds) {
        try {
            return ResponseEntity.ok(taskService.addSoftwareToTask(id, softwareIds));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}/softwares/{idSoftware}")
    public ResponseEntity<?> deleteTaskSoftware(@PathVariable int id, @PathVariable int idSoftware) {
        try {
            taskService.deleteSoftwareFromTask(id, idSoftware);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Исполнители (multi)
    @GetMapping("/{id}/performers")
    public ResponseEntity<?> getPerformers(@PathVariable int id) {
        try {
            return ResponseEntity.ok(taskService.getPerformersByTaskId(id));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/performers")
    public ResponseEntity<?> addPerformer(@PathVariable int id, @RequestBody List<ListPerformerDTO> performers) {
        try {
            return ResponseEntity.ok(taskService.addPerformersToTask(id, performers));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/performers/{idListPerformers}")
    public ResponseEntity<?> deletePerformer(@PathVariable int idListPerformers) {
        try {
            taskService.deletePerformerFromTask(idListPerformers);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Notes (чат, с файлами)
    @GetMapping("/{id}/notes")
    public ResponseEntity<?> getNotes(@PathVariable int id) {
        try {
            return ResponseEntity.ok(taskService.getNotesByTaskId(id));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<?> addNote(
            @PathVariable int id,
            @RequestPart("content") String content,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart("guidEmployee") String guidEmployee
    ) {
        try {
            return ResponseEntity.ok(taskService.addNoteToTaskWithFiles(id, content, guidEmployee, files));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Checklist
    @PostMapping("/{id}/checklist")
    public ResponseEntity<?> addChecklistItem(@PathVariable int id, @RequestBody ChecklistDTO item) {
        try {
            return ResponseEntity.ok(taskService.addChecklistItem(id, item));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/{id}/checklist")
    public ResponseEntity<?> getChecklist(@PathVariable int id) {
        try {
            return ResponseEntity.ok(taskService.getChecklistByTaskId(id));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/checklist/{checklistId}")
    public ResponseEntity<?> updateChecklistItem(@PathVariable int checklistId, @RequestBody ChecklistDTO item) {
        try {
            return ResponseEntity.ok(taskService.updateChecklistItem(checklistId, item));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/checklist/{checklistId}")
    public ResponseEntity<?> deleteChecklistItem(@PathVariable int checklistId) {
        try {
            taskService.deleteChecklistItem(checklistId);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Файлы к заметке (асинхронно)
    @PostMapping("/notes/{noteId}/files")
    public ResponseEntity<?> addFileToNote(@PathVariable int noteId, @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(taskService.addFileToNote(noteId, file));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/notes/{noteId}/files")
    public ResponseEntity<?> getFilesByNote(@PathVariable int noteId) {
        try {
            return ResponseEntity.ok(taskService.getFilesByNoteId(noteId));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // --- Услуги ---
    @GetMapping("/{id}/services")
    public ResponseEntity<?> getTaskServices(@PathVariable int id) {
        try {
            return ResponseEntity.ok(taskService.getServicesByTaskId(id));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/services")
    public ResponseEntity<?> addTaskService(@PathVariable int id, @RequestBody ServiceDTO dto) {
        try {
            return ResponseEntity.ok(taskService.addServiceToTask(id, dto));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{id}/services/{idListServices}")
    public ResponseEntity<?> updateTaskService(@PathVariable int id, @PathVariable int idListServices, @RequestBody ServiceDTO dto) {
        try {
            return ResponseEntity.ok(taskService.updateServiceOfTask(id, idListServices, dto));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}/services/{idListServices}")
    public ResponseEntity<?> deleteTaskService(@PathVariable int id, @PathVariable int idListServices) {
        try {
            taskService.deleteServiceFromTask(id, idListServices);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}