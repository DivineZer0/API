package plantime.ru.API.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import plantime.ru.API.dto.EmployeeGenderDTO;
import plantime.ru.API.dto.ErrorResponse;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.service.AuthService;
import plantime.ru.API.service.EmployeeGenderService;

import java.util.List;

/**
 * Контроллер для управления гендерами сотрудников в API PlanTime.
 * Обеспечивает REST-интерфейс для операций создания, получения, обновления и удаления гендеров.
 */
@RestController
@RequestMapping("/api/employee/genders")
public class EmployeeGenderController {

    private final EmployeeGenderService genderService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeGenderController.class);

    /**
     * Конструктор контроллера.
     *
     * @param genderService Сервис для работы с гендерами.
     * @param authService   Сервис для аутентификации.
     */
    public EmployeeGenderController(EmployeeGenderService genderService, AuthService authService) {
        this.genderService = genderService;
        this.authService = authService;
    }

    /**
     * Получает список всех гендеров с поддержкой сортировки.
     * Доступно всем аутентифицированным пользователям.
     *
     * @param authHeader Заголовок авторизации с JWT-токеном.
     * @param sortBy     Поле для сортировки.
     * @param order      Порядок сортировки: "asc" или "desc".
     * @return Список гендеров или информативное сообщение, если гендеров нет.
     */
    @GetMapping
    public ResponseEntity<?> getAllGenders(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "sortBy", defaultValue = "gender") String sortBy,
            @RequestParam(value = "order", defaultValue = "asc") String order) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, false);
        List<EmployeeGenderDTO> genders = genderService.getAllGenders(authEmployee, sortBy, order);
        if (genders.isEmpty()) {
            return ResponseEntity.ok(new ErrorResponse(
                    "В системе ещё не создано ни одного гендера. Добавьте первый гендер для начала работы.",
                    "Нет доступных гендеров",
                    200
            ));
        }
        return ResponseEntity.ok(genders);
    }

    /**
     * Создаёт новый гендер.
     * Доступно только администраторам.
     * Поддерживает параметр forceCreate для подтверждения создания при схожести названия.
     *
     * @param genderDTO   DTO с данными гендера.
     * @param authHeader  Заголовок авторизации с JWT-токеном.
     * @param forceCreate Флаг подтверждения создания при схожем названии.
     * @return DTO созданного гендера или корректное информативное сообщение об ошибке.
     */
    @PostMapping
    public ResponseEntity<?> createGender(
            @Valid @RequestBody EmployeeGenderDTO genderDTO,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "forceCreate", required = false) Boolean forceCreate
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            EmployeeGenderDTO savedGender = genderService.createGender(genderDTO, authEmployee, forceCreate);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedGender);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка создания гендера",
                    400
            ));
        }
    }

    /**
     * Обновляет существующий гендер.
     * Доступно только администраторам.
     * Поддерживает параметр forceUpdate для подтверждения обновления при схожем названии.
     *
     * @param id          Идентификатор гендера.
     * @param genderDTO   DTO с обновлёнными данными гендера.
     * @param authHeader  Заголовок авторизации с JWT-токеном.
     * @param forceUpdate Флаг подтверждения обновления при схожем названии.
     * @return DTO обновлённого гендера или информативное сообщение об ошибке.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGender(
            @PathVariable Integer id,
            @Valid @RequestBody EmployeeGenderDTO genderDTO,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            EmployeeGenderDTO updatedGender = genderService.updateGender(id, genderDTO, authEmployee, forceUpdate);
            return ResponseEntity.ok(updatedGender);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка обновления гендера",
                    400
            ));
        }
    }

    /**
     * Удаляет гендер по идентификатору.
     * Доступно только администраторам.
     *
     * @param id         Идентификатор гендера.
     * @param authHeader Заголовок авторизации с JWT-токеном.
     * @return Информативное сообщение об успешном удалении.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGender(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            genderService.deleteGender(id, authEmployee);
            return ResponseEntity.ok("Гендер успешно удалён.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка удаления гендера",
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