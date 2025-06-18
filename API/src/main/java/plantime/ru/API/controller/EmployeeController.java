package plantime.ru.API.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import plantime.ru.API.dto.EmployeeDTO;
import plantime.ru.API.dto.ErrorResponse;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.service.AuthService;
import plantime.ru.API.service.EmployeeService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

/**
 * Контроллер для управления сотрудниками в API PlanTime.
 */
@RestController
@RequestMapping("/api/employee/employees/")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    public EmployeeController(EmployeeService employeeService, AuthService authService) {
        this.employeeService = employeeService;
        this.authService = authService;
    }

    /**
     * Получает список сотрудников с фильтрацией и поиском.
     * Доступно всем аутентифицированным пользователям.
     */
    @GetMapping
    public ResponseEntity<?> getAllEmployees(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Integer genderId,
            @RequestParam(required = false) Integer postId,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) BigDecimal minHourlyRate,
            @RequestParam(required = false) BigDecimal maxHourlyRate,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String search) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, false);
        List<EmployeeDTO> employees = employeeService.getAllEmployees(
                authEmployee, startDate, endDate, genderId, postId, statusId,
                minHourlyRate, maxHourlyRate, department, search);
        if (employees.isEmpty()) {
            return ResponseEntity.ok(new ErrorResponse("Сотрудники отсутствуют", "Список пуст", 200));
        }
        return ResponseEntity.ok(employees);
    }

    /**
     * Получить краткий список сотрудников по наименованию отдела: (Фамилия Имя Отчество (Номер телефона)).
     */
    @GetMapping("/by-department/{department}/short")
    public ResponseEntity<?> getShortEmployeesByDepartment(
            @PathVariable String department,
            @RequestHeader("Authorization") String authHeader) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, false);
        List<String> list = employeeService.getShortEmployeesByDepartment(department);
        return ResponseEntity.ok(list);
    }

    /**
     * Создаёт нового сотрудника (только Администратор или Руководитель отдела).
     * Фото обязательно (multipart).
     */
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createEmployee(
            @RequestPart("employee") @Valid EmployeeDTO employeeDTO,
            @RequestPart("photo") MultipartFile photo,
            @RequestHeader("Authorization") String authHeader) throws IOException {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        EmployeeDTO savedEmployee = employeeService.createEmployee(employeeDTO, photo, authEmployee);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);
    }

    /**
     * Обновляет данные сотрудника (только Администратор или Руководитель отдела).
     * Фото можно не передавать.
     */
    @PutMapping(value = "/{guid}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateEmployee(
            @PathVariable String guid,
            @RequestPart("employee") @Valid EmployeeDTO employeeDTO,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @RequestHeader("Authorization") String authHeader) throws IOException {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(guid, employeeDTO, photo, authEmployee);
        return ResponseEntity.ok(updatedEmployee);
    }

    /**
     * Удаляет сотрудника (только Администратор или Руководитель отдела).
     */
    @DeleteMapping("/{guid}")
    public ResponseEntity<String> deleteEmployee(
            @PathVariable String guid,
            @RequestHeader("Authorization") String authHeader) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        employeeService.deleteEmployee(guid, authEmployee);
        return ResponseEntity.ok("Сотрудник успешно удалён");
    }

    /**
     * Смена пароля по GUID (только Администратор или Руководитель отдела).
     */
    @PutMapping("/{guid}/password")
    public ResponseEntity<String> changePassword(
            @PathVariable String guid,
            @RequestParam("newPassword") String newPassword,
            @RequestHeader("Authorization") String authHeader) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        employeeService.changePassword(guid, newPassword, authEmployee);
        return ResponseEntity.ok("Пароль успешно изменён");
    }

    /**
     * Аутентифицирует сотрудника и проверяет права.
     * requireEditPermission=true — только Администратор или Руководитель отдела.
     */
    private Employee getAuthenticatedEmployee(String authHeader, boolean requireEditPermission) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.error("Отсутствует или недействителен заголовок авторизации");
            throw new IllegalArgumentException("Требуется токен в заголовке Authorization с префиксом Bearer");
        }
        String token = authHeader.substring(7);
        Employee employee = authService.getEmployeeFromToken(token);
        String permission = employee.getEmployeePost() != null && employee.getEmployeePost().getEmployeePermission() != null
                ? employee.getEmployeePost().getEmployeePermission().getPermission()
                : null;
        if (requireEditPermission && !("Администратор".equals(permission) || "Руководитель отдела".equals(permission))) {
            logger.error("Доступ запрещён: пользователь не имеет роль администратора или руководителя отдела, guid_employee={}", employee.getGuidEmployee());
            throw new IllegalArgumentException("Доступ разрешён только администраторам или руководителям отдела");
        }
        return employee;
    }
}