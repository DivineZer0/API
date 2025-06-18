package plantime.ru.API.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import plantime.ru.API.dto.EmployeeDepartmentDTO;
import plantime.ru.API.dto.ErrorResponse;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.service.AuthService;
import plantime.ru.API.service.EmployeeDepartmentService;

import java.util.List;

/**
 * Контроллер для управления отделами сотрудников в API PlanTime.
 * Обеспечивает REST-интерфейс для операций создания, получения, обновления и удаления отделов.
 */
@RestController
@RequestMapping("/api/employee/departments")
public class EmployeeDepartmentController {

    private final EmployeeDepartmentService departmentService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeDepartmentController.class);

    /**
     * Конструктор контроллера.
     *
     * @param departmentService Сервис для работы с отделами.
     * @param authService       Сервис для аутентификации.
     */
    public EmployeeDepartmentController(EmployeeDepartmentService departmentService, AuthService authService) {
        this.departmentService = departmentService;
        this.authService = authService;
    }

    /**
     * Получает список всех отделов с поддержкой сортировки.
     * Доступно всем аутентифицированным пользователям.
     *
     * @param authHeader Заголовок авторизации с JWT-токеном.
     * @param sortBy     Поле для сортировки.
     * @param order      Порядок сортировки: "asc" или "desc".
     * @return Список отделов или информативное сообщение, если отделов нет.
     */
    @GetMapping
    public ResponseEntity<?> getAllDepartments(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "sortBy", defaultValue = "department") String sortBy,
            @RequestParam(value = "order", defaultValue = "asc") String order) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, false);
        List<EmployeeDepartmentDTO> departments = departmentService.getAllDepartments(authEmployee, sortBy, order);
        if (departments.isEmpty()) {
            return ResponseEntity.ok(new ErrorResponse(
                    "В системе ещё не создано ни одного отдела. Добавьте первый отдел для начала работы.",
                    "Нет доступных отделов",
                    200
            ));
        }
        return ResponseEntity.ok(departments);
    }

    /**
     * Создаёт новый отдел.
     * Доступно только администраторам.
     * Поддерживает параметр forceCreate для подтверждения создания при схожести названия.
     *
     * @param departmentDTO DTO с данными отдела.
     * @param authHeader    Заголовок авторизации с JWT-токеном.
     * @param forceCreate   Флаг подтверждения создания при схожем названии.
     * @return DTO созданного отдела или корректное информативное сообщение об ошибке.
     */
    @PostMapping
    public ResponseEntity<?> createDepartment(
            @Valid @RequestBody EmployeeDepartmentDTO departmentDTO,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "forceCreate", required = false) Boolean forceCreate
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            EmployeeDepartmentDTO savedDepartment = departmentService.createDepartment(departmentDTO, authEmployee, forceCreate);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDepartment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка создания отдела",
                    400
            ));
        }
    }

    /**
     * Обновляет существующий отдел.
     * Доступно только администраторам.
     * Поддерживает параметр forceUpdate для подтверждения обновления при схожем названии.
     *
     * @param id            Идентификатор отдела.
     * @param departmentDTO DTO с обновлёнными данными отдела.
     * @param authHeader    Заголовок авторизации с JWT-токеном.
     * @param forceUpdate   Флаг подтверждения обновления при схожем названии.
     * @return DTO обновлённого отдела или информативное сообщение об ошибке.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(
            @PathVariable Integer id,
            @Valid @RequestBody EmployeeDepartmentDTO departmentDTO,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            EmployeeDepartmentDTO updatedDepartment = departmentService.updateDepartment(id, departmentDTO, authEmployee, forceUpdate);
            return ResponseEntity.ok(updatedDepartment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка обновления отдела",
                    400
            ));
        }
    }

    /**
     * Удаляет отдел по идентификатору.
     * Доступно только администраторам.
     *
     * @param id         Идентификатор отдела.
     * @param authHeader Заголовок авторизации с JWT-токеном.
     * @return Информативное сообщение об успешном удалении.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            departmentService.deleteDepartment(id, authEmployee);
            return ResponseEntity.ok("Отдел успешно удалён.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка удаления отдела",
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