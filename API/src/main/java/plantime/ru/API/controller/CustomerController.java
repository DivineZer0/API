package plantime.ru.API.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import plantime.ru.API.dto.CustomerDTO;
import plantime.ru.API.dto.ErrorResponse;
import plantime.ru.API.entity.Customer;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.service.AuthService;
import plantime.ru.API.service.CustomerService;
import plantime.ru.API.service.OrganizationService;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final AuthService authService;
    private final OrganizationService organizationService;
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    public CustomerController(CustomerService customerService, AuthService authService, OrganizationService organizationService) {
        this.customerService = customerService;
        this.authService = authService;
        this.organizationService = organizationService;
    }

    @GetMapping("/organization/{orgId}/customers")
    public ResponseEntity<?> getCustomersByOrganization(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer orgId
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, false);
        List<Customer> customers = organizationService.getCustomersByOrganization(orgId);
        // или преобразовать в DTO при необходимости
        return ResponseEntity.ok(customers);
    }

    /**
     * Получает список клиентов по организации и с фильтрацией/сортировкой.
     * ?view=full|short
     * ?sortBy={fio|phoneNumber}
     * ?order=asc|desc
     * ?organizationId=...
     * ?search=...
     */
    @GetMapping
    public ResponseEntity<?> getCustomers(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "organizationId", required = false) Integer organizationId,
            @RequestParam(value = "view", defaultValue = "full") String view,
            @RequestParam(value = "sortBy", defaultValue = "surname") String sortBy,
            @RequestParam(value = "order", defaultValue = "asc") String order,
            @RequestParam(value = "search", required = false) String search
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, false);

        List<CustomerDTO> customers = customerService.getCustomers(organizationId, view, sortBy, order, search);

        if (customers.isEmpty()) {
            return ResponseEntity.ok(new ErrorResponse("Клиенты отсутствуют", "Список пуст", 200));
        }
        return ResponseEntity.ok(customers);
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(
            @Valid @RequestBody CustomerDTO customerDTO,
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        CustomerDTO saved = customerService.createCustomer(customerDTO, authEmployee);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(
            @PathVariable Integer id,
            @Valid @RequestBody CustomerDTO customerDTO,
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        CustomerDTO updated = customerService.updateCustomer(id, customerDTO, authEmployee);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCustomer(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        customerService.deleteCustomer(id, authEmployee);
        return ResponseEntity.ok("Клиент успешно удалён");
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