package plantime.ru.API.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import plantime.ru.API.dto.TaskTypeDTO;
import plantime.ru.API.dto.ErrorResponse;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.service.AuthService;
import plantime.ru.API.service.TaskTypeService;

import java.util.List;

/**
 * Контроллер для управления типами задач в API PlanTime.
 * Обеспечивает REST-интерфейс для операций создания, получения, обновления и удаления типов задач.
 */
@RestController
@RequestMapping("/api/task/types")
public class TaskTypeController {

    private final TaskTypeService taskTypeService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(TaskTypeController.class);

    public TaskTypeController(TaskTypeService taskTypeService, AuthService authService) {
        this.taskTypeService = taskTypeService;
        this.authService = authService;
    }

    /**
     * Получает список всех типов задач с сортировкой.
     * Доступно всем аутентифицированным пользователям.
     *
     * @param authHeader Заголовок авторизации с JWT-токеном.
     * @param sortBy     Поле для сортировки.
     * @param order      Порядок сортировки: "asc" или "desc".
     * @return Список типов задач или информативное сообщение, если типов нет.
     */
    @GetMapping
    public ResponseEntity<?> getAllTaskTypes(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "sortBy", defaultValue = "type") String sortBy,
            @RequestParam(value = "order", defaultValue = "asc") String order
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, false);
        List<TaskTypeDTO> types = taskTypeService.getAllTaskTypes(authEmployee, sortBy, order);
        if (types.isEmpty()) {
            return ResponseEntity.ok(new ErrorResponse(
                    "В системе ещё не создано ни одного типа задачи. Добавьте первый тип для начала работы.",
                    "Нет доступных типов задач",
                    200
            ));
        }
        return ResponseEntity.ok(types);
    }

    /**
     * Создаёт новый тип задачи.
     * Доступно только администраторам.
     * Поддерживает параметр forceCreate для подтверждения создания при схожести названия.
     *
     * @param taskTypeDTO DTO с данными типа задачи.
     * @param authHeader  Заголовок авторизации с JWT-токеном.
     * @param forceCreate Флаг подтверждения создания при схожем названии.
     * @return DTO созданного типа задачи или корректное информативное сообщение об ошибке.
     */
    @PostMapping
    public ResponseEntity<?> createTaskType(
            @Valid @RequestBody TaskTypeDTO taskTypeDTO,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "forceCreate", required = false) Boolean forceCreate
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            TaskTypeDTO savedType = taskTypeService.createTaskType(taskTypeDTO, authEmployee, forceCreate);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedType);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка создания типа задачи",
                    400
            ));
        }
    }

    /**
     * Обновляет существующий тип задачи.
     * Доступно только администраторам.
     * Поддерживает параметр forceUpdate для подтверждения обновления при схожем названии.
     *
     * @param id          Идентификатор типа задачи.
     * @param taskTypeDTO DTO с обновлёнными данными типа задачи.
     * @param authHeader  Заголовок авторизации с JWT-токеном.
     * @param forceUpdate Флаг подтверждения обновления при схожем названии.
     * @return DTO обновлённого типа задачи или информативное сообщение об ошибке.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTaskType(
            @PathVariable Integer id,
            @Valid @RequestBody TaskTypeDTO taskTypeDTO,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            TaskTypeDTO updatedType = taskTypeService.updateTaskType(id, taskTypeDTO, authEmployee, forceUpdate);
            return ResponseEntity.ok(updatedType);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка обновления типа задачи",
                    400
            ));
        }
    }

    /**
     * Удаляет тип задачи по идентификатору.
     * Доступно только администраторам.
     *
     * @param id         Идентификатор типа задачи.
     * @param authHeader Заголовок авторизации с JWT-токеном.
     * @return Информативное сообщение об успешном удалении.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTaskType(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            taskTypeService.deleteTaskType(id, authEmployee);
            return ResponseEntity.ok("Тип задачи успешно удалён.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка удаления типа задачи",
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