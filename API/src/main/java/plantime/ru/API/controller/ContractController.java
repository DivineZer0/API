package plantime.ru.API.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.dto.ContractDTO;
import plantime.ru.API.dto.ErrorResponse;
import plantime.ru.API.service.AuthService;
import plantime.ru.API.service.ContractService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(ContractController.class);

    public ContractController(ContractService contractService, AuthService authService) {
        this.contractService = contractService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> getContracts(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "customerId", required = false) Integer customerId,
            @RequestParam(value = "paymentStatusId", required = false) Integer paymentStatusId,
            @RequestParam(value = "organizationId", required = false) Integer organizationId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "paymentDateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDateFrom,
            @RequestParam(value = "paymentDateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDateTo,
            @RequestParam(value = "minCost", required = false) BigDecimal minCost,
            @RequestParam(value = "maxCost", required = false) BigDecimal maxCost
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, false);
        List<ContractDTO> contracts = contractService.getContracts(
                customerId, paymentStatusId, organizationId,
                dateFrom, dateTo, paymentDateFrom, paymentDateTo, minCost, maxCost
        );
        if (contracts.isEmpty()) {
            return ResponseEntity.ok(new ErrorResponse("Контракты отсутствуют", "Список пуст", 200));
        }
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractDTO> getContract(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, false);
        ContractDTO contract = contractService.getContract(id);
        if (contract == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(contract);
    }

    @PostMapping
    public ResponseEntity<ContractDTO> createContract(
            @Valid @RequestBody ContractDTO contractDTO,
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        ContractDTO saved = contractService.createContract(contractDTO, authEmployee);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContractDTO> updateContract(
            @PathVariable Integer id,
            @Valid @RequestBody ContractDTO contractDTO,
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        ContractDTO updated = contractService.updateContract(id, contractDTO, authEmployee);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContract(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee authEmployee = getAuthenticatedEmployee(authHeader, true);
        contractService.deleteContract(id, authEmployee);
        return ResponseEntity.ok("Контракт успешно удалён");
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