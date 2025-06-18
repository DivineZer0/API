package plantime.ru.API.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plantime.ru.API.dto.TypeAbsenceDTO;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.entity.Log;
import plantime.ru.API.entity.TypeAbsence;
import plantime.ru.API.repository.DutyScheduleRepository;
import plantime.ru.API.repository.TypeAbsenceRepository;
import plantime.ru.API.repository.LogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления типами отсутствия сотрудников.
 * Обеспечивает бизнес-логику для CRUD-операций, включая проверки уникальности,
 * схожести названий и ведение журналов действий.
 */
@Service
public class TypeAbsenceService {

    private final TypeAbsenceRepository typeAbsenceRepository;
    private final DutyScheduleRepository dutyScheduleRepository;
    private final LogRepository logRepository;
    private static final Logger logger = LoggerFactory.getLogger(TypeAbsenceService.class);

    public TypeAbsenceService(
            TypeAbsenceRepository typeAbsenceRepository,
            DutyScheduleRepository dutyScheduleRepository,
            LogRepository logRepository) {
        this.typeAbsenceRepository = typeAbsenceRepository;
        this.dutyScheduleRepository = dutyScheduleRepository;
        this.logRepository = logRepository;
    }

    /**
     * Получение списка всех типов отсутствия с сортировкой.
     *
     * @param authEmployee Аутентифицированный сотрудник.
     * @param sortBy       Поле сортировки.
     * @param order        Порядок сортировки.
     * @return Список DTO типов отсутствия.
     * @throws IllegalArgumentException Если параметры сортировки некорректны.
     */
    public List<TypeAbsenceDTO> getAllTypes(Employee authEmployee, String sortBy, String order) {
        try {
            if (!sortBy.equals("typeOfAbsence")) {
                logRepository.save(new Log(authEmployee, "Недопустимое поле сортировки: " + sortBy, LocalDateTime.now()));
                logger.error("Недопустимое поле сортировки: {}, guid_employee={}", sortBy, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Сортировка возможна только по полю «Название типа отсутствия».");
            }

            if (!order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc")) {
                logRepository.save(new Log(authEmployee, "Недопустимый порядок сортировки: " + order, LocalDateTime.now()));
                logger.error("Недопустимый порядок сортировки: {}, guid_employee={}", order, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Порядок сортировки должен быть «asc» (по возрастанию) или «desc» (по убыванию).");
            }

            Sort sort = order.equalsIgnoreCase("asc")
                    ? Sort.by(Sort.Direction.ASC, "typeOfAbsence")
                    : Sort.by(Sort.Direction.DESC, "typeOfAbsence");

            List<TypeAbsence> types = typeAbsenceRepository.findAll(sort);
            if (types.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Список типов отсутствия пуст", LocalDateTime.now()));
                logger.info("Список типов отсутствия пуст, guid_employee={}", authEmployee.getGuidEmployee());
                return List.of();
            }

            List<TypeAbsenceDTO> typeDTOs = types.stream()
                    .map(type -> new TypeAbsenceDTO(type.getIdTypeAbsence(), type.getTypeOfAbsence()))
                    .collect(Collectors.toList());
            logRepository.save(new Log(authEmployee, "Получен список типов отсутствия, количество: " + types.size() + ", сортировка: " + sortBy + ", порядок: " + order, LocalDateTime.now()));
            logger.info("Успешно получен список типов отсутствия, количество: {}, sortBy={}, order={}, guid_employee={}",
                    types.size(), sortBy, order, authEmployee.getGuidEmployee());
            return typeDTOs;
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при получении списка типов отсутствия: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при получении списка типов отсутствия: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при получении списка типов отсутствия. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Создание нового типа отсутствия с проверкой уникальности и схожести названия.
     *
     * @param typeAbsenceDTO DTO с данными типа отсутствия.
     * @param authEmployee   Аутентифицированный сотрудник.
     * @param forceCreate    Флаг подтверждения создания при схожести.
     * @return DTO созданного типа отсутствия.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public TypeAbsenceDTO createType(TypeAbsenceDTO typeAbsenceDTO, Employee authEmployee, Boolean forceCreate) {
        try {
            String typeName = typeAbsenceDTO.getTypeOfAbsence().trim();
            if (!typeName.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee, "Не удалось создать тип отсутствия: название содержит недопустимые символы", LocalDateTime.now()));
                logger.error("Название типа отсутствия содержит недопустимые символы: {}, guid_employee={}", typeName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название типа отсутствия может содержать только буквы, пробелы и дефисы. Цифры и специальные символы запрещены.");
            }

            if (typeName.length() < 3 || typeName.length() > 25) {
                logRepository.save(new Log(authEmployee, "Не удалось создать тип отсутствия: длина названия не соответствует требованиям", LocalDateTime.now()));
                logger.error("Недопустимая длина названия типа отсутствия: {}, guid_employee={}", typeName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название типа отсутствия должно быть от 3 до 25 символов.");
            }

            if (typeAbsenceRepository.existsByTypeOfAbsenceIgnoreCase(typeName)) {
                logRepository.save(new Log(authEmployee, "Не удалось создать тип отсутствия: тип с таким названием уже существует", LocalDateTime.now()));
                logger.error("Тип отсутствия '{}' уже существует, guid_employee={}", typeName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Тип отсутствия с таким названием уже существует. Пожалуйста, выберите другое название.");
            }

            List<TypeAbsence> allTypes = typeAbsenceRepository.findAll();
            Optional<String> similarType = allTypes.stream()
                    .map(TypeAbsence::getTypeOfAbsence)
                    .filter(existingName -> stringSimilarity(existingName, typeName) >= 0.85)
                    .findFirst();

            if (similarType.isPresent()) {
                if (Boolean.TRUE.equals(forceCreate)) {
                    logRepository.save(new Log(authEmployee, "Создан тип отсутствия с похожим названием: " + typeName + ", похож на: " + similarType.get(), LocalDateTime.now()));
                    logger.info("Создан тип отсутствия с похожим названием: {}, guid_employee={}", typeName, authEmployee.getGuidEmployee());
                } else {
                    logRepository.save(new Log(authEmployee,
                            "Попытка создать тип отсутствия с похожим названием: " + typeName + ", похоже на: " + similarType.get(),
                            LocalDateTime.now()));
                    logger.warn("Попытка создать тип отсутствия с похожим названием: {}, похоже на: {}, guid_employee={}",
                            typeName, similarType.get(), authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException(
                            String.format(
                                    "Обнаружено похожее название типа отсутствия: «%s». Если вы уверены, что хотите создать новый тип с этим названием.",
                                    similarType.get()
                            )
                    );
                }
            }

            TypeAbsence typeAbsence = new TypeAbsence();
            typeAbsence.setTypeOfAbsence(typeName);
            TypeAbsence savedType = typeAbsenceRepository.save(typeAbsence);
            logRepository.save(new Log(authEmployee, "Создан тип отсутствия: " + typeName, LocalDateTime.now()));
            logger.info("Успешно создан тип отсутствия: {}, guid_employee={}", typeName, authEmployee.getGuidEmployee());
            return new TypeAbsenceDTO(savedType.getIdTypeAbsence(), savedType.getTypeOfAbsence());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при создании типа отсутствия: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при создании типа отсутствия: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при создании типа отсутствия. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Перегруженный метод для обратной совместимости.
     */
    @Transactional
    public TypeAbsenceDTO createType(TypeAbsenceDTO typeAbsenceDTO, Employee authEmployee) {
        return createType(typeAbsenceDTO, authEmployee, false);
    }

    /**
     * Обновление существующего типа отсутствия с проверкой уникальности и схожести названия.
     *
     * @param id             Идентификатор типа отсутствия.
     * @param typeAbsenceDTO DTO с обновлёнными данными.
     * @param authEmployee   Аутентифицированный сотрудник.
     * @param forceUpdate    Флаг подтверждения обновления при схожести.
     * @return DTO обновлённого типа отсутствия.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public TypeAbsenceDTO updateType(Integer id, TypeAbsenceDTO typeAbsenceDTO, Employee authEmployee, Boolean forceUpdate) {
        try {
            Optional<TypeAbsence> existing = typeAbsenceRepository.findById(id);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить тип отсутствия: тип не найден (id " + id + ")", LocalDateTime.now()));
                logger.error("Тип отсутствия с id {} не найден, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Тип отсутствия с указанным идентификатором не найден.");
            }

            String typeName = typeAbsenceDTO.getTypeOfAbsence().trim();
            if (!typeName.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить тип отсутствия: название содержит недопустимые символы", LocalDateTime.now()));
                logger.error("Название типа отсутствия содержит недопустимые символы: {}, guid_employee={}", typeName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название типа отсутствия может содержать только буквы, пробелы и дефисы. Цифры и специальные символы запрещены.");
            }

            if (typeName.length() < 3 || typeName.length() > 25) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить тип отсутствия: длина названия не соответствует требованиям", LocalDateTime.now()));
                logger.error("Недопустимая длина названия типа отсутствия: {}, guid_employee={}", typeName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название типа отсутствия должно быть от 3 до 25 символов.");
            }

            List<TypeAbsence> allTypes = typeAbsenceRepository.findAll();
            for (TypeAbsence type : allTypes) {
                if (!type.getIdTypeAbsence().equals(id)
                        && type.getTypeOfAbsence().equalsIgnoreCase(typeName)) {
                    logRepository.save(new Log(authEmployee, "Не удалось обновить тип отсутствия: тип с таким названием уже существует", LocalDateTime.now()));
                    logger.error("Тип отсутствия '{}' уже существует, guid_employee={}", typeName, authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException("Тип отсутствия с таким названием уже существует. Пожалуйста, выберите другое название.");
                }
            }

            Optional<String> similarType = allTypes.stream()
                    .filter(type -> !type.getIdTypeAbsence().equals(id))
                    .map(TypeAbsence::getTypeOfAbsence)
                    .filter(existingName -> stringSimilarity(existingName, typeName) >= 0.85)
                    .findFirst();

            if (similarType.isPresent()) {
                if (Boolean.TRUE.equals(forceUpdate)) {
                    logRepository.save(new Log(authEmployee, "Обновлен тип отсутствия с похожим названием: " + typeName + ", похоже на: " + similarType.get(), LocalDateTime.now()));
                    logger.info("Обновлен тип отсутствия с похожим названием: {}, guid_employee={}", typeName, authEmployee.getGuidEmployee());
                } else {
                    logRepository.save(new Log(authEmployee,
                            "Попытка обновить тип отсутствия на похожее название: " + typeName + ", похоже на: " + similarType.get(),
                            LocalDateTime.now()));
                    logger.warn("Попытка обновления типа отсутствия на похожее название: {}, похоже на: {}, guid_employee={}",
                            typeName, similarType.get(), authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException(
                            String.format(
                                    "Обнаружено похожее название типа отсутствия: «%s». Если вы уверены, что хотите обновить тип до этого названия.",
                                    similarType.get()
                            )
                    );
                }
            }

            TypeAbsence typeAbsence = existing.get();
            typeAbsence.setTypeOfAbsence(typeName);
            TypeAbsence updatedType = typeAbsenceRepository.save(typeAbsence);
            logRepository.save(new Log(authEmployee, "Обновлен тип отсутствия: " + typeName, LocalDateTime.now()));
            logger.info("Успешно обновлен тип отсутствия: {}, guid_employee={}", typeName, authEmployee.getGuidEmployee());
            return new TypeAbsenceDTO(updatedType.getIdTypeAbsence(), updatedType.getTypeOfAbsence());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при обновлении типа отсутствия: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при обновлении типа отсутствия: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при обновлении типа отсутствия. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Перегруженный метод обновления для обратной совместимости.
     */
    @Transactional
    public TypeAbsenceDTO updateType(Integer id, TypeAbsenceDTO typeAbsenceDTO, Employee authEmployee) {
        return updateType(id, typeAbsenceDTO, authEmployee, false);
    }

    /**
     * Удаление типа отсутствия по идентификатору.
     * Проверяет, что тип отсутствия не используется в графиках.
     *
     * @param id           Идентификатор типа отсутствия.
     * @param authEmployee Аутентифицированный сотрудник.
     * @throws IllegalArgumentException При ошибке удаления или если есть связанные графики.
     */
    @Transactional
    public void deleteType(Integer id, Employee authEmployee) {
        try {
            Optional<TypeAbsence> existing = typeAbsenceRepository.findById(id);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить тип отсутствия: тип не найден (id " + id + ")", LocalDateTime.now()));
                logger.error("Тип отсутствия с id {} не найден, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Тип отсутствия с указанным идентификатором не найден.");
            }

            TypeAbsence typeAbsence = existing.get();
            boolean isUsed = dutyScheduleRepository.existsByTypeOfAbsence(typeAbsence);
            if (isUsed) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить тип отсутствия: тип используется в графиках (id " + id + ")", LocalDateTime.now()));
                logger.error("Тип отсутствия с id {} используется в графиках, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Невозможно удалить тип отсутствия, так как он используется в графиках отсутствия.");
            }

            typeAbsenceRepository.deleteById(id);
            typeAbsenceRepository.flush();
            logRepository.save(new Log(authEmployee, "Удален тип отсутствия с id: " + id, LocalDateTime.now()));
            logger.info("Успешно удален тип отсутствия с id: {}, guid_employee={}", id, authEmployee.getGuidEmployee());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Нарушение целостности данных при удалении типа отсутствия с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось удалить тип отсутствия из-за связей в базе данных. Проверьте, не используется ли тип отсутствия в графиках.", e);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при удалении типа отсутствия: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при удалении типа отсутствия с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при удалении типа отсутствия. Пожалуйста, попробуйте позже.", e);
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