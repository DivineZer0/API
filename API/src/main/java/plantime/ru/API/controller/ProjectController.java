package plantime.ru.API.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import plantime.ru.API.dto.ProjectDTO;
import plantime.ru.API.service.ProjectService;

import java.util.List;

@RestController
@RequestMapping("/api/project")
public class ProjectController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService service;

    @GetMapping
    public List<ProjectDTO> getAll() {
        logger.info("GET /api/project - Получение списка всех проектов");
        List<ProjectDTO> result = service.findAll();
        logger.info("Найдено проектов: {}", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("GET /api/project/{} - Получение проекта по id", id);
        try {
            ProjectDTO dto = service.findById(id);
            if (dto == null) {
                logger.warn("Проект с id {} не найден", id);
                return ResponseEntity.notFound().build();
            }
            logger.info("Проект с id {} найден", id);
            return ResponseEntity.ok(dto);
        } catch (Exception ex) {
            logger.error("Ошибка при получении проекта по id {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProjectDTO dto) {
        logger.info("POST /api/project - Создание проекта: {}", dto);
        try {
            ProjectDTO created = service.save(dto, true);
            logger.info("Проект успешно создан с id {}", created.getIdProject());
            return ResponseEntity.ok(created);
        } catch (Exception ex) {
            logger.error("Ошибка при создании проекта: {}", ex.getMessage(), ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody ProjectDTO dto) {
        logger.info("PUT /api/project/{} - Обновление проекта: {}", id, dto);
        try {
            dto.setIdProject(id);
            ProjectDTO updated = service.save(dto, false);
            logger.info("Проект с id {} успешно обновлен", id);
            return ResponseEntity.ok(updated);
        } catch (Exception ex) {
            logger.error("Ошибка при обновлении проекта с id {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("DELETE /api/project/{} - Удаление проекта", id);
        try {
            service.delete(id);
            logger.info("Проект с id {} успешно удален", id);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            logger.error("Ошибка при удалении проекта с id {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}