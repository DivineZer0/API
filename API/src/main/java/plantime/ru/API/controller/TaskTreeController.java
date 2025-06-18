package plantime.ru.API.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import plantime.ru.API.dto.TaskTreeDTO;
import plantime.ru.API.service.TaskTreeService;

import java.util.List;

@RestController
@RequestMapping("/api/tasktree")
public class TaskTreeController {
    @Autowired
    private TaskTreeService service;

    @GetMapping("/by-project/{idProject}")
    public List<TaskTreeDTO> getByProject(@PathVariable Integer idProject) {
        return service.getByProjectId(idProject);
    }
}