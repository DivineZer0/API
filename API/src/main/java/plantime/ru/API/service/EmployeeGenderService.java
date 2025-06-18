package plantime.ru.API.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plantime.ru.API.dto.EmployeeGenderDTO;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.entity.EmployeeGender;
import plantime.ru.API.entity.Log;
import plantime.ru.API.repository.EmployeeGenderRepository;
import plantime.ru.API.repository.EmployeeRepository;
import plantime.ru.API.repository.LogRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для управления гендерами сотрудников.
 * Обеспечивает бизнес-логику для CRUD-операций с гендерами, включая проверки уникальности,
 * схожести названий и ведение журналов действий.
 */
@Service
public class EmployeeGenderService {

    private final EmployeeGenderRepository genderRepository;
    private final EmployeeRepository employeeRepository;
    private final LogRepository logRepository;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeGenderService.class);

    /**
     * Конструктор сервиса.
     *
     * @param genderRepository   Репозиторий гендеров.
     * @param employeeRepository Репозиторий сотрудников.
     * @param logRepository      Репозиторий логов.
     */
    public EmployeeGenderService(
            EmployeeGenderRepository genderRepository,
            EmployeeRepository employeeRepository,
            LogRepository logRepository) {
        this.genderRepository = genderRepository;
        this.employeeRepository = employeeRepository;
        this.logRepository = logRepository;
    }

    /**
     * Получение списка всех гендеров с сортировкой.
     *
     * @param authEmployee Аутентифицированный сотрудник.
     * @param sortBy       Поле сортировки.
     * @param order        Порядок сортировки.
     * @return Список DTO гендеров.
     * @throws IllegalArgumentException Если параметры сортировки некорректны.
     */
    public List<EmployeeGenderDTO> getAllGenders(Employee authEmployee, String sortBy, String order) {
        try {
            if (!sortBy.equals("gender")) {
                logRepository.save(new Log(authEmployee, "Недопустимое поле сортировки: " + sortBy, LocalDateTime.now()));
                logger.error("Недопустимое поле сортировки: {}, guid_employee={}", sortBy, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Сортировка возможна только по полю «Название гендера».");
            }

            if (!order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc")) {
                logRepository.save(new Log(authEmployee, "Недопустимый порядок сортировки: " + order, LocalDateTime.now()));
                logger.error("Недопустимый порядок сортировки: {}, guid_employee={}", order, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Порядок сортировки должен быть «asc» (по возрастанию) или «desc» (по убыванию).");
            }

            Sort sort = order.equalsIgnoreCase("asc")
                    ? Sort.by(Sort.Direction.ASC, "gender")
                    : Sort.by(Sort.Direction.DESC, "gender");
            List<EmployeeGender> genders = genderRepository.findAll(sort);

            if (genders.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Список гендеров пуст", LocalDateTime.now()));
                logger.info("Список гендеров пуст, guid_employee={}", authEmployee.getGuidEmployee());
                return List.of();
            }
            List<EmployeeGenderDTO> genderDTOs = genders.stream()
                    .map(gender -> new EmployeeGenderDTO(gender.getIdEmployeeGender(), gender.getGender()))
                    .collect(Collectors.toList());
            logRepository.save(new Log(authEmployee, "Получен список гендеров, количество: " + genders.size() + ", сортировка: " + sortBy + ", порядок: " + order, LocalDateTime.now()));
            logger.info("Успешно получен список гендеров, количество: {}, sortBy={}, order={}, guid_employee={}",
                    genders.size(), sortBy, order, authEmployee.getGuidEmployee());
            return genderDTOs;
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при получении списка гендеров: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при получении списка гендеров: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при получении списка гендеров. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Создание нового гендера с проверкой уникальности и схожести названия.
     *
     * @param genderDTO    DTO с данными гендера.
     * @param authEmployee Аутентифицированный сотрудник.
     * @param forceCreate  Флаг подтверждения создания при схожести.
     * @return DTO созданного гендера.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public EmployeeGenderDTO createGender(EmployeeGenderDTO genderDTO, Employee authEmployee, Boolean forceCreate) {
        try {
            String genderName = genderDTO.getGender().trim();
            if (!genderName.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee, "Не удалось создать гендер: название содержит недопустимые символы", LocalDateTime.now()));
                logger.error("Название гендера содержит недопустимые символы: {}, guid_employee={}", genderName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название гендера может содержать только буквы, пробелы и дефисы. Цифры и специальные символы запрещены.");
            }

            if (genderName.length() < 3 || genderName.length() > 10) {
                logRepository.save(new Log(authEmployee, "Не удалось создать гендер: длина названия не соответствует требованиям", LocalDateTime.now()));
                logger.error("Недопустимая длина названия гендера: {}, guid_employee={}", genderName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название гендера должно быть от 3 до 10 символов.");
            }

            if (genderRepository.existsByGenderIgnoreCase(genderName)) {
                logRepository.save(new Log(authEmployee, "Не удалось создать гендер: гендер с таким названием уже существует", LocalDateTime.now()));
                logger.error("Гендер '{}' уже существует, guid_employee={}", genderName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Гендер с таким названием уже существует. Пожалуйста, выберите другое название.");
            }

            List<EmployeeGender> allGenders = genderRepository.findAll();
            Optional<String> similarGender = allGenders.stream()
                    .map(EmployeeGender::getGender)
                    .filter(existingName -> stringSimilarity(existingName, genderName) >= 0.85)
                    .findFirst();

            if (similarGender.isPresent()) {
                if (Boolean.TRUE.equals(forceCreate)) {
                    logRepository.save(new Log(authEmployee, "Создан гендер с похожим названием: " + genderName + ", похож на: " + similarGender.get(), LocalDateTime.now()));
                    logger.info("Создан гендер с похожим названием: {}, guid_employee={}", genderName, authEmployee.getGuidEmployee());
                } else {
                    logRepository.save(new Log(authEmployee,
                            "Попытка создать гендер с похожим названием: " + genderName + ", похоже на: " + similarGender.get(),
                            LocalDateTime.now()));
                    logger.warn("Попытка создать гендер с похожим названием: {}, похоже на: {}, guid_employee={}",
                            genderName, similarGender.get(), authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException(
                            String.format(
                                    "Обнаружено похожее название гендера: «%s». Если вы уверены, что хотите создать новый гендер с этим названием.",
                                    similarGender.get()
                            )
                    );
                }
            }

            EmployeeGender gender = new EmployeeGender();
            gender.setGender(genderName);
            EmployeeGender savedGender = genderRepository.save(gender);
            logRepository.save(new Log(authEmployee, "Создан гендер: " + genderName, LocalDateTime.now()));
            logger.info("Успешно создан гендер: {}, guid_employee={}", genderName, authEmployee.getGuidEmployee());
            return new EmployeeGenderDTO(savedGender.getIdEmployeeGender(), savedGender.getGender());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при создании гендера: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при создании гендера: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при создании гендера. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Перегруженный метод для обратной совместимости.
     */
    @Transactional
    public EmployeeGenderDTO createGender(EmployeeGenderDTO genderDTO, Employee authEmployee) {
        return createGender(genderDTO, authEmployee, false);
    }

    /**
     * Обновление существующего гендера с проверкой уникальности и схожести названия.
     *
     * @param id           Идентификатор гендера.
     * @param genderDTO    DTO с обновленными данными.
     * @param authEmployee Аутентифицированный сотрудник.
     * @param forceUpdate  Флаг подтверждения обновления при схожести.
     * @return DTO обновленного гендера.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public EmployeeGenderDTO updateGender(Integer id, EmployeeGenderDTO genderDTO, Employee authEmployee, Boolean forceUpdate) {
        try {
            Optional<EmployeeGender> existing = genderRepository.findById(id);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить гендер: гендер не найден (id " + id + ")", LocalDateTime.now()));
                logger.error("Гендер с id {} не найден, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Гендер с указанным идентификатором не найден.");
            }

            String genderName = genderDTO.getGender().trim();
            if (!genderName.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить гендер: название содержит недопустимые символы", LocalDateTime.now()));
                logger.error("Название гендера содержит недопустимые символы: {}, guid_employee={}", genderName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название гендера может содержать только буквы, пробелы и дефисы. Цифры и специальные символы запрещены.");
            }

            if (genderName.length() < 3 || genderName.length() > 10) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить гендер: длина названия не соответствует требованиям", LocalDateTime.now()));
                logger.error("Недопустимая длина названия гендера: {}, guid_employee={}", genderName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название гендера должно быть от 3 до 10 символов.");
            }

            List<EmployeeGender> allGenders = genderRepository.findAll();
            for (EmployeeGender gender : allGenders) {
                if (!gender.getIdEmployeeGender().equals(id)
                        && gender.getGender().equalsIgnoreCase(genderName)) {
                    logRepository.save(new Log(authEmployee, "Не удалось обновить гендер: гендер с таким названием уже существует", LocalDateTime.now()));
                    logger.error("Гендер '{}' уже существует, guid_employee={}", genderName, authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException("Гендер с таким названием уже существует. Пожалуйста, выберите другое название.");
                }
            }

            Optional<String> similarGender = allGenders.stream()
                    .filter(gender -> !gender.getIdEmployeeGender().equals(id))
                    .map(EmployeeGender::getGender)
                    .filter(existingName -> stringSimilarity(existingName, genderName) >= 0.85)
                    .findFirst();

            if (similarGender.isPresent()) {
                if (Boolean.TRUE.equals(forceUpdate)) {
                    logRepository.save(new Log(authEmployee, "Обновлен гендер с похожим названием: " + genderName + ", похоже на: " + similarGender.get(), LocalDateTime.now()));
                    logger.info("Обновлен гендер с похожим названием: {}, guid_employee={}", genderName, authEmployee.getGuidEmployee());
                } else {
                    logRepository.save(new Log(authEmployee,
                            "Попытка обновить гендер на похожее название: " + genderName + ", похоже на: " + similarGender.get(),
                            LocalDateTime.now()));
                    logger.warn("Попытка обновления гендера на похожее название: {}, похоже на: {}, guid_employee={}",
                            genderName, similarGender.get(), authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException(
                            String.format(
                                    "Обнаружено похожее название гендера: «%s». Если вы уверены, что хотите обновить гендер до этого названия.",
                                    similarGender.get()
                            )
                    );
                }
            }

            EmployeeGender gender = existing.get();
            gender.setGender(genderName);
            EmployeeGender updatedGender = genderRepository.save(gender);
            logRepository.save(new Log(authEmployee, "Обновлен гендер: " + genderName, LocalDateTime.now()));
            logger.info("Успешно обновлен гендер: {}, guid_employee={}", genderName, authEmployee.getGuidEmployee());
            return new EmployeeGenderDTO(updatedGender.getIdEmployeeGender(), updatedGender.getGender());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при обновлении гендера: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при обновлении гендера: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при обновлении гендера. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Перегруженный метод обновления для обратной совместимости.
     */
    @Transactional
    public EmployeeGenderDTO updateGender(Integer id, EmployeeGenderDTO genderDTO, Employee authEmployee) {
        return updateGender(id, genderDTO, authEmployee, false);
    }

    /**
     * Удаление гендера по идентификатору.
     * Проверяет, что гендер не используется сотрудниками.
     *
     * @param id           Идентификатор гендера.
     * @param authEmployee Аутентифицированный сотрудник.
     * @throws IllegalArgumentException При ошибке удаления или если есть зависимые сотрудники.
     */
    @Transactional
    public void deleteGender(Integer id, Employee authEmployee) {
        try {
            Optional<EmployeeGender> existing = genderRepository.findById(id);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить гендер: гендер не найден (id " + id + ")", LocalDateTime.now()));
                logger.error("Гендер с id {} не найден, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Гендер с указанным идентификатором не найден.");
            }

            EmployeeGender gender = existing.get();
            boolean isUsed = employeeRepository.existsByEmployeeGender(gender);
            if (isUsed) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить гендер: гендер используется сотрудниками (id " + id + ")", LocalDateTime.now()));
                logger.error("Гендер с id {} используется сотрудниками, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Невозможно удалить гендер, так как он используется сотрудниками.");
            }

            genderRepository.deleteById(id);
            genderRepository.flush();
            logRepository.save(new Log(authEmployee, "Удален гендер с id: " + id, LocalDateTime.now()));
            logger.info("Успешно удален гендер с id: {}, guid_employee={}", id, authEmployee.getGuidEmployee());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Нарушение целостности данных при удалении гендера с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось удалить гендер из-за связей в базе данных. Проверьте, не используется ли гендер сотрудниками.", e);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при удалении гендера: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при удалении гендера с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при удалении гендера. Пожалуйста, попробуйте позже.", e);
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