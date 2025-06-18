package plantime.ru.API.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plantime.ru.API.dto.EmployeePermissionDTO;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.entity.EmployeePermission;
import plantime.ru.API.entity.Log;
import plantime.ru.API.repository.EmployeePermissionRepository;
import plantime.ru.API.repository.EmployeePostRepository;
import plantime.ru.API.repository.EmployeeRepository;
import plantime.ru.API.repository.LogRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для управления уровнями прав доступа сотрудников.
 * Обеспечивает бизнес-логику для CRUD-операций, включая проверки уникальности,
 * схожести названий и ведение журналов действий.
 */
@Service
public class EmployeePermissionService {

    private final EmployeePermissionRepository permissionRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeePostRepository employeePostRepository;
    private final LogRepository logRepository;
    private static final Logger logger = LoggerFactory.getLogger(EmployeePermissionService.class);

    /**
     * Конструктор сервиса.
     *
     * @param permissionRepository   Репозиторий уровней прав.
     * @param employeeRepository     Репозиторий сотрудников.
     * @param logRepository          Репозиторий логов.
     * @param employeePostRepository Репозиторий должностей.
     */
    public EmployeePermissionService(
            EmployeePermissionRepository permissionRepository,
            EmployeeRepository employeeRepository,
            LogRepository logRepository,
            EmployeePostRepository employeePostRepository) {
        this.permissionRepository = permissionRepository;
        this.employeeRepository = employeeRepository;
        this.logRepository = logRepository;
        this.employeePostRepository = employeePostRepository;
    }

