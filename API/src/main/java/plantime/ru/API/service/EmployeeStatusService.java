package plantime.ru.API.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plantime.ru.API.dto.EmployeeStatusDTO;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.entity.EmployeeStatus;
import plantime.ru.API.entity.Log;
import plantime.ru.API.repository.EmployeeStatusRepository;
import plantime.ru.API.repository.EmployeeRepository;
import plantime.ru.API.repository.LogRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для управления статусами сотрудников.
 * Обеспечивает бизнес-логику для CRUD-операций со статусами, включая проверки уникальности,
 * схожести названий и ведение журналов действий.
 */
@Service
public class EmployeeStatusService {

    private final EmployeeStatusRepository statusRepository;
    private final EmployeeRepository employeeRepository;
    private final LogRepository logRepository;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeStatusService.class);

    /**
     * Конструктор сервиса.
     *
     * @param statusRepository   Репозиторий статусов.
     * @param employeeRepository Репозиторий сотрудников.
     * @param logRepository      Репозиторий логов.
     */
    public EmployeeStatusService(
            EmployeeStatusRepository statusRepository,
            EmployeeRepository employeeRepository,
            LogRepository logRepository) {
        this.statusRepository = statusRepository;
        this.employeeRepository = employeeRepository;
        this.logRepository = logRepository;
    }

    /**
     * Получение списка всех статусов с сортировкой.
     *
     * @param authEmployee Аутентифицированный сотрудник.
     * @param sortBy       Поле сортировки.
     * @param order        Порядок сортировки.
     * @return Список DTO статусов.
     * @throws IllegalArgumentException Если параметры сортировки некорректны.
     */
    public List<EmployeeStatusDTO> getAllStatuses(Employee authEmployee, String sortBy, String order) {
        try {
            if (!sortBy.equals("status")) {
                logRepository.save(new Log(authEmployee, "Недопустимое поле сортировки: " + sortBy, LocalDateTime.now()));
                logger.error("Недопустимое поле сортировки: {}, guid_employee={}", sortBy, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Сортировка возможна только по полю «Название статуса».");
            }

            if (!order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc")) {
                logRepository.save(new Log(authEmployee, "Недопустимый порядок сортировки: " + order, LocalDateTime.now()));
                logger.error("Недопустимый порядок сортировки: {}, guid_employee={}", order, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Порядок сортировки должен быть «asc» (по возрастанию) или «desc» (по убыванию).");
            }

            Sort sort = order.equalsIgnoreCase("asc")
                    ? Sort.by(Sort.Direction.ASC, "status")
                    : Sort.by(Sort.Direction.DESC, "status");
            List<EmployeeStatus> statuses = statusRepository.findAll(sort);

            if (statuses.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Список статусов пуст", LocalDateTime.now()));
                logger.info("Список статусов пуст, guid_employee={}", authEmployee.getGuidEmployee());
                return List.of();
            }

            List<EmployeeStatusDTO> statusDTOs = statuses.stream()
                    .map(status -> new EmployeeStatusDTO(status.getIdEmployeeStatus(), status.getStatus()))
                    .collect(Collectors.toList());
            logRepository.save(new Log(authEmployee, "Получен список статусов, количество: " + statuses.size() + ", сортировка: " + sortBy + ", порядок: " + order, LocalDateTime.now()));
            logger.info("Успешно получен список статусов, количество: {}, sortBy={}, order={}, guid_employee={}",
                    statuses.size(), sortBy, order, authEmployee.getGuidEmployee());
            return statusDTOs;
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при получении списка статусов: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при получении списка статусов: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при получении списка статусов. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Создание нового статуса с проверкой уникальности и схожести названия.
     *
     * @param statusDTO    DTO с данными статуса.
     * @param authEmployee Аутентифицированный сотрудник.
     * @param forceCreate  Флаг подтверждения создания при схожести.
     * @return DTO созданного статуса.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public EmployeeStatusDTO createStatus(EmployeeStatusDTO statusDTO, Employee authEmployee, Boolean forceCreate) {
        try {
            String statusName = statusDTO.getStatus().trim();
            if (!statusName.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee, "Не удалось создать статус: название содержит недопустимые символы", LocalDateTime.now()));
                logger.error("Название статуса содержит недопустимые символы: {}, guid_employee={}", statusName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название статуса может содержать только буквы, пробелы и дефисы. Цифры и специальные символы запрещены.");
            }

            if (statusName.length() < 3 || statusName.length() > 20) {
                logRepository.save(new Log(authEmployee, "Не удалось создать статус: длина названия не соответствует требованиям", LocalDateTime.now()));
                logger.error("Недопустимая длина названия статуса: {}, guid_employee={}", statusName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название статуса должно быть от 3 до 20 символов.");
            }

            if (statusRepository.existsByStatusIgnoreCase(statusName)) {
                logRepository.save(new Log(authEmployee, "Не удалось создать статус: статус с таким названием уже существует", LocalDateTime.now()));
                logger.error("Статус '{}' уже существует, guid_employee={}", statusName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Статус с таким названием уже существует. Пожалуйста, выберите другое название.");
            }

            List<EmployeeStatus> allStatuses = statusRepository.findAll();
            Optional<String> similarStatus = allStatuses.stream()
                    .map(EmployeeStatus::getStatus)
                    .filter(existingName -> stringSimilarity(existingName, statusName) >= 0.85)
                    .findFirst();

            if (similarStatus.isPresent()) {
                if (Boolean.TRUE.equals(forceCreate)) {
                    logRepository.save(new Log(authEmployee, "Создан статус с похожим названием: " + statusName + ", похож на: " + similarStatus.get(), LocalDateTime.now()));
                    logger.info("Создан статус с похожим названием: {}, guid_employee={}", statusName, authEmployee.getGuidEmployee());
                } else {
                    logRepository.save(new Log(authEmployee,
                            "Попытка создать статус с похожим названием: " + statusName + ", похоже на: " + similarStatus.get(),
                            LocalDateTime.now()));
                    logger.warn("Попытка создать статус с похожим названием: {}, похоже на: {}, guid_employee={}",
                            statusName, similarStatus.get(), authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException(
                            String.format(
                                    "Обнаружено похожее название статуса: «%s». Если вы уверены, что хотите создать новый статус с этим названием.",
                                    similarStatus.get()
                            )
                    );
                }
            }

            EmployeeStatus status = new EmployeeStatus();
            status.setStatus(statusName);
            EmployeeStatus savedStatus = statusRepository.save(status);
            logRepository.save(new Log(authEmployee, "Создан статус: " + statusName, LocalDateTime.now()));
            logger.info("Успешно создан статус: {}, guid_employee={}", statusName, authEmployee.getGuidEmployee());
            return new EmployeeStatusDTO(savedStatus.getIdEmployeeStatus(), savedStatus.getStatus());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при создании статуса: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при создании статуса: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при создании статуса. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Перегруженный метод для обратной совместимости.
     */
    @Transactional
    public EmployeeStatusDTO createStatus(EmployeeStatusDTO statusDTO, Employee authEmployee) {
        return createStatus(statusDTO, authEmployee, false);
    }

    /**
     * Обновление существующего статуса с проверкой уникальности и схожести названия.
     *
     * @param id           Идентификатор статуса.
     * @param statusDTO    DTO с обновленными данными.
     * @param authEmployee Аутентифицированный сотрудник.
     * @param forceUpdate  Флаг подтверждения обновления при схожести.
     * @return DTO обновленного статуса.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public EmployeeStatusDTO updateStatus(Integer id, EmployeeStatusDTO statusDTO, Employee authEmployee, Boolean forceUpdate) {
        try {
            Optional<EmployeeStatus> existing = statusRepository.findById(id);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить статус: статус не найден (id " + id + ")", LocalDateTime.now()));
                logger.error("Статус с id {} не найден, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Статус с указанным идентификатором не найден.");
            }

            String statusName = statusDTO.getStatus().trim();
            if (!statusName.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить статус: название содержит недопустимые символы", LocalDateTime.now()));
                logger.error("Название статуса содержит недопустимые символы: {}, guid_employee={}", statusName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название статуса может содержать только буквы, пробелы и дефисы. Цифры и специальные символы запрещены.");
            }

            if (statusName.length() < 3 || statusName.length() > 20) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить статус: длина названия не соответствует требованиям", LocalDateTime.now()));
                logger.error("Недопустимая длина названия статуса: {}, guid_employee={}", statusName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название статуса должно быть от 3 до 20 символов.");
            }

            List<EmployeeStatus> allStatuses = statusRepository.findAll();
            for (EmployeeStatus status : allStatuses) {
                if (!status.getIdEmployeeStatus().equals(id)
                        && status.getStatus().equalsIgnoreCase(statusName)) {
                    logRepository.save(new Log(authEmployee, "Не удалось обновить статус: статус с таким названием уже существует", LocalDateTime.now()));
                    logger.error("Статус '{}' уже существует, guid_employee={}", statusName, authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException("Статус с таким названием уже существует. Пожалуйста, выберите другое название.");
                }
            }

            Optional<String> similarStatus = allStatuses.stream()
                    .filter(status -> !status.getIdEmployeeStatus().equals(id))
                    .map(EmployeeStatus::getStatus)
                    .filter(existingName -> stringSimilarity(existingName, statusName) >= 0.85)
                    .findFirst();

            if (similarStatus.isPresent()) {
                if (Boolean.TRUE.equals(forceUpdate)) {
                    logRepository.save(new Log(authEmployee, "Обновлен статус с похожим названием: " + statusName + ", похоже на: " + similarStatus.get(), LocalDateTime.now()));
                    logger.info("Обновлен статус с похожим названием: {}, guid_employee={}", statusName, authEmployee.getGuidEmployee());
                } else {
                    logRepository.save(new Log(authEmployee,
                            "Попытка обновить статус на похожее название: " + statusName + ", похоже на: " + similarStatus.get(),
                            LocalDateTime.now()));
                    logger.warn("Попытка обновления статуса на похожее название: {}, похоже на: {}, guid_employee={}",
                            statusName, similarStatus.get(), authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException(
                            String.format(
                                    "Обнаружено похожее название статуса: «%s». Если вы уверены, что хотите обновить статус до этого названия.",
                                    similarStatus.get()
                            )
                    );
                }
            }

            EmployeeStatus status = existing.get();
            status.setStatus(statusName);
            EmployeeStatus updatedStatus = statusRepository.save(status);
            logRepository.save(new Log(authEmployee, "Обновлен статус: " + statusName, LocalDateTime.now()));
            logger.info("Успешно обновлен статус: {}, guid_employee={}", statusName, authEmployee.getGuidEmployee());
            return new EmployeeStatusDTO(updatedStatus.getIdEmployeeStatus(), updatedStatus.getStatus());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при обновлении статуса: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при обновлении статуса: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при обновлении статуса. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Перегруженный метод обновления для обратной совместимости.
     */
    @Transactional
    public EmployeeStatusDTO updateStatus(Integer id, EmployeeStatusDTO statusDTO, Employee authEmployee) {
        return updateStatus(id, statusDTO, authEmployee, false);
    }

    /**
     * Удаление статуса по идентификатору.
     * Проверяет, что статус не используется сотрудниками.
     *
     * @param id           Идентификатор статуса.
     * @param authEmployee Аутентифицированный сотрудник.
     * @throws IllegalArgumentException При ошибке удаления или если есть зависимые сотрудники.
     */
    @Transactional
    public void deleteStatus(Integer id, Employee authEmployee) {
        try {
            Optional<EmployeeStatus> existing = statusRepository.findById(id);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить статус: статус не найден (id " + id + ")", LocalDateTime.now()));
                logger.error("Статус с id {} не найден, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Статус с указанным идентификатором не найден.");
            }

            EmployeeStatus status = existing.get();
            boolean isUsed = employeeRepository.existsByEmployeeStatus(status);
            if (isUsed) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить статус: статус используется сотрудниками (id " + id + ")", LocalDateTime.now()));
                logger.error("Статус с id {} используется сотрудниками, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Невозможно удалить статус, так как он используется сотрудниками.");
            }

            statusRepository.deleteById(id);
            statusRepository.flush();
            logRepository.save(new Log(authEmployee, "Удален статус с id: " + id, LocalDateTime.now()));
            logger.info("Успешно удален статус с id: {}, guid_employee={}", id, authEmployee.getGuidEmployee());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Нарушение целостности данных при удалении статуса с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось удалить статус из-за связей в базе данных. Проверьте, не используется ли статус сотрудниками.", e);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при удалении статуса: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при удалении статуса с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при удалении статуса. Пожалуйста, попробуйте позже.", e);
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