package plantime.ru.API.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plantime.ru.API.dto.TaskRecurrenceDTO;
import plantime.ru.API.entity.*;
import plantime.ru.API.repository.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskRecurrenceService {

    private final TaskRecurrenceRepository recurrenceRepository;
    private final TaskRepository taskRepository;
    private final LogRepository logRepository;
    private static final Logger logger = LoggerFactory.getLogger(TaskRecurrenceService.class);

    // Получение всех шаблонов с сортировкой
    public List<TaskRecurrenceDTO> getAllRecurrences(Employee authEmployee, String sortBy, String order) {
        try {
            if (!sortBy.equals("recurrencePattern")) {
                logRepository.save(new Log(authEmployee, "Недопустимое поле сортировки: " + sortBy, LocalDateTime.now()));
                logger.error("Недопустимое поле сортировки: {}, guid_employee={}", sortBy, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Сортировка возможна только по полю «Шаблон периодичности».");
            }

            if (!order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc")) {
                logRepository.save(new Log(authEmployee, "Недопустимый порядок сортировки: " + order, LocalDateTime.now()));
                logger.error("Недопустимый порядок сортировки: {}, guid_employee={}", order, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Порядок сортировки должен быть «asc» (по возрастанию) или «desc» (по убыванию).");
            }

            Sort sort = order.equalsIgnoreCase("asc")
                    ? Sort.by(Sort.Direction.ASC, "recurrencePattern")
                    : Sort.by(Sort.Direction.DESC, "recurrencePattern");

            List<TaskRecurrence> recurrences = recurrenceRepository.findAll(sort);
            if (recurrences.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Список шаблонов периодичности пуст", LocalDateTime.now()));
                logger.info("Список шаблонов периодичности пуст, guid_employee={}", authEmployee.getGuidEmployee());
                return List.of();
            }

            List<TaskRecurrenceDTO> dtos = recurrences.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            logRepository.save(new Log(authEmployee,
                    "Получен список шаблонов периодичности, количество: " + recurrences.size(),
                    LocalDateTime.now()));

            return dtos;
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при получении шаблонов: {}, guid_employee={}",
                    e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        }
    }

    // Создание нового шаблона
    @Transactional
    public TaskRecurrenceDTO createRecurrence(
            TaskRecurrenceDTO dto,
            Employee authEmployee,
            Boolean forceCreate
    ) {
        try {
            String pattern = dto.getRecurrencePattern().trim();

            // Валидация
            if (!pattern.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee,
                        "Попытка создать шаблон с недопустимыми символами: " + pattern,
                        LocalDateTime.now()));
                throw new IllegalArgumentException("Шаблон может содержать только буквы, пробелы и дефисы");
            }

            if (pattern.length() < 2 || pattern.length() > 40) {
                logRepository.save(new Log(authEmployee,
                        "Недопустимая длина шаблона: " + pattern.length(),
                        LocalDateTime.now()));
                throw new IllegalArgumentException("Длина шаблона должна быть от 2 до 40 символов");
            }

            // Проверка на дубликат
            if (recurrenceRepository.existsByRecurrencePatternIgnoreCase(pattern)) {
                logRepository.save(new Log(authEmployee,
                        "Попытка создать дубликат шаблона: " + pattern,
                        LocalDateTime.now()));
                throw new IllegalArgumentException("Шаблон с таким названием уже существует");
            }

            // Проверка на схожесть
            Optional<String> similarPattern = recurrenceRepository.findAll()
                    .stream()
                    .map(TaskRecurrence::getRecurrencePattern)
                    .filter(p -> stringSimilarity(p, pattern) >= 0.85)
                    .findFirst();

            if (similarPattern.isPresent() && !Boolean.TRUE.equals(forceCreate)) {
                logRepository.save(new Log(authEmployee,
                        "Обнаружен похожий шаблон: " + similarPattern.get(),
                        LocalDateTime.now()));
                throw new IllegalArgumentException(
                        String.format("Обнаружено похожее название уровня прав: «%s». Если вы уверены, что хотите создать новый шаблон с этим названием. Нажмите \"Добавить\" ещё раз для подтверждения.", similarPattern.get())
                );
            }

            // Создание
            TaskRecurrence recurrence = new TaskRecurrence();
            recurrence.setRecurrencePattern(pattern);
            TaskRecurrence saved = recurrenceRepository.save(recurrence);

            logRepository.save(new Log(authEmployee,
                    "Создан новый шаблон периодичности: " + pattern,
                    LocalDateTime.now()));

            return convertToDTO(saved);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка создания шаблона: {}, guid_employee={}",
                    e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        }
    }

    // Обновление шаблона
    @Transactional
    public TaskRecurrenceDTO updateRecurrence(
            Integer id,
            TaskRecurrenceDTO dto,
            Employee authEmployee,
            Boolean forceUpdate
    ) {
        try {
            TaskRecurrence recurrence = recurrenceRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Шаблон не найден"));

            String newPattern = dto.getRecurrencePattern().trim();

            // Валидация
            if (!newPattern.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee,
                        "Попытка обновить шаблон с недопустимыми символами",
                        LocalDateTime.now()));
                throw new IllegalArgumentException("Шаблон может содержать только буквы, пробелы и дефисы");
            }

            // Проверка на дубликат
            if (recurrenceRepository.existsByRecurrencePatternIgnoreCaseAndIdTaskRecurrenceNot(newPattern, id)) {
                logRepository.save(new Log(authEmployee,
                        "Попытка создать дубликат шаблона при обновлении",
                        LocalDateTime.now()));
                throw new IllegalArgumentException("Шаблон с таким названием уже существует");
            }

            // Проверка на схожесть
            Optional<String> similarPattern = recurrenceRepository.findAll()
                    .stream()
                    .filter(r -> !r.getIdTaskRecurrence().equals(id))
                    .map(TaskRecurrence::getRecurrencePattern)
                    .filter(p -> stringSimilarity(p, newPattern) >= 0.85)
                    .findFirst();

            if (similarPattern.isPresent() && !Boolean.TRUE.equals(forceUpdate)) {
                logRepository.save(new Log(authEmployee,
                        "Обнаружен похожий шаблон при обновлении: " + similarPattern.get(),
                        LocalDateTime.now()));
                throw new IllegalArgumentException(
                        String.format("Обнаружен похожий шаблон: «%s». Подтвердите изменение.", similarPattern.get())
                );
            }

            recurrence.setRecurrencePattern(newPattern);
            TaskRecurrence updated = recurrenceRepository.save(recurrence);

            logRepository.save(new Log(authEmployee,
                    "Обновлён шаблон периодичности ID: " + id,
                    LocalDateTime.now()));

            return convertToDTO(updated);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка обновления шаблона: {}, guid_employee={}",
                    e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        }
    }

    // Удаление шаблона
    @Transactional
    public void deleteRecurrence(Integer id, Employee authEmployee) {
        try {
            TaskRecurrence recurrence = recurrenceRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Шаблон не найден"));

            if (taskRepository.existsByTaskRecurrence(recurrence)) {
                logRepository.save(new Log(authEmployee,
                        "Попытка удалить используемый шаблон ID: " + id,
                        LocalDateTime.now()));
                throw new IllegalArgumentException("Шаблон используется в задачах и не может быть удалён");
            }

            recurrenceRepository.delete(recurrence);
            logRepository.save(new Log(authEmployee,
                    "Удалён шаблон периодичности ID: " + id,
                    LocalDateTime.now()));
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка удаления шаблона: {}, guid_employee={}",
                    e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        }
    }

    // Вспомогательные методы
    private TaskRecurrenceDTO convertToDTO(TaskRecurrence entity) {
        TaskRecurrenceDTO dto = new TaskRecurrenceDTO();
        dto.setIdTaskRecurrence(entity.getIdTaskRecurrence());
        dto.setRecurrencePattern(entity.getRecurrencePattern());
        return dto;
    }

    private double stringSimilarity(String s1, String s2) {
        s1 = s1.trim().toLowerCase();
        s2 = s2.trim().toLowerCase();
        if (s1.equals(s2)) return 1.0;

        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;

        int dist = levenshteinDistance(s1, s2);
        return 1.0 - (double) dist / maxLen;
    }

    private int levenshteinDistance(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int j = 0; j < costs.length; j++) costs[j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= s2.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        s1.charAt(i - 1) == s2.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[s2.length()];
    }
}