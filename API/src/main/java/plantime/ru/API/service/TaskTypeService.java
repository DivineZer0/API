package plantime.ru.API.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plantime.ru.API.dto.TaskTypeDTO;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.entity.EmployeeDepartment;
import plantime.ru.API.entity.Log;
import plantime.ru.API.entity.TaskType;
import plantime.ru.API.repository.TaskTypeRepository;
import plantime.ru.API.repository.LogRepository;
import plantime.ru.API.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления типами задач.
 * Обеспечивает бизнес-логику для CRUD-операций, включая проверки уникальности,
 * схожести названий и ведение журналов действий.
 */
@Service
public class TaskTypeService {

    private final TaskTypeRepository taskTypeRepository;
    private final LogRepository logRepository;

    private final TaskRepository taskRepository;
    private static final Logger logger = LoggerFactory.getLogger(TaskTypeService.class);

    public TaskTypeService(TaskTypeRepository taskTypeRepository, LogRepository logRepository, TaskRepository taskRepository) {
        this.taskTypeRepository = taskTypeRepository;
        this.logRepository = logRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Получение списка всех типов задач с сортировкой.
     *
     * @param authEmployee Аутентифицированный сотрудник.
     * @param sortBy       Поле сортировки.
     * @param order        Порядок сортировки.
     * @return Список DTO типов задач.
     * @throws IllegalArgumentException Если параметры сортировки некорректны.
     */
    public List<TaskTypeDTO> getAllTaskTypes(Employee authEmployee, String sortBy, String order) {
        try {
            if (!sortBy.equals("type")) {
                logRepository.save(new Log(authEmployee, "Недопустимое поле сортировки: " + sortBy, LocalDateTime.now()));
                logger.error("Недопустимое поле сортировки: {}, guid_employee={}", sortBy, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Сортировка возможна только по полю «Название типа задачи».");
            }

            if (!order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc")) {
                logRepository.save(new Log(authEmployee, "Недопустимый порядок сортировки: " + order, LocalDateTime.now()));
                logger.error("Недопустимый порядок сортировки: {}, guid_employee={}", order, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Порядок сортировки должен быть «asc» или «desc».");
            }

            Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(Sort.Direction.ASC, "type") : Sort.by(Sort.Direction.DESC, "type");
            List<TaskType> types = taskTypeRepository.findAll(sort);
            if (types.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Список типов задач пуст", LocalDateTime.now()));
                logger.info("Список типов задач пуст, guid_employee={}", authEmployee.getGuidEmployee());
                return List.of();
            }

            List<TaskTypeDTO> dtoList = types.stream()
                    .map(tt -> new TaskTypeDTO(tt.getIdTaskType(), tt.getType()))
                    .collect(Collectors.toList());
            logRepository.save(new Log(authEmployee, "Получен список типов задач, количество: " + types.size() + ", сортировка: " + sortBy + ", порядок: " + order, LocalDateTime.now()));
            logger.info("Успешно получен список типов задач, количество: {}, sortBy={}, order={}, guid_employee={}",
                    types.size(), sortBy, order, authEmployee.getGuidEmployee());
            return dtoList;
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при получении списка типов задач: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при получении списка типов задач: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось получить список типов задач", e);
        }
    }

    /**
     * Создаёт новый тип задачи с проверкой уникальности и схожести названия.
     *
     * @param taskTypeDTO  DTO с данными типа задачи.
     * @param authEmployee Аутентифицированный сотрудник.
     * @param forceCreate  Флаг подтверждения создания при схожести.
     * @return DTO созданного типа задачи.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public TaskTypeDTO createTaskType(TaskTypeDTO taskTypeDTO, Employee authEmployee, Boolean forceCreate) {
        try {
            String typeName = taskTypeDTO.getType().trim();
            if (!typeName.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee, "Не удалось создать тип задачи: название содержит недопустимые символы", LocalDateTime.now()));
                logger.error("Название типа задачи содержит недопустимые символы: {}, guid_employee={}", typeName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название типа задачи может содержать только буквы, пробелы и дефисы. Цифры и специальные символы запрещены.");
            }

            if (typeName.length() < 2 || typeName.length() > 20) {
                logRepository.save(new Log(authEmployee, "Не удалось создать тип задачи: длина названия не соответствует требованиям", LocalDateTime.now()));
                logger.error("Недопустимая длина названия типа задачи: {}, guid_employee={}", typeName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название типа задачи должно быть от 2 до 20 символов.");
            }

            if (taskTypeRepository.existsByTypeIgnoreCase(typeName)) {
                logRepository.save(new Log(authEmployee, "Не удалось создать тип задачи: тип с таким названием уже существует", LocalDateTime.now()));
                logger.error("Тип задачи '{}' уже существует, guid_employee={}", typeName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Тип задачи с таким названием уже существует. Пожалуйста, выберите другое название.");
            }

            List<TaskType> allTypes = taskTypeRepository.findAll();
            Optional<String> similarType = allTypes.stream()
                    .map(TaskType::getType)
                    .filter(existingName -> stringSimilarity(existingName, typeName) >= 0.85)
                    .findFirst();

            if (similarType.isPresent()) {
                if (Boolean.TRUE.equals(forceCreate)) {
                    logRepository.save(new Log(authEmployee, "Создан тип задачи с похожим названием: " + typeName + ", похож на: " + similarType.get(), LocalDateTime.now()));
                    logger.info("Создан тип задачи с похожим названием: {}, guid_employee={}", typeName, authEmployee.getGuidEmployee());
                } else {
                    logRepository.save(new Log(authEmployee,
                            "Попытка создать тип задачи с похожим названием: " + typeName + ", похоже на: " + similarType.get(),
                            LocalDateTime.now()));
                    logger.warn("Попытка создать тип задачи с похожим названием: {}, похоже на: {}, guid_employee={}",
                            typeName, similarType.get(), authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException(
                            String.format(
                                    "Обнаружено похожее название типа задачи: «%s». Если вы уверены, что хотите создать новый тип с этим названием.",
                                    similarType.get()
                            )
                    );
                }
            }

            TaskType type = new TaskType();
            type.setType(typeName);
            TaskType savedType = taskTypeRepository.save(type);
            logRepository.save(new Log(authEmployee, "Создан тип задачи: " + typeName, LocalDateTime.now()));
            logger.info("Успешно создан тип задачи: {}, guid_employee={}", typeName, authEmployee.getGuidEmployee());
            return new TaskTypeDTO(savedType.getIdTaskType(), savedType.getType());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при создании типа задачи: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при создании типа задачи: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось создать тип задачи", e);
        }
    }

    /**
     * Перегруженный метод для обратной совместимости.
     */
    @Transactional
    public TaskTypeDTO createTaskType(TaskTypeDTO taskTypeDTO, Employee authEmployee) {
        return createTaskType(taskTypeDTO, authEmployee, false);
    }

    /**
     * Обновляет существующий тип задачи с проверкой уникальности и схожести названия.
     *
     * @param id           Идентификатор типа задачи.
     * @param taskTypeDTO  DTO с обновлёнными данными.
     * @param authEmployee Аутентифицированный сотрудник.
     * @param forceUpdate  Флаг подтверждения обновления при схожести.
     * @return DTO обновлённого типа задачи.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public TaskTypeDTO updateTaskType(Integer id, TaskTypeDTO taskTypeDTO, Employee authEmployee, Boolean forceUpdate) {
        try {
            Optional<TaskType> existing = taskTypeRepository.findById(id);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить тип задачи: тип не найден (id " + id + ")", LocalDateTime.now()));
                logger.error("Тип задачи с id {} не найден, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Тип задачи с указанным идентификатором не найден.");
            }

            String typeName = taskTypeDTO.getType().trim();
            if (!typeName.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить тип задачи: название содержит недопустимые символы", LocalDateTime.now()));
                logger.error("Название типа задачи содержит недопустимые символы: {}, guid_employee={}", typeName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название типа задачи может содержать только буквы, пробелы и дефисы. Цифры и специальные символы запрещены.");
            }

            if (typeName.length() < 2 || typeName.length() > 20) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить тип задачи: длина названия не соответствует требованиям", LocalDateTime.now()));
                logger.error("Недопустимая длина названия типа задачи: {}, guid_employee={}", typeName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название типа задачи должно быть от 2 до 20 символов.");
            }

            List<TaskType> allTypes = taskTypeRepository.findAll();
            for (TaskType type : allTypes) {
                if (!type.getIdTaskType().equals(id)
                        && type.getType().equalsIgnoreCase(typeName)) {
                    logRepository.save(new Log(authEmployee, "Не удалось обновить тип задачи: тип с таким названием уже существует", LocalDateTime.now()));
                    logger.error("Тип задачи '{}' уже существует, guid_employee={}", typeName, authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException("Тип задачи с таким названием уже существует. Пожалуйста, выберите другое название.");
                }
            }

            Optional<String> similarType = allTypes.stream()
                    .filter(type -> !type.getIdTaskType().equals(id))
                    .map(TaskType::getType)
                    .filter(existingName -> stringSimilarity(existingName, typeName) >= 0.85)
                    .findFirst();

            if (similarType.isPresent()) {
                if (Boolean.TRUE.equals(forceUpdate)) {
                    logRepository.save(new Log(authEmployee, "Обновлён тип задачи на похожее название: " + typeName + ", похоже на: " + similarType.get(), LocalDateTime.now()));
                    logger.info("Обновлён тип задачи на похожее название: {}, guid_employee={}", typeName, authEmployee.getGuidEmployee());
                } else {
                    logRepository.save(new Log(authEmployee,
                            "Попытка обновить тип задачи на похожее название: " + typeName + ", похоже на: " + similarType.get(),
                            LocalDateTime.now()));
                    logger.warn("Попытка обновления типа задачи на похожее название: {}, похоже на: {}, guid_employee={}",
                            typeName, similarType.get(), authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException(
                            String.format(
                                    "Обнаружено похожее название типа задачи: «%s». Если вы уверены, что хотите обновить тип задачи до этого названия.",
                                    similarType.get()
                            )
                    );
                }
            }

            TaskType type = existing.get();
            type.setType(typeName);
            TaskType updatedType = taskTypeRepository.save(type);
            logRepository.save(new Log(authEmployee, "Обновлён тип задачи: " + typeName, LocalDateTime.now()));
            logger.info("Успешно обновлён тип задачи: {}, guid_employee={}", typeName, authEmployee.getGuidEmployee());
            return new TaskTypeDTO(updatedType.getIdTaskType(), updatedType.getType());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при обновлении типа задачи: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при обновлении типа задачи: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось обновить тип задачи", e);
        }
    }

    /**
     * Перегруженный метод для обратной совместимости.
     */
    @Transactional
    public TaskTypeDTO updateTaskType(Integer id, TaskTypeDTO taskTypeDTO, Employee authEmployee) {
        return updateTaskType(id, taskTypeDTO, authEmployee, false);
    }

    /**
     * Удаляет тип задачи по идентификатору.
     * Проверяет, что тип задачи не используется в других сущностях (добавьте логику при необходимости).
     *
     * @param id           Идентификатор типа задачи.
     * @param authEmployee Аутентифицированный сотрудник.
     * @throws IllegalArgumentException При ошибке удаления или наличии зависимостей.
     */
    @Transactional
    public void deleteTaskType(Integer id, Employee authEmployee) {
        try {
            Optional<TaskType> existing = taskTypeRepository.findById(id);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить тип задачи: тип не найден (id " + id + ")", LocalDateTime.now()));
                logger.error("Тип задачи с id {} не найден, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Тип задачи с указанным идентификатором не найден.");
            }

            TaskType taskType = existing.get();
            boolean isUsed = taskRepository.existsByTaskType(taskType);
            if (isUsed) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить тип задачи: тип задачи используется в задачах (id " + id + ")", LocalDateTime.now()));
                logger.error("Тип задачи с id {} используется задачей, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Невозможно удалить тип задачи, так как он используется в задачах.");
            }

            taskTypeRepository.deleteById(id);
            taskTypeRepository.flush();
            logRepository.save(new Log(authEmployee, "Удалён тип задачи с id: " + id, LocalDateTime.now()));
            logger.info("Успешно удалён тип задачи с id: {}, guid_employee={}", id, authEmployee.getGuidEmployee());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Нарушение целостности данных при удалении типа задачи с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось удалить тип задачи из-за связей в базе данных. Проверьте, не используется ли тип задачи в других сущностях.", e);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при удалении типа задачи: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при удалении типа задачи с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось удалить тип задачи", e);
        }
    }

    /**
     * Возвращает степень схожести между двумя строками.
     * Алгоритм основан на расстоянии Левенштейна.
     *
     * @param s1 Первая строка.
     * @param s2 Вторая строка.
     * @return Коэффициент схожести.
     */
    public static double stringSimilarity(String s1, String s2) {
        s1 = s1.trim().toLowerCase();
        s2 = s2.trim().toLowerCase();
        if (s1.equals(s2)) return 1.0;
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        int dist = levenshteinDistance(s1, s2);
        return 1.0 - (double) dist / maxLen;
    }

    /**
     * Вычисляет расстояние Левенштейна между двумя строками.
     *
     * @param s1 Первая строка.
     * @param s2 Вторая строка.
     * @return Расстояние Левенштейна.
     */
    private static int levenshteinDistance(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= s2.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), s1.charAt(i - 1) == s2.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[s2.length()];
    }
}