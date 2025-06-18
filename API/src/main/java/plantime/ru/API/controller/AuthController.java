package plantime.ru.API.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import plantime.ru.API.dto.*;
import plantime.ru.API.entity.*;
import plantime.ru.API.repository.LogRepository;
import plantime.ru.API.repository.SessionRepository;
import plantime.ru.API.service.AuthService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Контроллер для обработки операций аутентификации и авторизации.
 */
@RestController
@RequestMapping("/api/auth")
public class
AuthController {
    private final AuthService authService;
    private final LogRepository logRepository;
    private final SessionRepository sessionRepository;

    private static final Employee employee = new Employee(
            "00000000-0000-0000-0000-000000000000",
            "",
            "",
            "",
            "",
            "",
            null,
            "no_photo.jpg",
            null,
            LocalDateTime.of(1920, 1, 1, 0, 0),
            "",
            BigDecimal.ZERO,
            null,
            new EmployeePost(null, "Default", new EmployeePermission()),
            new EmployeeStatus(null, "Default"),
            new EmployeeDepartment(null, "Default"),
            new EmployeeGender(null, "Default")
    );

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * Конструктор для инициализации контроллера.
     *
     * @param authService       Сервис аутентификации для обработки операций входа и проверки токенов.
     * @param logRepository     Репозиторий для сохранения логов операций.
     * @param sessionRepository Репозиторий для управления сессиями пользователей.
     */
    public AuthController(AuthService authService, LogRepository logRepository, SessionRepository sessionRepository) {
        this.authService = authService;
        this.logRepository = logRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Шифрование предоставленного пароля.
     *
     * @param request Запрос, содержащий пароль для шифрования.
     * @return Ответ с зашифрованным паролем.
     * @throws IllegalArgumentException Если пароль отсутствует или невалиден.
     */
    @PostMapping("/hash-password")
    public ResponseEntity<HashPasswordResponse> hashPassword(@Valid @RequestBody HashPasswordRequest request) {
        HashPasswordResponse response = authService.hashPassword(request.getPassword());
        logRepository.save(new Log(employee, "Успешное шифрование пароля", LocalDateTime.now()));
        logger.info("Пароль успешно зашифрован");
        return ResponseEntity.ok(response);
    }

    /**
     * Выполняет вход пользователя в систему.
     *
     * @param request Запрос, содержащий логин и пароль пользователя.
     * @return Ответ с токеном авторизации.
     * @throws IllegalArgumentException Если логин или пароль отсутствуют или невалидны.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        Employee employeeFromLoginRequest = authService.getEmployeeFromLoginRequest(request);
        logRepository.save(new Log(employeeFromLoginRequest, "Успешная авторизация", LocalDateTime.now()));
        logger.info("Пользователь успешно авторизовался: guid_employee={}", employeeFromLoginRequest.getGuidEmployee());
        return ResponseEntity.ok(response);
    }

    /**
     * Проверяет токен авторизации и возвращает информацию о пользователе.
     * Проверяет токен авторизации и возвращает информацию о пользователе.
     *
     * @param authHeader Заголовок Authorization с токеном (формат: Bearer <токен>).
     * @return Ответ с информацией о пользователе, включая должность и права доступа.
     * @throws IllegalArgumentException Если токен отсутствует или невалиден.
     */
    @PostMapping("/verify")
    public ResponseEntity<VerifyResponse> verify(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.error("Верификация не удалась: отсутствует или некорректен заголовок Authorization");
            logRepository.save(new Log(employee, "Неуспешная верификация: отсутствует токен", LocalDateTime.now()));
            throw new IllegalArgumentException("Токен обязателен в заголовке Authorization с префиксом Bearer");
        }

        String token = authHeader.substring(7);
        try {
            VerifyResponse response = authService.verifyToken(token);
            Employee employeeFromToken = authService.getEmployeeFromToken(token);
            logRepository.save(new Log(employeeFromToken, "Успешная верификация токена", LocalDateTime.now()));
            logger.info("Токен успешно верифицирован: guid_employee={}", employeeFromToken.getGuidEmployee());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Верификация не удалась: {}", e.getMessage());
            logRepository.save(new Log(employee, "Неуспешная верификация: " + e.getMessage(), LocalDateTime.now()));
            throw e;
        }
    }

    /**
     * Завершает сессию пользователя, удаляя её из базы данных.
     *
     * @param authHeader Заголовок Authorization с токеном (формат: Bearer <токен>).
     * @return Ответ, подтверждающий успешное завершение сессии.
     * @throws IllegalArgumentException Если токен отсутствует, невалиден, сессия не найдена или пользователь неактивен.
     */
    @Transactional
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.error("Выход не удался: отсутствует или некорректен заголовок Authorization");
            logRepository.save(new Log(employee, "Неуспешный выход: отсутствует токен", LocalDateTime.now()));
            throw new IllegalArgumentException("Токен обязателен в заголовке Authorization с префиксом Bearer");
        }

        String token = authHeader.substring(7);
        try {
            Employee employeeFromToken = authService.getEmployeeFromToken(token); // Проверка валидности токена и статуса пользователя
            Optional<Session> sessionOpt = sessionRepository.findByToken(token);
            if (sessionOpt.isEmpty()) {
                logger.error("Выход не удался: сессия для токена не найдена");
                logRepository.save(new Log(employee, "Неуспешный выход: сессия не найдена", LocalDateTime.now()));
                throw new IllegalArgumentException("Сессия не найдена");
            }

            sessionRepository.deleteByEmployee(employeeFromToken);
            logRepository.save(new Log(employeeFromToken, "Успешный выход из системы", LocalDateTime.now()));
            logger.info("Пользователь успешно вышел из системы: guid_employee={}", employeeFromToken.getGuidEmployee());
            return ResponseEntity.ok("Успешный выход из системы");
        } catch (IllegalArgumentException e) {
            logger.error("Выход не удался: {}", e.getMessage());
            logRepository.save(new Log(employee, "Неуспешный выход: " + e.getMessage(), LocalDateTime.now()));
            throw e;
        }
    }

    /**
     * Обрабатывает запрос на сброс пароля.
     *
     * @param request Запрос, содержащий электронную почту для сброса пароля.
     * @return Ответ, подтверждающий успешный запрос сброса пароля.
     * @throws IllegalArgumentException Если электронная почта отсутствует или невалидна.
     * @throws IllegalStateException Если запрос не может быть выполнен из-за внутренних ошибок.
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<PasswordResetResponse> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        try {
            PasswordResetResponse response = authService.requestPasswordReset(request);
            logRepository.save(new Log(employee, "Успешный запрос сброса пароля для " + request.getEmail(), LocalDateTime.now()));
            logger.info("Запрос на сброс пароля выполнен для email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Запрос на сброс пароля не удался: {}", e.getMessage());
            logRepository.save(new Log(employee, "Неуспешный запрос сброса пароля: " + e.getMessage(), LocalDateTime.now()));
            throw e;
        }
    }

    /**
     * Подтверждает сброс пароля по предоставленному токену сброса.
     *
     * @param request Запрос, содержащий токен сброса пароля и новый пароль.
     * @return Ответ, подтверждающий успешный сброс пароля.
     * @throws IllegalArgumentException Если токен или новый пароль невалидны.
     */
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<PasswordResetResponse> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        try {
            PasswordResetResponse response = authService.confirmPasswordReset(request);
            logRepository.save(new Log(employee, "Успешный сброс пароля", LocalDateTime.now()));
            logger.info("Сброс пароля подтвержден для токена: {}", request.getToken());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Подтверждение сброса пароля не удалось: {}", e.getMessage());
            logRepository.save(new Log(employee, "Неуспешный сброс пароля: " + e.getMessage(), LocalDateTime.now()));
            throw e;
        }
    }
}