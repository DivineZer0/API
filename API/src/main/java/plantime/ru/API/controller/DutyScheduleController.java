package plantime.ru.API.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import plantime.ru.API.dto.DutyScheduleDTO;
import plantime.ru.API.dto.ErrorResponse;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.service.AuthService;
import plantime.ru.API.service.DutyScheduleReportService;
import plantime.ru.API.service.DutyScheduleService;

import java.time.LocalDate;
import java.util.List;

/**
 * Контроллер для управления расписанием дежурств сотрудников.
 * Предоставляет REST API для фильтрации, создания, обновления, удаления записей расписания,
 * получения типов отсутствий и экспорта в Excel.
 */
@RestController
@RequestMapping("/api/duty-schedule")
public class DutyScheduleController {

    private final DutyScheduleService dutyScheduleService;
    private final AuthService authService;
    private final DutyScheduleReportService reportService;
    private static final Logger logger = LoggerFactory.getLogger(DutyScheduleController.class);

    public DutyScheduleController(DutyScheduleService dutyScheduleService, AuthService authService, DutyScheduleReportService reportService) {
        this.dutyScheduleService = dutyScheduleService;
        this.authService = authService;
        this.reportService = reportService;
    }

    /**
     * Получить расписание с фильтрацией по отделу, ФИО и типу отсутствия.
     */
    @GetMapping
    public ResponseEntity<?> getFilteredDutySchedules(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String typeOfAbsence
    ) {
        Employee emp = getAuthenticatedEmployee(authHeader, false);
        List<DutyScheduleDTO> schedules = dutyScheduleService.getFilteredSchedules(department, employeeName, typeOfAbsence);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Создать новую запись расписания.
     */
    @PostMapping
    public ResponseEntity<?> createDutySchedule(
            @Valid @RequestBody DutyScheduleDTO dto,
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee employee = getAuthenticatedEmployee(authHeader, true);
        try {
            DutyScheduleDTO created = dutyScheduleService.createSchedule(dto, employee);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Запись для сотрудника в текущий период уже существует", ex.getMessage(), 400));
        }
    }

    /**
     * Обновить запись расписания.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDutySchedule(
            @PathVariable Long id,
            @Valid @RequestBody DutyScheduleDTO dto,
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee employee = getAuthenticatedEmployee(authHeader, true);
        try {
            DutyScheduleDTO updated = dutyScheduleService.updateSchedule(id, dto, employee);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Запись для сотрудника в текущий период уже существует", ex.getMessage(), 400));
        }
    }

    /**
     * Удалить запись расписания.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDutySchedule(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee employee = getAuthenticatedEmployee(authHeader, true);
        try {
            dutyScheduleService.deleteSchedule(id, employee);
            return ResponseEntity.ok("Запись успешно удалена");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Ошибка удаления записи", ex.getMessage(), 400));
        }
    }

    /**
     * Получить все типы отсутствий.
     */
    @GetMapping("/absence-types")
    public ResponseEntity<?> getAllAbsenceTypes(
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee employee = getAuthenticatedEmployee(authHeader, false);
        return ResponseEntity.ok(dutyScheduleService.getAllAbsenceTypes());
    }

    /**
     * Экспорт календаря дежурств/отпусков в Excel.
     */
    @GetMapping("/export/calendar")
    public ResponseEntity<Resource> exportDutyCalendar(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        Employee admin = getAuthenticatedEmployee(authHeader, true);
        List<DutyScheduleDTO> schedules = dutyScheduleService.getFilteredSchedules(null, null, null);
        byte[] data = reportService.generateDutyCalendar(start, end, schedules);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=DutyCalendar.xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    /**
     * Экспорт отпуска/дежурств в виде Excel-файла (*.xlsx) по заданному периоду и фильтрам.
     */
    @GetMapping("/export/vacation")
    public ResponseEntity<Resource> exportVacationFile(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) String department
    ) {
        Employee admin = getAuthenticatedEmployee(authHeader, true);
        if (department != null && department.contains(",")) {
            department = department.substring(0, department.indexOf(",")).trim();
        }
        List<DutyScheduleDTO> schedules = dutyScheduleService.getSchedulesForPeriod(start, end, department);
        byte[] data = reportService.generateVacationCalendar(start, end, schedules);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Schedule of on-call and absence of employees.xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    /**
     * Проверяет токен пользователя и (по необходимости) права администратора.
     * @param authHeader Заголовок Authorization
     * @param requireAdmin Требовать ли права администратора для действия
     * @return Сущность Employee
     * @throws IllegalArgumentException если нет доступа/токена
     */
    private Employee getAuthenticatedEmployee(String authHeader, boolean requireAdmin) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.error("Отсутствует токен в заголовке Authorization");
            throw new IllegalArgumentException("Требуется токен в заголовке Authorization с префиксом Bearer");
        }
        String token = authHeader.substring(7);
        Employee employee = authService.getEmployeeFromToken(token);
        if (requireAdmin && !"Администратор".equals(employee.getEmployeePost().getEmployeePermission().getPermission()) &&
                !"Руководитель отдела".equals(employee.getEmployeePost().getEmployeePermission().getPermission())) {
            logger.error("Доступ запрещён: пользователь не является администратором или руководителем отдела, guid_employee={}", employee.getGuidEmployee());
            throw new IllegalArgumentException("Только администраторы или руководители отдела могут изменять расписание");
        }
        return employee;
    }
}