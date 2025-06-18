package plantime.ru.API.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import plantime.ru.API.dto.ListOfSoftwareDTO;
import plantime.ru.API.service.ListOfSoftwareService;

import java.util.List;

@RestController
@RequestMapping("/api/list-of-software")
public class ListOfSoftwareController {
    @Autowired
    private ListOfSoftwareService service;

    @GetMapping
    public List<ListOfSoftwareDTO> getAll() { return service.findAll(); }

    @GetMapping("/project/{idProject}")
    public List<ListOfSoftwareDTO> getByProject(@PathVariable Integer idProject) {
        return service.findByProject(idProject);
    }

    @PostMapping
    public ListOfSoftwareDTO create(@RequestBody ListOfSoftwareDTO dto) {
        return service.save(dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) { service.delete(id); }
}