    /**
     * Получение списка всех уровней прав доступа с сортировкой.
     *
     * @param authEmployee Аутентифицированный сотрудник.
     * @param sortBy       Поле сортировки.
     * @param order        Порядок сортировки.
     * @return Список DTO уровней прав.
     * @throws IllegalArgumentException Если параметры сортировки некорректны.
     */
    public List<EmployeePermissionDTO> getAllPermissions(Employee authEmployee, String sortBy, String order) {
        try {
            if (!sortBy.equals("permission")) {
                logRepository.save(new Log(authEmployee, "Недопустимое поле сортировки: " + sortBy, LocalDateTime.now()));
                logger.error("Недопустимое поле сортировки: {}, guid_employee={}", sortBy, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Сортировка возможна только по полю «Название уровня прав доступа».");
            }

            if (!order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc")) {
                logRepository.save(new Log(authEmployee, "Недопустимый порядок сортировки: " + order, LocalDateTime.now()));
                logger.error("Недопустимый порядок сортировки: {}, guid_employee={}", order, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Порядок сортировки должен быть «asc» (по возрастанию) или «desc» (по убыванию).");
            }

            Sort sort = order.equalsIgnoreCase("asc")
                    ? Sort.by(Sort.Direction.ASC, "permission")
                    : Sort.by(Sort.Direction.DESC, "permission");
            List<EmployeePermission> permissions = permissionRepository.findAll(sort);

            if (permissions.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Список уровней прав доступа пуст", LocalDateTime.now()));
                logger.info("Список уровней прав доступа пуст, guid_employee={}", authEmployee.getGuidEmployee());
                return List.of();
            }

            List<EmployeePermissionDTO> permissionDTOs = permissions.stream()
                    .map(perm -> new EmployeePermissionDTO(perm.getIdEmployeePermission(), perm.getPermission()))
                    .collect(Collectors.toList());
            logRepository.save(new Log(authEmployee, "Получен список уровней прав доступа, количество: " + permissions.size() + ", сортировка: " + sortBy + ", порядок: " + order, LocalDateTime.now()));
            logger.info("Успешно получен список уровней прав доступа, количество: {}, sortBy={}, order={}, guid_employee={}",
                    permissions.size(), sortBy, order, authEmployee.getGuidEmployee());
            return permissionDTOs;
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при получении списка уровней прав доступа: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при получении списка уровней прав доступа: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при получении списка уровней прав доступа. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Создание нового уровня прав доступа с проверкой уникальности и схожести названия.
     *
     * @param permissionDTO DTO с данными уровня прав.
     * @param authEmployee  Аутентифицированный сотрудник.
     * @param forceCreate   Флаг подтверждения создания при схожести.
     * @return DTO созданного уровня прав.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public EmployeePermissionDTO createPermission(EmployeePermissionDTO permissionDTO, Employee authEmployee, Boolean forceCreate) {
        try {
            String permissionName = permissionDTO.getPermission().trim();
            if (!permissionName.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee, "Не удалось создать уровень прав доступа: название содержит недопустимые символы", LocalDateTime.now()));
                logger.error("Название уровня прав доступа содержит недопустимые символы: {}, guid_employee={}", permissionName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название уровня прав доступа может содержать только буквы, пробелы и дефисы. Цифры и специальные символы запрещены.");
            }

            if (permissionName.length() < 3 || permissionName.length() > 40) {
                logRepository.save(new Log(authEmployee, "Не удалось создать уровень прав доступа: длина названия не соответствует требованиям", LocalDateTime.now()));
                logger.error("Недопустимая длина названия уровня прав доступа: {}, guid_employee={}", permissionName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название уровня прав доступа должно быть от 3 до 40 символов.");
            }

            if (permissionRepository.existsByPermissionIgnoreCase(permissionName)) {
                logRepository.save(new Log(authEmployee, "Не удалось создать уровень прав доступа: уровень с таким названием уже существует", LocalDateTime.now()));
                logger.error("Уровень прав доступа '{}' уже существует, guid_employee={}", permissionName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Уровень прав доступа с таким названием уже существует. Пожалуйста, выберите другое название.");
            }

            List<EmployeePermission> allPermissions = permissionRepository.findAll();
            Optional<String> similarPermission = allPermissions.stream()
                    .map(EmployeePermission::getPermission)
                    .filter(existingName -> stringSimilarity(existingName, permissionName) >= 0.85)
                    .findFirst();

            if (similarPermission.isPresent()) {
                if (Boolean.TRUE.equals(forceCreate)) {
                    logRepository.save(new Log(authEmployee, "Создан уровень прав с похожим названием: " + permissionName + ", похоже на: " + similarPermission.get(), LocalDateTime.now()));
                    logger.info("Создан уровень прав с похожим названием: {}, guid_employee={}", permissionName, authEmployee.getGuidEmployee());
                } else {
                    logRepository.save(new Log(authEmployee,
                            "Попытка создать уровень прав с похожим названием: " + permissionName + ", похоже на: " + similarPermission.get(),
                            LocalDateTime.now()));
                    logger.warn("Попытка создать уровень прав с похожим названием: {}, похоже на: {}, guid_employee={}",
                            permissionName, similarPermission.get(), authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException(
                            String.format(
                                    "Обнаружено похожее название уровня прав: «%s». Если вы уверены, что хотите создать новый уровень с этим названием.",
                                    similarPermission.get()
                            )
                    );
                }
            }

            EmployeePermission permission = new EmployeePermission();
            permission.setPermission(permissionName);
            EmployeePermission savedPermission = permissionRepository.save(permission);
            logRepository.save(new Log(authEmployee, "Создан уровень прав доступа: " + permissionName, LocalDateTime.now()));
            logger.info("Успешно создан уровень прав доступа: {}, guid_employee={}", permissionName, authEmployee.getGuidEmployee());
            return new EmployeePermissionDTO(savedPermission.getIdEmployeePermission(), savedPermission.getPermission());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при создании уровня прав доступа: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при создании уровня прав доступа: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при создании уровня прав доступа. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Перегруженный метод для обратной совместимости.
     */
    @Transactional
    public EmployeePermissionDTO createPermission(EmployeePermissionDTO permissionDTO, Employee authEmployee) {
        return createPermission(permissionDTO, authEmployee, false);
    }

    /**
     * Обновление существующего уровня прав с проверкой уникальности и схожести названия.
     *
     * @param id            Идентификатор уровня прав.
     * @param permissionDTO DTO с обновлёнными данными.
     * @param authEmployee  Аутентифицированный сотрудник.
     * @param forceUpdate   Флаг подтверждения обновления при схожести.
     * @return DTO обновлённого уровня прав.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public EmployeePermissionDTO updatePermission(Integer id, EmployeePermissionDTO permissionDTO, Employee authEmployee, Boolean forceUpdate) {
        try {
            Optional<EmployeePermission> existing = permissionRepository.findById(id);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить уровень прав доступа: уровень не найден (id " + id + ")", LocalDateTime.now()));
                logger.error("Уровень прав доступа с id {} не найден, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Уровень прав доступа с указанным идентификатором не найден.");
            }

            String permissionName = permissionDTO.getPermission().trim();
            if (!permissionName.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить уровень прав доступа: название содержит недопустимые символы", LocalDateTime.now()));
                logger.error("Название уровня прав доступа содержит недопустимые символы: {}, guid_employee={}", permissionName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название уровня прав доступа может содержать только буквы, пробелы и дефисы. Цифры и специальные символы запрещены.");
            }

            if (permissionName.length() < 3 || permissionName.length() > 40) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить уровень прав доступа: длина названия не соответствует требованиям", LocalDateTime.now()));
                logger.error("Недопустимая длина названия уровня прав доступа: {}, guid_employee={}", permissionName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название уровня прав доступа должно быть от 3 до 40 символов.");
            }

            List<EmployeePermission> allPermissions = permissionRepository.findAll();
            for (EmployeePermission perm : allPermissions) {
                if (!perm.getIdEmployeePermission().equals(id)
                        && perm.getPermission().equalsIgnoreCase(permissionName)) {
                    logRepository.save(new Log(authEmployee, "Не удалось обновить уровень прав доступа: уровень с таким названием уже существует", LocalDateTime.now()));
                    logger.error("Уровень прав доступа '{}' уже существует, guid_employee={}", permissionName, authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException("Уровень прав доступа с таким названием уже существует. Пожалуйста, выберите другое название.");
                }
            }

            Optional<String> similarPermission = allPermissions.stream()
                    .filter(perm -> !perm.getIdEmployeePermission().equals(id))
                    .map(EmployeePermission::getPermission)
                    .filter(existingName -> stringSimilarity(existingName, permissionName) >= 0.85)
                    .findFirst();

            if (similarPermission.isPresent()) {
                if (Boolean.TRUE.equals(forceUpdate)) {
                    logRepository.save(new Log(authEmployee, "Обновлен уровень прав с похожим названием: " + permissionName + ", похоже на: " + similarPermission.get(), LocalDateTime.now()));
                    logger.info("Обновлен уровень прав с похожим названием: {}, guid_employee={}", permissionName, authEmployee.getGuidEmployee());
                } else {
                    logRepository.save(new Log(authEmployee,
                            "Попытка обновить уровень прав на похожее название: " + permissionName + ", похоже на: " + similarPermission.get(),
                            LocalDateTime.now()));
                    logger.warn("Попытка обновления уровня прав на похожее название: {}, похоже на: {}, guid_employee={}",
                            permissionName, similarPermission.get(), authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException(
                            String.format(
                                    "Обнаружено похожее название уровня прав: «%s». Если вы уверены, что хотите обновить уровень до этого названия.",
                                    similarPermission.get()
                            )
                    );
                }
            }

            EmployeePermission permission = existing.get();
            permission.setPermission(permissionName);
            EmployeePermission updatedPermission = permissionRepository.save(permission);
            logRepository.save(new Log(authEmployee, "Обновлен уровень прав доступа: " + permissionName, LocalDateTime.now()));
            logger.info("Успешно обновлен уровень прав доступа: {}, guid_employee={}", permissionName, authEmployee.getGuidEmployee());
            return new EmployeePermissionDTO(updatedPermission.getIdEmployeePermission(), updatedPermission.getPermission());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при обновлении уровня прав доступа: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при обновлении уровня прав доступа: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при обновлении уровня прав доступа. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Перегруженный метод обновления для обратной совместимости.
     */
    @Transactional
    public EmployeePermissionDTO updatePermission(Integer id, EmployeePermissionDTO permissionDTO, Employee authEmployee) {
        return updatePermission(id, permissionDTO, authEmployee, false);
    }

    /**
     * Удаление уровня прав доступа по идентификатору.
     * Проверяет, что уровень прав не используется в должностях сотрудников.
     *
     * @param id           Идентификатор уровня прав.
     * @param authEmployee Аутентифицированный сотрудник.
     * @throws IllegalArgumentException При ошибке удаления или если есть зависимые должности.
     */
    @Transactional
    public void deletePermission(Integer id, Employee authEmployee) {
        try {
            Optional<EmployeePermission> existing = permissionRepository.findById(id);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить уровень прав доступа: уровень не найден (id " + id + ")", LocalDateTime.now()));
                logger.error("Уровень прав доступа с id {} не найден, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Уровень прав доступа с указанным идентификатором не найден.");
            }

            EmployeePermission permission = existing.get();
            boolean isUsed = employeePostRepository.existsByEmployeePermission(permission);
            if (isUsed) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить уровень прав доступа: уровень используется в должностях (id " + id + ")", LocalDateTime.now()));
                logger.error("Уровень прав доступа с id {} используется в должностях, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Невозможно удалить уровень прав доступа, так как он используется в должностях.");
            }

            permissionRepository.deleteById(id);
            permissionRepository.flush();
            logRepository.save(new Log(authEmployee, "Удален уровень прав доступа с id: " + id, LocalDateTime.now()));
            logger.info("Успешно удален уровень прав доступа с id: {}, guid_employee={}", id, authEmployee.getGuidEmployee());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Нарушение целостности данных при удалении уровня прав с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось удалить уровень прав доступа из-за связей в базе данных. Проверьте, не используется ли уровень прав в должностях.", e);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при удалении уровня прав доступа: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при удалении уровня прав доступа с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при удалении уровня прав доступа. Пожалуйста, попробуйте позже.", e);
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