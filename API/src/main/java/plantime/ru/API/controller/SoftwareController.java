package plantime.ru.API.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import plantime.ru.API.dto.SoftwareDTO;
import plantime.ru.API.dto.ErrorResponse;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.service.AuthService;
import plantime.ru.API.service.SoftwareService;

import java.util.List;

/**
 * Контроллер для управления ПО в API PlanTime.
 */
@RestController
@RequestMapping("/api/software")
public class SoftwareController {

    private final SoftwareService softwareService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(SoftwareController.class);

    public SoftwareController(SoftwareService softwareService, AuthService authService) {
        this.softwareService = softwareService;
        this.authService = authService;
    }

    /**
     * Получает список всего ПО, отсортированных по названию.
     * Доступно только администраторам.
     */
    @GetMapping
    public ResponseEntity<?> getAllSoftware(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "sortBy", defaultValue = "software") String sortBy,
            @RequestParam(value = "order", defaultValue = "asc") String order
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        List<SoftwareDTO> list = softwareService.getAllSoftware(authEmployee, sortBy, order);
        if (list.isEmpty()) {
            return ResponseEntity.ok(new ErrorResponse("ПО отсутствует", "Список пуст", 200));
        }
        return ResponseEntity.ok(list);
    }

    /**
     * Создаёт новый софт.
     * Доступно только администраторам.
     */
    @PostMapping
    public ResponseEntity<SoftwareDTO> createSoftware(
            @Valid @RequestBody SoftwareDTO softwareDTO,
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        SoftwareDTO saved = softwareService.createSoftware(softwareDTO, authEmployee);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Обновляет существующий софт.
     * Доступно только администраторам.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SoftwareDTO> updateSoftware(
            @PathVariable Integer id,
            @Valid @RequestBody SoftwareDTO softwareDTO,
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        SoftwareDTO updated = softwareService.updateSoftware(id, softwareDTO, authEmployee);
        return ResponseEntity.ok(updated);
    }

    /**
     * Удаляет софт по идентификатору.
     * Доступно только администраторам.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSoftware(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        softwareService.deleteSoftware(id, authEmployee);
        return ResponseEntity.ok("ПО успешно удалено");
    }

    private Employee getAuthenticatedEmployee(String authHeader, boolean requireAdmin) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.error("Отсутствует или недействителен заголовок авторизации");
            throw new IllegalArgumentException("Требуется токен в заголовке Authorization с префиксом Bearer");
        }
        String token = authHeader.substring(7);
        Employee employee = authService.getEmployeeFromToken(token);
        if (requireAdmin) {
            String permission = employee.getEmployeePost() != null && employee.getEmployeePost().getEmployeePermission() != null
                    ? employee.getEmployeePost().getEmployeePermission().getPermission()
                    : null;
            if (!"Администратор".equals(permission)) {
                logger.error("Доступ запрещён: пользователь не имеет роль администратора, guid_employee={}", employee.getGuidEmployee());
                throw new IllegalArgumentException("Доступ разрешён только администраторам");
            }
        }
        return employee;
    }
}