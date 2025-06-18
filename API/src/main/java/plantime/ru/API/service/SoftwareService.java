package plantime.ru.API.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plantime.ru.API.dto.SoftwareDTO;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.entity.Software;
import plantime.ru.API.entity.Log;
import plantime.ru.API.repository.SoftwareRepository;
import plantime.ru.API.repository.LogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления ПО.
 */
@Service
public class SoftwareService {

    private final SoftwareRepository softwareRepository;
    private final LogRepository logRepository;
    private static final Logger logger = LoggerFactory.getLogger(SoftwareService.class);

    public SoftwareService(
            SoftwareRepository softwareRepository,
            LogRepository logRepository) {
        this.softwareRepository = softwareRepository;
        this.logRepository = logRepository;
    }

    public List<SoftwareDTO> getAllSoftware(Employee authEmployee, String sortBy, String order) {
        try {
            if (!sortBy.equals("software")) {
                logRepository.save(new Log(authEmployee, "Недопустимое поле сортировки: " + sortBy, LocalDateTime.now()));
                logger.error("Недопустимое поле сортировки: {}, guid_employee={}", sortBy, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Сортировка возможна только по полю 'software'");
            }
            if (!order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc")) {
                logRepository.save(new Log(authEmployee, "Недопустимый порядок сортировки: " + order, LocalDateTime.now()));
                logger.error("Недопустимый порядок сортировки: {}, guid_employee={}", order, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Порядок сортировки должен быть 'asc' или 'desc'");
            }
            Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(Sort.Direction.ASC, "software") : Sort.by(Sort.Direction.DESC, "software");
            List<Software> list = softwareRepository.findAll(sort);
            if (list.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Список ПО пуст", LocalDateTime.now()));
                logger.info("Список ПО пуст, guid_employee={}", authEmployee.getGuidEmployee());
                return List.of();
            }
            List<SoftwareDTO> dtos = list.stream()
                    .map(s -> new SoftwareDTO(s.getIdSoftware(), s.getSoftware(), s.getDescription(), s.getLogo()))
                    .collect(Collectors.toList());
            logRepository.save(new Log(authEmployee, "Получен список ПО, количество: " + list.size() + ", сортировка: " + sortBy + ", порядок: " + order, LocalDateTime.now()));
            logger.info("Успешно получен список ПО, количество: {}, sortBy={}, order={}, guid_employee={}",
                    list.size(), sortBy, order, authEmployee.getGuidEmployee());
            return dtos;
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при получении списка ПО: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при получении списка ПО: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось получить список ПО", e);
        }
    }

    @Transactional
    public SoftwareDTO createSoftware(SoftwareDTO dto, Employee authEmployee) {
        try {
            String name = dto.getSoftware().trim();
            String description = dto.getDescription() == null ? "" : dto.getDescription().trim();
            String logo = dto.getLogo() == null ? "" : dto.getLogo().trim();

            if (name.length() < 2 || name.length() > 60)
                throw new IllegalArgumentException("Название ПО должно быть от 2 до 60 символов");
            if (softwareRepository.existsBySoftwareIgnoreCase(name))
                throw new IllegalArgumentException("ПО с таким названием уже существует");

            Software sw = new Software(null, name, description, logo);
            Software saved = softwareRepository.save(sw);
            logRepository.save(new Log(authEmployee, "Создано ПО: " + name, LocalDateTime.now()));
            logger.info("Успешно создано ПО: {}, guid_employee={}", name, authEmployee.getGuidEmployee());
            return new SoftwareDTO(saved.getIdSoftware(), saved.getSoftware(), saved.getDescription(), saved.getLogo());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при создании ПО: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при создании ПО: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось создать ПО", e);
        }
    }

    @Transactional
    public SoftwareDTO updateSoftware(Integer id, SoftwareDTO dto, Employee authEmployee) {
        try {
            Optional<Software> existing = softwareRepository.findById(id);
            if (existing.isEmpty())
                throw new IllegalArgumentException("ПО с id " + id + " не найдено");
            String name = dto.getSoftware().trim();
            String description = dto.getDescription() == null ? "" : dto.getDescription().trim();
            String logo = dto.getLogo() == null ? "" : dto.getLogo().trim();

            if (!existing.get().getSoftware().equalsIgnoreCase(name) && softwareRepository.existsBySoftwareIgnoreCase(name))
                throw new IllegalArgumentException("ПО с таким названием уже существует");
            Software sw = existing.get();
            sw.setSoftware(name);
            sw.setDescription(description);
            sw.setLogo(logo);
            Software updated = softwareRepository.save(sw);
            logRepository.save(new Log(authEmployee, "Обновлено ПО: " + name, LocalDateTime.now()));
            logger.info("Успешно обновлено ПО: {}, guid_employee={}", name, authEmployee.getGuidEmployee());
            return new SoftwareDTO(updated.getIdSoftware(), updated.getSoftware(), updated.getDescription(), updated.getLogo());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при обновлении ПО: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при обновлении ПО: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось обновить ПО", e);
        }
    }

    @Transactional
    public void deleteSoftware(Integer id, Employee authEmployee) {
        try {
            Optional<Software> existing = softwareRepository.findById(id);
            if (existing.isEmpty())
                throw new IllegalArgumentException("ПО с id " + id + " не найдено");
            softwareRepository.deleteById(id);
            softwareRepository.flush();
            logRepository.save(new Log(authEmployee, "Удалено ПО с id: " + id, LocalDateTime.now()));
            logger.info("Успешно удалено ПО с id: {}, guid_employee={}", id, authEmployee.getGuidEmployee());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Нарушение целостности данных при удалении ПО с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось удалить ПО с id " + id + " из-за зависимостей в базе данных", e);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при удалении ПО: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при удалении ПО с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось удалить ПО", e);
        }
    }
}