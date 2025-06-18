package plantime.ru.API.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import plantime.ru.API.dto.TaskRecurrenceDTO;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.service.*;

@RestController
@RequestMapping("/api/task/recurrences")
@RequiredArgsConstructor
public class TaskRecurrenceController {

    private final TaskRecurrenceService recurrenceService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "recurrencePattern") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Employee employee = authService.getEmployeeFromToken(authHeader.substring(7));
        return ResponseEntity.ok(recurrenceService.getAllRecurrences(employee, sortBy, order));
    }

    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody TaskRecurrenceDTO dto,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Boolean forceCreate
    ) {
        Employee employee = authService.getEmployeeFromToken(authHeader.substring(7));
        return ResponseEntity.ok(recurrenceService.createRecurrence(dto, employee, forceCreate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody TaskRecurrenceDTO dto,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Boolean forceUpdate
    ) {
        Employee employee = authService.getEmployeeFromToken(authHeader.substring(7));
        return ResponseEntity.ok(recurrenceService.updateRecurrence(id, dto, employee, forceUpdate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader
    ) {
        Employee employee = authService.getEmployeeFromToken(authHeader.substring(7));
        recurrenceService.deleteRecurrence(id, employee);
        return ResponseEntity.ok("Шаблон периодичности удалён");
    }
}