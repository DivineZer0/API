package plantime.ru.API.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plantime.ru.API.dto.EmployeePostDTO;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.entity.EmployeePermission;
import plantime.ru.API.entity.EmployeePost;
import plantime.ru.API.entity.Log;
import plantime.ru.API.repository.EmployeePermissionRepository;
import plantime.ru.API.repository.EmployeePostRepository;
import plantime.ru.API.repository.EmployeeRepository;
import plantime.ru.API.repository.LogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления должностями сотрудников.
 * Обеспечивает бизнес-логику для CRUD-операций, включая проверки уникальности,
 * схожести названий, фильтрацию и ведение журналов действий.
 */
@Service
public class EmployeePostService {

    private final EmployeePostRepository postRepository;
    private final EmployeePermissionRepository permissionRepository;
    private final EmployeeRepository employeeRepository;
    private final LogRepository logRepository;
    private static final Logger logger = LoggerFactory.getLogger(EmployeePostService.class);

    public EmployeePostService(
            EmployeePostRepository postRepository,
            EmployeePermissionRepository permissionRepository,
            EmployeeRepository employeeRepository,
            LogRepository logRepository) {
        this.postRepository = postRepository;
        this.permissionRepository = permissionRepository;
        this.employeeRepository = employeeRepository;
        this.logRepository = logRepository;
    }

