package plantime.ru.API.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import plantime.ru.API.dto.EmployeePostDTO;
import plantime.ru.API.dto.ErrorResponse;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.service.AuthService;
import plantime.ru.API.service.EmployeePostService;

import java.util.List;

/**
 * Контроллер для управления должностями сотрудников в API PlanTime.
 * Обеспечивает REST-интерфейс для операций создания, получения, обновления и удаления должностей.
 */
@RestController
@RequestMapping("/api/employee/posts")
public class EmployeePostController {

    private final EmployeePostService postService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(EmployeePostController.class);

    public EmployeePostController(EmployeePostService postService, AuthService authService) {
        this.postService = postService;
        this.authService = authService;
    }

    /**
     * Получает список всех должностей с поддержкой сортировки и фильтрации по idEmployeePermission.
     * Доступно всем аутентифицированным пользователям.
     *
     * @param authHeader Заголовок авторизации с JWT-токеном.
     * @param idEmployeePermission Идентификатор уровня прав доступа для фильтрации.
     * @param sortBy Поле сортировки.
     * @param order Порядок сортировки: "asc" или "desc".
     * @return Список должностей или информативное сообщение, если должностей нет.
     */
    @GetMapping
    public ResponseEntity<?> getAllPosts(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "idEmployeePermission", required = false) Integer idEmployeePermission,
            @RequestParam(value = "sortBy", defaultValue = "post") String sortBy,
            @RequestParam(value = "order", defaultValue = "asc") String order) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, false);
        List<EmployeePostDTO> posts = postService.getAllPosts(authEmployee, idEmployeePermission, sortBy, order);
        if (posts.isEmpty()) {
            return ResponseEntity.ok(new ErrorResponse(
                    "В системе ещё не создано ни одной должности. Добавьте первую должность для начала работы.",
                    "Нет доступных должностей",
                    200
            ));
        }
        return ResponseEntity.ok(posts);
    }

    /**
     * Создаёт новую должность.
     * Доступно только администраторам.
     * Поддерживает параметр forceCreate для подтверждения создания при схожести названия.
     */
    @PostMapping
    public ResponseEntity<?> createPost(
            @Valid @RequestBody EmployeePostDTO postDTO,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "forceCreate", required = false) Boolean forceCreate
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            EmployeePostDTO savedPost = postService.createPost(postDTO, authEmployee, forceCreate);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка создания должности",
                    400
            ));
        }
    }

    /**
     * Обновляет существующую должность.
     * Доступно только администраторам.
     * Поддерживает параметр forceUpdate для подтверждения обновления при схожести названия.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable Integer id,
            @Valid @RequestBody EmployeePostDTO postDTO,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            EmployeePostDTO updatedPost = postService.updatePost(id, postDTO, authEmployee, forceUpdate);
            return ResponseEntity.ok(updatedPost);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка обновления должности",
                    400
            ));
        }
    }

    /**
     * Удаляет должность по идентификатору.
     * Доступно только администраторам.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            postService.deletePost(id, authEmployee);
            return ResponseEntity.ok("Должность успешно удалена.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка удаления должности",
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