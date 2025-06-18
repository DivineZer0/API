package plantime.ru.API.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plantime.ru.API.dto.EmployeeDepartmentDTO;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.entity.EmployeeDepartment;
import plantime.ru.API.entity.Log;
import plantime.ru.API.repository.EmployeeDepartmentRepository;
import plantime.ru.API.repository.EmployeeRepository;
import plantime.ru.API.repository.LogRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для управления отделами сотрудников.
 * Обеспечивает бизнес-логику для CRUD-операций с отделами, включая проверки уникальности,
 * схожести названий и ведение журналов действий.
 */
@Service
public class EmployeeDepartmentService {

    private final EmployeeDepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LogRepository logRepository;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeDepartmentService.class);

    /**
     * Конструктор сервиса.
     *
     * @param departmentRepository Репозиторий отделов.
     * @param employeeRepository   Репозиторий сотрудников.
     * @param logRepository        Репозиторий логов.
     */
    public EmployeeDepartmentService(
            EmployeeDepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository,
            LogRepository logRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.logRepository = logRepository;
    }

    /**
     * Получение списка всех отделов с сортировкой.
     *
     * @param authEmployee Аутентифицированный сотрудник.
     * @param sortBy       Поле сортировки.
     * @param order        Порядок сортировки.
     * @return Список DTO отделов.
     * @throws IllegalArgumentException Если параметры сортировки некорректны.
     */
    public List<EmployeeDepartmentDTO> getAllDepartments(Employee authEmployee, String sortBy, String order) {
        try {
            if (!sortBy.equals("department")) {
                logRepository.save(new Log(authEmployee, "Недопустимое поле сортировки: " + sortBy, LocalDateTime.now()));
                logger.error("Недопустимое поле сортировки: {}, guid_employee={}", sortBy, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Сортировка возможна только по полю «Название отдела».");
            }

            if (!order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc")) {
                logRepository.save(new Log(authEmployee, "Недопустимый порядок сортировки: " + order, LocalDateTime.now()));
                logger.error("Недопустимый порядок сортировки: {}, guid_employee={}", order, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Порядок сортировки должен быть «asc» (по возрастанию) или «desc» (по убыванию).");
            }

            Sort sort = order.equalsIgnoreCase("asc")
                    ? Sort.by(Sort.Direction.ASC, "department")
                    : Sort.by(Sort.Direction.DESC, "department");
            List<EmployeeDepartment> departments = departmentRepository.findAll(sort);
            if (departments.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Список отделов пуст", LocalDateTime.now()));
                logger.info("Список отделов пуст, guid_employee={}", authEmployee.getGuidEmployee());
                return List.of();
            }

            List<EmployeeDepartmentDTO> departmentDTOs = departments.stream()
                    .map(dept -> new EmployeeDepartmentDTO(dept.getIdEmployeeDepartment(), dept.getDepartment()))
                    .collect(Collectors.toList());
            logRepository.save(new Log(authEmployee, "Получен список отделов, количество: " + departments.size() + ", сортировка: " + sortBy + ", порядок: " + order, LocalDateTime.now()));
            logger.info("Успешно получен список отделов, количество: {}, sortBy={}, order={}, guid_employee={}",
                    departments.size(), sortBy, order, authEmployee.getGuidEmployee());
            return departmentDTOs;
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при получении списка отделов: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при получении списка отделов: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при получении списка отделов. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Создание нового отдела с проверкой уникальности и схожести названия.
     *
     * @param departmentDTO DTO с данными отдела.
     * @param authEmployee  Аутентифицированный сотрудник.
     * @param forceCreate   Флаг подтверждения создания при схожести.
     * @return DTO созданного отдела.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public EmployeeDepartmentDTO createDepartment(EmployeeDepartmentDTO departmentDTO, Employee authEmployee, Boolean forceCreate) {
        try {
            String departmentName = departmentDTO.getDepartment().trim();

            if (!departmentName.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee, "Не удалось создать отдел: название содержит недопустимые символы", LocalDateTime.now()));
                logger.error("Название отдела содержит недопустимые символы: {}, guid_employee={}", departmentName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название отдела может содержать только буквы, пробелы и дефисы. Цифры и специальные символы запрещены.");
            }

            if (departmentName.length() < 3 || departmentName.length() > 60) {
                logRepository.save(new Log(authEmployee, "Не удалось создать отдел: длина названия не соответствует требованиям", LocalDateTime.now()));
                logger.error("Недопустимая длина названия отдела: {}, guid_employee={}", departmentName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название отдела должно быть от 3 до 60 символов.");
            }

            if (departmentRepository.existsByDepartmentIgnoreCase(departmentName)) {
                logRepository.save(new Log(authEmployee, "Не удалось создать отдел: отдел с таким названием уже существует", LocalDateTime.now()));
                logger.error("Отдел '{}' уже существует, guid_employee={}", departmentName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Отдел с таким названием уже существует. Пожалуйста, выберите другое название.");
            }

            List<EmployeeDepartment> allDepartments = departmentRepository.findAll();
            Optional<String> similarDepartment = allDepartments.stream()
                    .map(EmployeeDepartment::getDepartment)
                    .filter(existingName -> stringSimilarity(existingName, departmentName) >= 0.85)
                    .findFirst();

            if (similarDepartment.isPresent()) {
                if (Boolean.TRUE.equals(forceCreate)) {
                    logRepository.save(new Log(authEmployee, "Создан отдел с похожим названием: " + departmentName + ", похож на: " + similarDepartment.get(), LocalDateTime.now()));
                    logger.info("Создан отдел с похожим названием: {}, guid_employee={}", departmentName, authEmployee.getGuidEmployee());
                } else {
                    logRepository.save(new Log(authEmployee,
                            "Попытка создать отдел с похожим названием: " + departmentName + ", похоже на: " + similarDepartment.get(),
                            LocalDateTime.now()));
                    logger.warn("Попытка создать отдел с похожим названием: {}, похоже на: {}, guid_employee={}",
                            departmentName, similarDepartment.get(), authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException(
                            String.format(
                                    "Обнаружено похожее название отдела: «%s». Если вы уверены, что хотите создать новый отдел с этим названием.",
                                    similarDepartment.get()
                            )
                    );
                }
            }

            EmployeeDepartment department = new EmployeeDepartment();
            department.setDepartment(departmentName);
            EmployeeDepartment savedDepartment = departmentRepository.save(department);
            logRepository.save(new Log(authEmployee, "Создан отдел: " + departmentName, LocalDateTime.now()));
            logger.info("Успешно создан отдел: {}, guid_employee={}", departmentName, authEmployee.getGuidEmployee());
            return new EmployeeDepartmentDTO(savedDepartment.getIdEmployeeDepartment(), savedDepartment.getDepartment());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при создании отдела: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при создании отдела: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при создании отдела. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Перегруженный метод для обратной совместимости.
     */
    @Transactional
    public EmployeeDepartmentDTO createDepartment(EmployeeDepartmentDTO departmentDTO, Employee authEmployee) {
        return createDepartment(departmentDTO, authEmployee, false);
    }

    /**
     * Обновление существующего отдела с проверкой уникальности и схожести названия.
     *
     * @param id            Идентификатор отдела.
     * @param departmentDTO DTO с обновленными данными.
     * @param authEmployee  Аутентифицированный сотрудник.
     * @param forceUpdate   Флаг подтверждения обновления при схожести.
     * @return DTO обновленного отдела.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public EmployeeDepartmentDTO updateDepartment(Integer id, EmployeeDepartmentDTO departmentDTO, Employee authEmployee, Boolean forceUpdate) {
        try {
            Optional<EmployeeDepartment> existing = departmentRepository.findById(id);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить отдел: отдел не найден (id " + id + ")", LocalDateTime.now()));
                logger.error("Отдел с id {} не найден, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Отдел с указанным идентификатором не найден.");
            }

            String departmentName = departmentDTO.getDepartment().trim();
            if (!departmentName.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить отдел: название содержит недопустимые символы", LocalDateTime.now()));
                logger.error("Название отдела содержит недопустимые символы: {}, guid_employee={}", departmentName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название отдела может содержать только буквы, пробелы и дефисы. Цифры и специальные символы запрещены.");
            }

            if (departmentName.length() < 3 || departmentName.length() > 60) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить отдел: длина названия не соответствует требованиям", LocalDateTime.now()));
                logger.error("Недопустимая длина названия отдела: {}, guid_employee={}", departmentName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название отдела должно быть от 3 до 60 символов.");
            }

            List<EmployeeDepartment> allDepartments = departmentRepository.findAll();
            for (EmployeeDepartment dept : allDepartments) {
                if (!dept.getIdEmployeeDepartment().equals(id)
                        && dept.getDepartment().equalsIgnoreCase(departmentName)) {
                    logRepository.save(new Log(authEmployee, "Не удалось обновить отдел: отдел с таким названием уже существует", LocalDateTime.now()));
                    logger.error("Отдел '{}' уже существует, guid_employee={}", departmentName, authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException("Отдел с таким названием уже существует. Пожалуйста, выберите другое название.");
                }
            }

            Optional<String> similarDepartment = allDepartments.stream()
                    .filter(dept -> !dept.getIdEmployeeDepartment().equals(id))
                    .map(EmployeeDepartment::getDepartment)
                    .filter(existingName -> stringSimilarity(existingName, departmentName) >= 0.85)
                    .findFirst();

            if (similarDepartment.isPresent()) {
                if (Boolean.TRUE.equals(forceUpdate)) {
                    logRepository.save(new Log(authEmployee, "Обновлен отдел с похожим названием: " + departmentName + ", похоже на: " + similarDepartment.get(), LocalDateTime.now()));
                    logger.info("Обновлен отдел с похожим названием: {}, guid_employee={}", departmentName, authEmployee.getGuidEmployee());
                } else {
                    logRepository.save(new Log(authEmployee,
                            "Попытка обновить отдел на похожее название: " + departmentName + ", похоже на: " + similarDepartment.get(),
                            LocalDateTime.now()));
                    logger.warn("Попытка обновления отдела на похожее название: {}, похоже на: {}, guid_employee={}",
                            departmentName, similarDepartment.get(), authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException(
                            String.format(
                                    "Обнаружено похожее название отдела: «%s». Если вы уверены, что хотите обновить отдел до этого названия.",
                                    similarDepartment.get()
                            )
                    );
                }
            }

            EmployeeDepartment department = existing.get();
            department.setDepartment(departmentName);
            EmployeeDepartment updatedDepartment = departmentRepository.save(department);
            logRepository.save(new Log(authEmployee, "Обновлен отдел: " + departmentName, LocalDateTime.now()));
            logger.info("Успешно обновлен отдел: {}, guid_employee={}", departmentName, authEmployee.getGuidEmployee());
            return new EmployeeDepartmentDTO(updatedDepartment.getIdEmployeeDepartment(), updatedDepartment.getDepartment());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при обновлении отдела: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при обновлении отдела: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при обновлении отдела. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Перегруженный метод обновления для обратной совместимости.
     */
    @Transactional
    public EmployeeDepartmentDTO updateDepartment(Integer id, EmployeeDepartmentDTO departmentDTO, Employee authEmployee) {
        return updateDepartment(id, departmentDTO, authEmployee, false);
    }

    /**
     * Удаление отдела по идентификатору.
     * Проверяет, что отдел не используется сотрудниками.
     *
     * @param id           Идентификатор отдела.
     * @param authEmployee Аутентифицированный сотрудник.
     * @throws IllegalArgumentException При ошибке удаления или если есть зависимые сотрудники.
     */
    @Transactional
    public void deleteDepartment(Integer id, Employee authEmployee) {
        try {
            Optional<EmployeeDepartment> existing = departmentRepository.findById(id);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить отдел: отдел не найден (id " + id + ")", LocalDateTime.now()));
                logger.error("Отдел с id {} не найден, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Отдел с указанным идентификатором не найден.");
            }

            EmployeeDepartment department = existing.get();
            boolean isUsed = employeeRepository.existsByEmployeeDepartment(department);
            if (isUsed) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить отдел: отдел используется сотрудниками (id " + id + ")", LocalDateTime.now()));
                logger.error("Отдел с id {} используется сотрудниками, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Невозможно удалить отдел, так как он используется сотрудниками.");
            }

            departmentRepository.deleteById(id);
            departmentRepository.flush();
            logRepository.save(new Log(authEmployee, "Удален отдел с id: " + id, LocalDateTime.now()));
            logger.info("Успешно удален отдел с id: {}, guid_employee={}", id, authEmployee.getGuidEmployee());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Нарушение целостности данных при удалении отдела с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось удалить отдел из-за связей в базе данных. Проверьте, не используется ли отдел сотрудниками.", e);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при удалении отдела: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при удалении отдела с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при удалении отдела. Пожалуйста, попробуйте позже.", e);
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