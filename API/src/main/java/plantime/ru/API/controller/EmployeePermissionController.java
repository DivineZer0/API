package plantime.ru.API.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import plantime.ru.API.dto.EmployeePermissionDTO;
import plantime.ru.API.dto.ErrorResponse;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.service.AuthService;
import plantime.ru.API.service.EmployeePermissionService;

import java.util.List;

/**
 * Контроллер для управления уровнями прав доступа сотрудников в API PlanTime.
 * Обеспечивает REST-интерфейс для операций создания, получения, обновления и удаления уровней прав.
 */
@RestController
@RequestMapping("/api/employee/permissions")
public class EmployeePermissionController {

    private final EmployeePermissionService permissionService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(EmployeePermissionController.class);

    /**
     * Конструктор контроллера.
     *
     * @param permissionService Сервис для работы с уровнями прав.
     * @param authService       Сервис для аутентификации.
     */
    public EmployeePermissionController(EmployeePermissionService permissionService, AuthService authService) {
        this.permissionService = permissionService;
        this.authService = authService;
    }

    /**
     * Получает список всех уровней прав доступа с поддержкой сортировки.
     * Доступно всем аутентифицированным пользователям.
     *
     * @param authHeader Заголовок авторизации с JWT-токеном.
     * @param sortBy     Поле для сортировки.
     * @param order      Порядок сортировки: "asc" или "desc".
     * @return Список уровней прав или информативное сообщение, если уровней нет.
     */
    @GetMapping
    public ResponseEntity<?> getAllPermissions(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "sortBy", defaultValue = "permission") String sortBy,
            @RequestParam(value = "order", defaultValue = "asc") String order) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, false);
        List<EmployeePermissionDTO> permissions = permissionService.getAllPermissions(authEmployee, sortBy, order);
        if (permissions.isEmpty()) {
            return ResponseEntity.ok(new ErrorResponse(
                    "В системе ещё не создано ни одного уровня прав доступа. Добавьте первый уровень для начала работы.",
                    "Нет доступных уровней прав",
                    200
            ));
        }
        return ResponseEntity.ok(permissions);
    }

    /**
     * Создаёт новый уровень прав доступа.
     * Доступно только администраторам.
     * Поддерживает параметр forceCreate для подтверждения создания при схожести названия.
     *
     * @param permissionDTO DTO с данными уровня прав.
     * @param authHeader    Заголовок авторизации с JWT-токеном.
     * @param forceCreate   Флаг подтверждения создания при схожем названии.
     * @return DTO созданного уровня прав или корректное информативное сообщение об ошибке.
     */
    @PostMapping
    public ResponseEntity<?> createPermission(
            @Valid @RequestBody EmployeePermissionDTO permissionDTO,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "forceCreate", required = false) Boolean forceCreate
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            EmployeePermissionDTO savedPermission = permissionService.createPermission(permissionDTO, authEmployee, forceCreate);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPermission);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка создания уровня прав доступа",
                    400
            ));
        }
    }

    /**
     * Обновляет существующий уровень прав доступа.
     * Доступно только администраторам.
     * Поддерживает параметр forceUpdate для подтверждения обновления при схожем названии.
     *
     * @param id            Идентификатор уровня прав.
     * @param permissionDTO DTO с обновлёнными данными уровня прав.
     * @param authHeader    Заголовок авторизации с JWT-токеном.
     * @param forceUpdate   Флаг подтверждения обновления при схожем названии.
     * @return DTO обновлённого уровня прав или информативное сообщение об ошибке.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePermission(
            @PathVariable Integer id,
            @Valid @RequestBody EmployeePermissionDTO permissionDTO,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            EmployeePermissionDTO updatedPermission = permissionService.updatePermission(id, permissionDTO, authEmployee, forceUpdate);
            return ResponseEntity.ok(updatedPermission);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка обновления уровня прав доступа",
                    400
            ));
        }
    }

    /**
     * Удаляет уровень прав доступа по идентификатору.
     * Доступно только администраторам.
     *
     * @param id         Идентификатор уровня прав.
     * @param authHeader Заголовок авторизации с JWT-токеном.
     * @return Информативное сообщение об успешном удалении.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePermission(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            permissionService.deletePermission(id, authEmployee);
            return ResponseEntity.ok("Уровень прав доступа успешно удалён.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка удаления уровня прав доступа",
                    400
            ));
        }
    }

    /**
     * Аутентифицирует сотрудника и проверяет роль администратора, если требуется.
     *
     * @param authHeader   Заголовок авторизации с JWT-токеном.
     * @param requireAdmin Проверять ли наличие роли администратора.
     * @return Аутентифицированный сотрудник.
     * @throws IllegalArgumentException Если токен отсутствует, недействителен или сотрудник не администратор.
     */
    private Employee getAuthenticatedEmployee(String authHeader, boolean requireAdmin) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.error("Отсутствует или недействителен заголовок авторизации");
            throw new IllegalArgumentException("Для выполнения операции требуется действительный токен (заголовок Authorization с префиксом Bearer).");
        }
        String token = authHeader.substring(7);
        Employee employee = authService.getEmployeeFromToken(token);
        if (requireAdmin) {
            String permission = employee.getEmployeePost() != null && employee.getEmployeePost().getEmployeePermission() != null
                    ? employee.getEmployeePost().getEmployeePermission().getPermission()
                    : null;
            if (!"Администратор".equals(permission)) {
                logger.error("Доступ запрещён: пользователь не имеет роль администратора, guid_employee={}", employee.getGuidEmployee());
                throw new IllegalArgumentException("Доступ к данной операции разрешён только администраторам.");
            }
        }
        return employee;
    }
}