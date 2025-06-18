package plantime.ru.API.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plantime.ru.API.dto.TaskStatusDTO;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.entity.Log;
import plantime.ru.API.entity.TaskStatus;
import plantime.ru.API.repository.TaskStatusRepository;
import plantime.ru.API.repository.LogRepository;
import plantime.ru.API.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления статусами задач.
 * Обеспечивает бизнес-логику для CRUD-операций, включая проверки уникальности,
 * схожести названий и ведение журналов действий.
 */
@Service
public class TaskStatusService {

    private final TaskStatusRepository statusRepository;
    private final LogRepository logRepository;
    private static final Logger logger = LoggerFactory.getLogger(TaskStatusService.class);
    private final TaskRepository taskRepository;

    /**
     * Конструктор сервиса.
     *
     * @param statusRepository Репозиторий статусов задач.
     * @param logRepository    Репозиторий логов.
     */
    public TaskStatusService(
            TaskStatusRepository statusRepository, LogRepository logRepository, TaskRepository taskRepository) {
        this.statusRepository = statusRepository;
        this.logRepository = logRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Получение списка всех статусов задач с сортировкой.
     *
     * @param authEmployee Аутентифицированный сотрудник.
     * @param sortBy       Поле сортировки.
     * @param order        Порядок сортировки.
     * @return Список DTO статусов задач.
     * @throws IllegalArgumentException Если параметры сортировки некорректны.
     */
    public List<TaskStatusDTO> getAllStatuses(Employee authEmployee, String sortBy, String order) {
        try {
            if (!sortBy.equals("status")) {
                logRepository.save(new Log(authEmployee, "Недопустимое поле сортировки: " + sortBy, LocalDateTime.now()));
                logger.error("Недопустимое поле сортировки: {}, guid_employee={}", sortBy, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Сортировка возможна только по полю «Название статуса задачи».");
            }

            if (!order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc")) {
                logRepository.save(new Log(authEmployee, "Недопустимый порядок сортировки: " + order, LocalDateTime.now()));
                logger.error("Недопустимый порядок сортировки: {}, guid_employee={}", order, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Порядок сортировки должен быть «asc» (по возрастанию) или «desc» (по убыванию).");
            }

            Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(Sort.Direction.ASC, "status") : Sort.by(Sort.Direction.DESC, "status");
            List<TaskStatus> statuses = statusRepository.findAll(sort);
            if (statuses.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Список статусов задач пуст", LocalDateTime.now()));
                logger.info("Список статусов задач пуст, guid_employee={}", authEmployee.getGuidEmployee());
                return List.of();
            }

            List<TaskStatusDTO> statusDTOs = statuses.stream()
                    .map(s -> new TaskStatusDTO(s.getIdTaskStatus(), s.getStatus()))
                    .collect(Collectors.toList());
            logRepository.save(new Log(authEmployee, "Получен список статусов задач, количество: " + statuses.size() + ", сортировка: " + sortBy + ", порядок: " + order, LocalDateTime.now()));
            logger.info("Успешно получен список статусов задач, количество: {}, sortBy={}, order={}, guid_employee={}",
                    statuses.size(), sortBy, order, authEmployee.getGuidEmployee());
            return statusDTOs;
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при получении списка статусов задач: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при получении списка статусов задач: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при получении списка статусов задач. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Создание нового статуса задачи с проверкой уникальности и схожести названия.
     *
     * @param statusDTO    DTO с данными статуса задачи.
     * @param authEmployee Аутентифицированный сотрудник.
     * @param forceCreate  Флаг подтверждения создания при схожести.
     * @return DTO созданного статуса задачи.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public TaskStatusDTO createStatus(TaskStatusDTO statusDTO, Employee authEmployee, Boolean forceCreate) {
        try {
            String statusName = statusDTO.getStatus().trim();
            if (!statusName.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee, "Не удалось создать статус задачи: название содержит недопустимые символы", LocalDateTime.now()));
                logger.error("Название статуса задачи содержит недопустимые символы: {}, guid_employee={}", statusName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название статуса задачи может содержать только буквы, пробелы и дефисы. Цифры и специальные символы запрещены.");
            }

            if (statusName.length() < 2 || statusName.length() > 40) {
                logRepository.save(new Log(authEmployee, "Не удалось создать статус задачи: длина названия не соответствует требованиям", LocalDateTime.now()));
                logger.error("Недопустимая длина названия статуса задачи: {}, guid_employee={}", statusName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название статуса задачи должно быть от 2 до 40 символов.");
            }

            if (statusRepository.existsByStatusIgnoreCase(statusName)) {
                logRepository.save(new Log(authEmployee, "Не удалось создать статус задачи: статус с таким названием уже существует", LocalDateTime.now()));
                logger.error("Статус задачи '{}' уже существует, guid_employee={}", statusName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Статус задачи с таким названием уже существует. Пожалуйста, выберите другое название.");
            }

            List<TaskStatus> allStatuses = statusRepository.findAll();
            Optional<String> similarStatus = allStatuses.stream()
                    .map(TaskStatus::getStatus)
                    .filter(existingName -> stringSimilarity(existingName, statusName) >= 0.85)
                    .findFirst();

            if (similarStatus.isPresent()) {
                if (Boolean.TRUE.equals(forceCreate)) {
                    logRepository.save(new Log(authEmployee, "Создан статус задачи с похожим названием: " + statusName + ", похож на: " + similarStatus.get(), LocalDateTime.now()));
                    logger.info("Создан статус задачи с похожим названием: {}, guid_employee={}", statusName, authEmployee.getGuidEmployee());
                } else {
                    logRepository.save(new Log(authEmployee,
                            "Попытка создать статус задачи с похожим названием: " + statusName + ", похоже на: " + similarStatus.get(),
                            LocalDateTime.now()));
                    logger.warn("Попытка создать статус задачи с похожим названием: {}, похоже на: {}, guid_employee={}",
                            statusName, similarStatus.get(), authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException(
                            String.format(
                                    "Обнаружено похожее название статуса задачи: «%s». Если вы уверены, что хотите создать новый статус с этим названием.",
                                    similarStatus.get()
                            )
                    );
                }
            }

            TaskStatus status = new TaskStatus();
            status.setStatus(statusName);
            TaskStatus savedStatus = statusRepository.save(status);
            logRepository.save(new Log(authEmployee, "Создан статус задачи: " + statusName, LocalDateTime.now()));
            logger.info("Успешно создан статус задачи: {}, guid_employee={}", statusName, authEmployee.getGuidEmployee());
            return new TaskStatusDTO(savedStatus.getIdTaskStatus(), savedStatus.getStatus());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при создании статуса задачи: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при создании статуса задачи: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при создании статуса задачи. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Перегруженный метод для обратной совместимости.
     */
    @Transactional
    public TaskStatusDTO createStatus(TaskStatusDTO statusDTO, Employee authEmployee) {
        return createStatus(statusDTO, authEmployee, false);
    }

    /**
     * Обновление существующего статуса задачи с проверкой уникальности и схожести названия.
     *
     * @param id           Идентификатор статуса задачи.
     * @param statusDTO    DTO с обновлёнными данными статуса.
     * @param authEmployee Аутентифицированный сотрудник.
     * @param forceUpdate  Флаг подтверждения обновления при схожести.
     * @return DTO обновлённого статуса задачи.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public TaskStatusDTO updateStatus(Integer id, TaskStatusDTO statusDTO, Employee authEmployee, Boolean forceUpdate) {
        try {
            Optional<TaskStatus> existing = statusRepository.findById(id);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить статус задачи: статус не найден (id " + id + ")", LocalDateTime.now()));
                logger.error("Статус задачи с id {} не найден, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Статус задачи с указанным идентификатором не найден.");
            }

            String statusName = statusDTO.getStatus().trim();
            if (!statusName.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить статус задачи: название содержит недопустимые символы", LocalDateTime.now()));
                logger.error("Название статуса задачи содержит недопустимые символы: {}, guid_employee={}", statusName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название статуса задачи может содержать только буквы, пробелы и дефисы. Цифры и специальные символы запрещены.");
            }

            if (statusName.length() < 2 || statusName.length() > 40) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить статус задачи: длина названия не соответствует требованиям", LocalDateTime.now()));
                logger.error("Недопустимая длина названия статуса задачи: {}, guid_employee={}", statusName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название статуса задачи должно быть от 2 до 40 символов.");
            }

            List<TaskStatus> allStatuses = statusRepository.findAll();
            for (TaskStatus status : allStatuses) {
                if (!status.getIdTaskStatus().equals(id)
                        && status.getStatus().equalsIgnoreCase(statusName)) {
                    logRepository.save(new Log(authEmployee, "Не удалось обновить статус задачи: статус с таким названием уже существует", LocalDateTime.now()));
                    logger.error("Статус задачи '{}' уже существует, guid_employee={}", statusName, authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException("Статус задачи с таким названием уже существует. Пожалуйста, выберите другое название.");
                }
            }

            Optional<String> similarStatus = allStatuses.stream()
                    .filter(status -> !status.getIdTaskStatus().equals(id))
                    .map(TaskStatus::getStatus)
                    .filter(existingName -> stringSimilarity(existingName, statusName) >= 0.85)
                    .findFirst();

            if (similarStatus.isPresent()) {
                if (Boolean.TRUE.equals(forceUpdate)) {
                    logRepository.save(new Log(authEmployee, "Обновлён статус задачи на похожее название: " + statusName + ", похоже на: " + similarStatus.get(), LocalDateTime.now()));
                    logger.info("Обновлён статус задачи на похожее название: {}, guid_employee={}", statusName, authEmployee.getGuidEmployee());
                } else {
                    logRepository.save(new Log(authEmployee,
                            "Попытка обновить статус задачи на похожее название: " + statusName + ", похоже на: " + similarStatus.get(),
                            LocalDateTime.now()));
                    logger.warn("Попытка обновления статуса задачи на похожее название: {}, похоже на: {}, guid_employee={}",
                            statusName, similarStatus.get(), authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException(
                            String.format(
                                    "Обнаружено похожее название статуса задачи: «%s». Если вы уверены, что хотите обновить статус до этого названия.",
                                    similarStatus.get()
                            )
                    );
                }
            }

            TaskStatus status = existing.get();
            status.setStatus(statusName);
            TaskStatus updatedStatus = statusRepository.save(status);
            logRepository.save(new Log(authEmployee, "Обновлён статус задачи: " + statusName, LocalDateTime.now()));
            logger.info("Успешно обновлён статус задачи: {}, guid_employee={}", statusName, authEmployee.getGuidEmployee());
            return new TaskStatusDTO(updatedStatus.getIdTaskStatus(), updatedStatus.getStatus());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при обновлении статуса задачи: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при обновлении статуса задачи: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при обновлении статуса задачи. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Перегруженный метод обновления для обратной совместимости.
     */
    @Transactional
    public TaskStatusDTO updateStatus(Integer id, TaskStatusDTO statusDTO, Employee authEmployee) {
        return updateStatus(id, statusDTO, authEmployee, false);
    }

    /**
     * Удаление статуса задачи по идентификатору.
     * Проверяет, что статус задачи не используется в других сущностях (добавьте логику при необходимости).
     *
     * @param id           Идентификатор статуса задачи.
     * @param authEmployee Аутентифицированный сотрудник.
     * @throws IllegalArgumentException При ошибке удаления или наличии зависимостей.
     */
    @Transactional
    public void deleteStatus(Integer id, Employee authEmployee) {
        try {
            Optional<TaskStatus> existing = statusRepository.findById(id);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить статус задачи: статус не найден (id " + id + ")", LocalDateTime.now()));
                logger.error("Статус задачи с id {} не найден, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Статус задачи с указанным идентификатором не найден.");
            }

            TaskStatus taskStatus = existing.get();
            boolean isUsed = taskRepository.existsByTaskStatus(taskStatus);
            if (isUsed) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить статус задачи: статус задачи используется в задачах (id " + id + ")", LocalDateTime.now()));
                logger.error("Статус задачи с id {} используется задачей, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Невозможно удалить статус задачи, так как он используется в задачах.");
            }

            statusRepository.deleteById(id);
            statusRepository.flush();
            logRepository.save(new Log(authEmployee, "Удалён статус задачи с id: " + id, LocalDateTime.now()));
            logger.info("Успешно удалён статус задачи с id: {}, guid_employee={}", id, authEmployee.getGuidEmployee());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Нарушение целостности данных при удалении статуса задачи с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось удалить статус задачи из-за связей в базе данных. Проверьте, не используется ли статус задачи в других сущностях.", e);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при удалении статуса задачи: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при удалении статуса задачи с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при удалении статуса задачи. Пожалуйста, попробуйте позже.", e);
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