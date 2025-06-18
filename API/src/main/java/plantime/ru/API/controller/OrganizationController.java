package plantime.ru.API.controller;

import org.springframework.web.bind.annotation.*;
import plantime.ru.API.dto.OrganizationDTO;
import plantime.ru.API.dto.OrganizationDetailsDTO;
import plantime.ru.API.service.OrganizationService;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {
    private final OrganizationService service;

    public OrganizationController(OrganizationService service) {
        this.service = service;
    }

    // Получение списка организаций с фильтрацией по всем полям (Краткое и полное имя, ИНН, почта, телефон)
    @GetMapping
    public List<OrganizationDTO> getAllWithFilter(
            @RequestParam(required = false) String short_name,
            @RequestParam(required = false) String long_name,
            @RequestParam(required = false) String inn,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone_number
    ) {
        OrganizationDTO filter = new OrganizationDTO();
        filter.setShort_name(short_name);
        filter.setLong_name(long_name);
        filter.setInn(inn);
        filter.setEmail(email);
        filter.setPhone_number(phone_number);
        return service.findAllWithFilter(filter);
    }

    // Получение полной информации по организации
    @GetMapping("/{id}")
    public OrganizationDetailsDTO getById(@PathVariable Integer id) {
        return service.findById(id);
    }

    // Добавление новой организации
    @PostMapping
    public OrganizationDetailsDTO create(@RequestBody OrganizationDetailsDTO dto) {
        return service.createOrUpdate(null, dto);
    }

    // Изменение существующей организации
    @PutMapping("/{id}")
    public OrganizationDetailsDTO update(@PathVariable Integer id, @RequestBody OrganizationDetailsDTO dto) {
        return service.createOrUpdate(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}