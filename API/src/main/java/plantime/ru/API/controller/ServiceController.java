package plantime.ru.API.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import plantime.ru.API.dto.ServiceDTO;
import plantime.ru.API.dto.ErrorResponse;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.service.AuthService;
import plantime.ru.API.service.ServiceService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Контроллер для управления услугами (service) в системе PlanTime.
 * Обеспечивает REST-интерфейс для операций создания, получения, обновления и удаления услуг,
 * а также выборку по диапазону цены и проверку схожести названия.
 */
@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceService serviceService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    public ServiceController(ServiceService serviceService, AuthService authService) {
        this.serviceService = serviceService;
        this.authService = authService;
    }

    /**
     * Получает список всех услуг с возможностью фильтрации по диапазону цены и сортировкой.
     * Доступно всем аутентифицированным пользователям.
     *
     * @param authHeader Заголовок авторизации с JWT-токеном.
     * @param minPrice   Минимальная цена.
     * @param maxPrice   Максимальная цена.
     * @param sortBy     Поле сортировки ("service", "price" и т.д.).
     * @param order      Направление сортировки ("asc" или "desc").
     * @return Список услуг или сообщение, если услуг нет.
     */
    @GetMapping
    public ResponseEntity<?> getAllServices(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "sortBy", required = false, defaultValue = "service") String sortBy,
            @RequestParam(value = "order", required = false, defaultValue = "asc") String order
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, false);
        List<ServiceDTO> services = serviceService.getAllServices(minPrice, maxPrice, sortBy, order);
        if (services.isEmpty()) {
            return ResponseEntity.ok(new ErrorResponse(
                    "В системе ещё не создано ни одной услуги. Добавьте первую услугу для начала работы.",
                    "Нет доступных услуг",
                    200
            ));
        }
        return ResponseEntity.ok(services);
    }

    /**
     * Создаёт новую услугу.
     * Доступно только администраторам.
     * Проверяет схожесть названия услуги с существующими.
     * Если найдено похожее название, требует подтверждения через forceCreate.
     */
    @PostMapping
    public ResponseEntity<?> createService(
            @Valid @RequestBody ServiceDTO serviceDTO,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "forceCreate", required = false) Boolean forceCreate
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            ServiceDTO savedService = serviceService.createService(serviceDTO, authEmployee, forceCreate);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedService);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка создания услуги",
                    400
            ));
        }
    }

    /**
     * Обновляет существующую услугу.
     * Доступно только администраторам.
     * Проверяет схожесть нового названия с другими услугами.
     * Если найдено похожее название, требует подтверждения через forceUpdate.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateService(
            @PathVariable Integer id,
            @Valid @RequestBody ServiceDTO serviceDTO,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            ServiceDTO updatedService = serviceService.updateService(id, serviceDTO, authEmployee, forceUpdate);
            return ResponseEntity.ok(updatedService);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка обновления услуги",
                    400
            ));
        }
    }

    /**
     * Удаляет услугу по идентификатору.
     * Доступно только администраторам.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        try {
            serviceService.deleteService(id, authEmployee);
            return ResponseEntity.ok("Услуга успешно удалена.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    e.getMessage(),
                    "Ошибка удаления услуги",
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