package plantime.ru.API.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import plantime.ru.API.dto.EmployeeStatusDTO;
import plantime.ru.API.dto.ErrorResponse;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.service.AuthService;
import plantime.ru.API.service.EmployeeStatusService;

import java.util.List;

/**
 * Контроллер для управления статусами сотрудников в API PlanTime.
 * Обеспечивает REST-интерфейс для операций создания, получения, обновления и удаления статусов.
 */
@RestController
@RequestMapping("/api/employee/statuses")
public class EmployeeStatusController {

    private final EmployeeStatusService statusService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeStatusController.class);

    /**
     * Конструктор контроллера.
     *
     * @param statusService Сервис для работы со статусами.
     * @param authService   Сервис для аутентификации.
     */
    public EmployeeStatusController(EmployeeStatusService statusService, AuthService authService) {
        this.statusService = statusService;
        this.authService = authService;
    }

    /**
     * Получает список всех статусов с поддержкой сортировки.
     * Доступно всем аутентифицированным пользователям.
     *
     * @param authHeader Заголовок авторизации с JWT-токеном.
     * @param sortBy     Поле для сортировки.
     * @param order      Порядок сортировки: "asc" или "desc".
     * @return Список статусов или информативное сообщение, если статусов нет.
     */
    @GetMapping
    public ResponseEntity<?> getAllStatuses(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "sortBy", defaultValue = "status") String sortBy,
            @RequestParam(value = "order", defaultValue = "asc") String order) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, false);
        List<EmployeeStatusDTO> statuses = statusService.getAllStatuses(authEmployee, sortBy, order);
        if (statuses.isEmpty()) {
            return ResponseEntity.ok(new ErrorResponse(
                    "В системе ещё не создано ни одного статуса. Добавьте первый статус для начала работы.",
                    "Нет доступных статусов",
                    200
            ));
        }
        return ResponseEntity.ok(statuses);
    }

    /**
     * Создаёт новый статус.
     * Доступно только администраторам.
     * Поддерживает параметр forceCreate для подтверждения создания при схожести названия.
     *
     * @param statusDTO   DTO с данными статуса.
     * @param authHeader  Заголовок авторизации с JWT-токеном.
     * @param forceCreate Флаг подтверждения создания при схожем названии.
     * @return DTO созданного статуса или корректное информативное сообщение об ошибке.
     */
    @PostMapping
    public ResponseEntity<?> createStatus(
            @Valid @RequestBody EmployeeStatusDTO statusDTO,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "forceCreate", required = false) Boolean forceCreate
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            EmployeeStatusDTO savedStatus = statusService.createStatus(statusDTO, authEmployee, forceCreate);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedStatus);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка создания статуса",
                    400
            ));
        }
    }

    /**
     * Обновляет существующий статус.
     * Доступно только администраторам.
     * Поддерживает параметр forceUpdate для подтверждения обновления при схожем названии.
     *
     * @param id          Идентификатор статуса.
     * @param statusDTO   DTO с обновлёнными данными статуса.
     * @param authHeader  Заголовок авторизации с JWT-токеном.
     * @param forceUpdate Флаг подтверждения обновления при схожем названии.
     * @return DTO обновлённого статуса или информативное сообщение об ошибке.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer id,
            @Valid @RequestBody EmployeeStatusDTO statusDTO,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            EmployeeStatusDTO updatedStatus = statusService.updateStatus(id, statusDTO, authEmployee, forceUpdate);
            return ResponseEntity.ok(updatedStatus);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка обновления статуса",
                    400
            ));
        }
    }

    /**
     * Удаляет статус по идентификатору.
     * Доступно только администраторам.
     *
     * @param id         Идентификатор статуса.
     * @param authHeader Заголовок авторизации с JWT-токеном.
     * @return Информативное сообщение об успешном удалении.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStatus(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            statusService.deleteStatus(id, authEmployee);
            return ResponseEntity.ok("Статус успешно удалён.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка удаления статуса",
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