    /**
     * Получает список всех должностей, отсортированных по указанному полю и порядку.
     * Если указан idEmployeePermission, возвращает только должности с этим уровнем прав доступа.
     *
     * @param authEmployee         Аутентифицированный сотрудник (для логирования).
     * @param idEmployeePermission Идентификатор уровня прав доступа для фильтрации (может быть null).
     * @param sortBy               Поле сортировки (например, "post").
     * @param order                Порядок сортировки: "asc" или "desc".
     * @return Список DTO должностей.
     * @throws IllegalArgumentException Если валидация не пройдена или уровень прав доступа не найден.
     */
    public List<EmployeePostDTO> getAllPosts(Employee authEmployee, Integer idEmployeePermission, String sortBy, String order) {
        try {
            if (!sortBy.equals("post")) {
                logRepository.save(new Log(authEmployee, "Недопустимое поле сортировки: " + sortBy, LocalDateTime.now()));
                logger.error("Недопустимое поле сортировки: {}, guid_employee={}", sortBy, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Сортировка возможна только по полю «Название должности».");
            }

            if (!order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc")) {
                logRepository.save(new Log(authEmployee, "Недопустимый порядок сортировки: " + order, LocalDateTime.now()));
                logger.error("Недопустимый порядок сортировки: {}, guid_employee={}", order, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Порядок сортировки должен быть «asc» (по возрастанию) или «desc» (по убыванию).");
            }

            Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(Sort.Direction.ASC, "post") : Sort.by(Sort.Direction.DESC, "post");
            List<EmployeePost> posts;
            String logMessage;
            if (idEmployeePermission != null) {
                if (!permissionRepository.existsById(idEmployeePermission)) {
                    logRepository.save(new Log(authEmployee, "Не удалось получить должности: уровень прав доступа с id " + idEmployeePermission + " не найден", LocalDateTime.now()));
                    logger.error("Уровень прав доступа с id {} не найден, guid_employee={}", idEmployeePermission, authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException("Уровень прав доступа с id " + idEmployeePermission + " не найден");
                }
                posts = postRepository.findByEmployeePermissionIdEmployeePermission(idEmployeePermission, sort);
                logMessage = "Получен список должностей для уровня прав доступа id " + idEmployeePermission + ", количество: " + posts.size();
            } else {
                posts = postRepository.findAll(sort);
                logMessage = "Получен список всех должностей, количество: " + posts.size();
            }

            if (posts.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Список должностей пуст", LocalDateTime.now()));
                logger.info("Список должностей пуст, guid_employee={}", authEmployee.getGuidEmployee());
                return List.of();
            }

            List<EmployeePostDTO> postDTOs = posts.stream()
                    .map(post -> new EmployeePostDTO(
                            post.getIdEmployeePost(),
                            post.getPost(),
                            post.getEmployeePermission().getIdEmployeePermission()))
                    .collect(Collectors.toList());
            logRepository.save(new Log(authEmployee, logMessage + ", сортировка: " + sortBy + ", порядок: " + order, LocalDateTime.now()));
            logger.info("{}, sortBy={}, order={}, guid_employee={}", logMessage, sortBy, order, authEmployee.getGuidEmployee());
            return postDTOs;
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при получении списка должностей: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при получении списка должностей: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при получении списка должностей. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Создаёт новую должность с проверкой уникальности и схожести названия.
     *
     * @param postDTO      DTO с данными должности.
     * @param authEmployee Аутентифицированный сотрудник.
     * @param forceCreate  Флаг подтверждения создания при схожести.
     * @return DTO созданной должности.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public EmployeePostDTO createPost(EmployeePostDTO postDTO, Employee authEmployee, Boolean forceCreate) {
        try {
            String postName = postDTO.getPost().trim();
            if (!postName.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee, "Не удалось создать должность: название содержит недопустимые символы", LocalDateTime.now()));
                logger.error("Название должности содержит недопустимые символы: {}, guid_employee={}", postName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название должности может содержать только буквы, пробелы и дефисы. Цифры и специальные символы запрещены.");
            }

            if (postName.length() > 40) {
                logRepository.save(new Log(authEmployee, "Не удалось создать должность: длина названия превышает 40 символов", LocalDateTime.now()));
                logger.error("Недопустимая длина названия должности: {}, guid_employee={}", postName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название должности не должно превышать 40 символов.");
            }

            if (postRepository.existsByPostIgnoreCase(postName)) {
                logRepository.save(new Log(authEmployee, "Не удалось создать должность: должность с таким названием уже существует", LocalDateTime.now()));
                logger.error("Должность '{}' уже существует, guid_employee={}", postName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Должность с таким названием уже существует. Пожалуйста, выберите другое название.");
            }

            List<EmployeePost> allPosts = postRepository.findAll();
            Optional<String> similarPost = allPosts.stream()
                    .map(EmployeePost::getPost)
                    .filter(existingName -> stringSimilarity(existingName, postName) >= 0.85)
                    .findFirst();

            if (similarPost.isPresent()) {
                if (Boolean.TRUE.equals(forceCreate)) {
                    logRepository.save(new Log(authEmployee, "Создана должность с похожим названием: " + postName + ", похоже на: " + similarPost.get(), LocalDateTime.now()));
                    logger.info("Создана должность с похожим названием: {}, guid_employee={}", postName, authEmployee.getGuidEmployee());
                } else {
                    logRepository.save(new Log(authEmployee,
                            "Попытка создать должность с похожим названием: " + postName + ", похоже на: " + similarPost.get(),
                            LocalDateTime.now()));
                    logger.warn("Попытка создать должность с похожим названием: {}, похоже на: {}, guid_employee={}",
                            postName, similarPost.get(), authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException(
                            String.format(
                                    "Обнаружено похожее название должности: «%s». Если вы уверены, что хотите создать новую должность с этим названием.",
                                    similarPost.get()
                            )
                    );
                }
            }

            Optional<EmployeePermission> permission = permissionRepository.findById(postDTO.getIdEmployeePermission());
            if (permission.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось создать должность: уровень прав доступа с id " + postDTO.getIdEmployeePermission() + " не найден", LocalDateTime.now()));
                logger.error("Уровень прав доступа с id {} не найден, guid_employee={}", postDTO.getIdEmployeePermission(), authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Уровень прав доступа с id " + postDTO.getIdEmployeePermission() + " не найден");
            }
            EmployeePost post = new EmployeePost();
            post.setPost(postName);
            post.setEmployeePermission(permission.get());
            EmployeePost savedPost = postRepository.save(post);
            logRepository.save(new Log(authEmployee, "Создана должность: " + postName, LocalDateTime.now()));
            logger.info("Успешно создана должность: {}, guid_employee={}", postName, authEmployee.getGuidEmployee());
            return new EmployeePostDTO(savedPost.getIdEmployeePost(), savedPost.getPost(), savedPost.getEmployeePermission().getIdEmployeePermission());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при создании должности: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при создании должности: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при создании должности. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Перегруженный метод для обратной совместимости.
     */
    @Transactional
    public EmployeePostDTO createPost(EmployeePostDTO postDTO, Employee authEmployee) {
        return createPost(postDTO, authEmployee, false);
    }

    /**
     * Обновляет существующую должность с проверкой уникальности и схожести названия.
     *
     * @param id           Идентификатор должности.
     * @param postDTO      DTO с обновлёнными данными должности.
     * @param authEmployee Аутентифицированный сотрудник.
     * @param forceUpdate  Флаг подтверждения обновления при схожести.
     * @return DTO обновлённой должности.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public EmployeePostDTO updatePost(Integer id, EmployeePostDTO postDTO, Employee authEmployee, Boolean forceUpdate) {
        try {
            Optional<EmployeePost> existing = postRepository.findById(id);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить должность: должность с id " + id + " не найдена", LocalDateTime.now()));
                logger.error("Должность с id {} не найдена, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Должность с id " + id + " не найдена");
            }

            String postName = postDTO.getPost().trim();
            if (!postName.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]*$")) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить должность: название содержит недопустимые символы", LocalDateTime.now()));
                logger.error("Название должности содержит недопустимые символы: {}, guid_employee={}", postName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название должности может содержать только буквы, пробелы и дефисы. Цифры и специальные символы запрещены.");
            }

            if (postName.length() > 40) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить должность: длина названия превышает 40 символов", LocalDateTime.now()));
                logger.error("Недопустимая длина названия должности: {}, guid_employee={}", postName, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Название должности не должно превышать 40 символов.");
            }

            List<EmployeePost> allPosts = postRepository.findAll();
            for (EmployeePost post : allPosts) {
                if (!post.getIdEmployeePost().equals(id)
                        && post.getPost().equalsIgnoreCase(postName)) {
                    logRepository.save(new Log(authEmployee, "Не удалось обновить должность: должность с таким названием уже существует", LocalDateTime.now()));
                    logger.error("Должность '{}' уже существует, guid_employee={}", postName, authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException("Должность с таким названием уже существует. Пожалуйста, выберите другое название.");
                }
            }

            Optional<String> similarPost = allPosts.stream()
                    .filter(post -> !post.getIdEmployeePost().equals(id))
                    .map(EmployeePost::getPost)
                    .filter(existingName -> stringSimilarity(existingName, postName) >= 0.85)
                    .findFirst();

            if (similarPost.isPresent()) {
                if (Boolean.TRUE.equals(forceUpdate)) {
                    logRepository.save(new Log(authEmployee, "Обновлена должность на похожее название: " + postName + ", похоже на: " + similarPost.get(), LocalDateTime.now()));
                    logger.info("Обновлена должность на похожее название: {}, guid_employee={}", postName, authEmployee.getGuidEmployee());
                } else {
                    logRepository.save(new Log(authEmployee,
                            "Попытка обновить должность на похожее название: " + postName + ", похоже на: " + similarPost.get(),
                            LocalDateTime.now()));
                    logger.warn("Попытка обновления должности на похожее название: {}, похоже на: {}, guid_employee={}",
                            postName, similarPost.get(), authEmployee.getGuidEmployee());
                    throw new IllegalArgumentException(
                            String.format(
                                    "Обнаружено похожее название должности: «%s». Если вы уверены, что хотите обновить должность до этого названия.",
                                    similarPost.get()
                            )
                    );
                }
            }

            Optional<EmployeePermission> permission = permissionRepository.findById(postDTO.getIdEmployeePermission());
            if (permission.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить должность: уровень прав доступа с id " + postDTO.getIdEmployeePermission() + " не найден", LocalDateTime.now()));
                logger.error("Уровень прав доступа с id {} не найден, guid_employee={}", postDTO.getIdEmployeePermission(), authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Уровень прав доступа с id " + postDTO.getIdEmployeePermission() + " не найден");
            }
            EmployeePost post = existing.get();
            post.setPost(postName);
            post.setEmployeePermission(permission.get());
            EmployeePost updatedPost = postRepository.save(post);
            logRepository.save(new Log(authEmployee, "Обновлена должность: " + postName, LocalDateTime.now()));
            logger.info("Успешно обновлена должность: {}, guid_employee={}", postName, authEmployee.getGuidEmployee());
            return new EmployeePostDTO(updatedPost.getIdEmployeePost(), updatedPost.getPost(), updatedPost.getEmployeePermission().getIdEmployeePermission());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при обновлении должности: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при обновлении должности: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при обновлении должности. Пожалуйста, попробуйте позже.", e);
        }
    }

    /**
     * Перегруженный метод обновления для обратной совместимости.
     */
    @Transactional
    public EmployeePostDTO updatePost(Integer id, EmployeePostDTO postDTO, Employee authEmployee) {
        return updatePost(id, postDTO, authEmployee, false);
    }

    /**
     * Удаляет должность по идентификатору.
     * Проверяет, что должность не используется сотрудниками.
     *
     * @param id           Идентификатор должности.
     * @param authEmployee Аутентифицированный сотрудник.
     * @throws IllegalArgumentException При ошибке удаления или наличии связей.
     */
    @Transactional
    public void deletePost(Integer id, Employee authEmployee) {
        try {
            Optional<EmployeePost> existing = postRepository.findById(id);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить должность: должность с id " + id + " не найдена", LocalDateTime.now()));
                logger.error("Должность с id {} не найдена, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Должность с указанным идентификатором не найдена.");
            }

            EmployeePost post = existing.get();
            boolean isUsed = employeeRepository.existsByEmployeePost(post);
            if (isUsed) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить должность: должность с id " + id + " используется сотрудниками", LocalDateTime.now()));
                logger.error("Должность с id {} используется сотрудниками, guid_employee={}", id, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Должность с id " + id + " не может быть удалена, так как используется сотрудниками.");
            }

            postRepository.deleteById(id);
            postRepository.flush();
            logRepository.save(new Log(authEmployee, "Удалена должность с id: " + id, LocalDateTime.now()));
            logger.info("Успешно удалена должность с id: {}, guid_employee={}", id, authEmployee.getGuidEmployee());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Нарушение целостности данных при удалении должности с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось удалить должность из-за связей в базе данных. Проверьте, не используется ли должность в сотрудниках.", e);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при удалении должности: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при удалении должности с id {}: {}, guid_employee={}", id, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Произошла непредвиденная ошибка при удалении должности. Пожалуйста, попробуйте позже.", e);
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