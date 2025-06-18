package plantime.ru.API.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plantime.ru.API.dto.*;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.entity.Session;
import plantime.ru.API.entity.PasswordResetToken;
import plantime.ru.API.repository.EmployeeRepository;
import plantime.ru.API.repository.PasswordResetTokenRepository;
import plantime.ru.API.repository.SessionRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import java.security.SecureRandom;

/**
 * Сервис для обработки операций аутентификации и авторизации.
 */
@Service
public class AuthService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;
    private final SessionRepository sessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /**
     * Секретный ключ для подписи JWT-токенов, загружаемый из конфигурации.
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Время действия JWT-токена в миллисекундах (по умолчанию 24 часа).
     */
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    /**
     * Адрес отправителя для писем, загружаемый из конфигурации.
     */
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Конструктор для инициализации сервиса аутентификации.
     *
     * @param employeeRepository        Репозиторий для доступа к данным сотрудников.
     * @param sessionRepository         Репозиторий для управления сессиями пользователей.
     * @param passwordResetTokenRepository Репозиторий для управления токенами сброса пароля.
     * @param mailSender                Сервис для отправки электронных писем.
     */
    public AuthService(EmployeeRepository employeeRepository, SessionRepository sessionRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository, JavaMailSender mailSender) {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.employeeRepository = employeeRepository;
        this.sessionRepository = sessionRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.mailSender = mailSender;
    }

    /**
     * Хеширует предоставленный пароль с использованием BCrypt.
     *
     * @param password Пароль в открытом виде. Должен быть длиной от 8 до 50 символов,
     *                 содержать минимум одну букву, одну цифру и один специальный символ.
     * @return Объект с хешированным паролем.
     * @throws IllegalArgumentException Если пароль пустой, слишком короткий или не соответствует требованиям.
     */
    public HashPasswordResponse hashPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            logger.error("Хеширование пароля не удалось: пароль пустой");
            throw new IllegalArgumentException("Поле пароля не может быть пустым");
        }
        if (password.length() < 8 || password.length() > 50) {
            logger.error("Хеширование пароля не удалось: длина пароля {} символов", password.length());
            throw new IllegalArgumentException("Пароль должен содержать от 8 до 50 символов");
        }
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&._,])[A-Za-z\\d@$!%*#?&._,]{8,50}$")) {
            logger.error("Хеширование пароля не удалось: пароль не соответствует требованиям");
            throw new IllegalArgumentException(
                    "Пароль должен содержать минимум одну букву, одну цифру и один специальный символ");
        }
        String hashedPassword = passwordEncoder.encode(password);
        logger.info("Пароль успешно хеширован");
        return new HashPasswordResponse(hashedPassword);
    }

    /**
     * Выполняет вход пользователя в систему, создавая JWT-токен и сессию.
     *
     * @param request Запрос с логином (или email) и паролем.
     * @return Объект с JWT-токеном авторизации.
     * @throws IllegalArgumentException Если логин или пароль пустые, неверные, или учетная запись неактивна.
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        if (request == null || request.getLogin() == null || request.getLogin().isEmpty()) {
            logger.error("Вход не удался: логин или email отсутствует");
            throw new IllegalArgumentException("Логин или email обязателен");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            logger.error("Вход не удался: пароль отсутствует");
            throw new IllegalArgumentException("Пароль обязателен");
        }

        Optional<Employee> employeeOpt = employeeRepository.findByLoginOrEmail(request.getLogin(), request.getLogin());
        if (employeeOpt.isEmpty()) {
            logger.error("Вход не удался: пользователь не найден для логина {}", request.getLogin());
            throw new IllegalArgumentException("Неверный логин или пароль");
        }

        Employee employee = employeeOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            logger.error("Вход не удался: неверный пароль для логина {}", request.getLogin());
            throw new IllegalArgumentException("Неверный логин или пароль");
        }

        if (employee.getEmployeeStatus() == null || "Неактивен".equals(employee.getEmployeeStatus().getStatus())) {
            logger.error("Вход не удался: учетная запись неактивна для логина {}", request.getLogin());
            throw new IllegalArgumentException("Учетная запись неактивна");
        }

        String guidEmployee = employee.getGuidEmployee();
        if (guidEmployee == null || guidEmployee.length() != 36 ||
                !guidEmployee.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            logger.error("Вход не удался: недействительный guid_employee для логина {}", request.getLogin());
            throw new IllegalArgumentException("Недействительный идентификатор сотрудника для пользователя: " + request.getLogin());
        }

        employee.setLastAuthorization(LocalDateTime.now());
        employeeRepository.save(employee);

        sessionRepository.deleteByEmployee(employee);
        String token = generateJwtToken(guidEmployee);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtExpiration / 1000);
        Session session = new Session(token, expiresAt, employee);
        sessionRepository.save(session);

        logger.info("Пользователь успешно вошел в систему: guid_employee={}", guidEmployee);
        return new LoginResponse(token, expiresAt, employee.getEmployeePost().getEmployeePermission().getPermission(), employee.getProfilePicture());
    }

    /**
     * Извлекает сотрудника из запроса на вход.
     *
     * @param request Запрос с логином (или email).
     * @return Объект сотрудника.
     * @throws IllegalArgumentException Если логин пустой, пользователь не найден или идентификатор сотрудника невалиден.
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeFromLoginRequest(LoginRequest request) {
        if (request == null || request.getLogin() == null || request.getLogin().isEmpty()) {
            logger.error("Извлечение сотрудника не удалось: логин или email отсутствует");
            throw new IllegalArgumentException("Логин или email обязателен");
        }

        Optional<Employee> employeeOpt = employeeRepository.findByLoginOrEmail(request.getLogin(), request.getLogin());
        if (employeeOpt.isEmpty()) {
            logger.error("Извлечение сотрудника не удалось: пользователь не найден для логина {}", request.getLogin());
            throw new IllegalArgumentException("Пользователь не найден: " + request.getLogin());
        }

        Employee employee = employeeOpt.get();
        String guidEmployee = employee.getGuidEmployee();

        if (guidEmployee == null || guidEmployee.length() != 36 ||
                !guidEmployee.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            logger.error("Извлечение сотрудника не удалось: недействительный guid_employee для логина {}", request.getLogin());
            throw new IllegalArgumentException("Недействительный идентификатор сотрудника для пользователя: " + request.getLogin());
        }

        logger.info("Сотрудник успешно извлечен: guid_employee={}", guidEmployee);
        return employee;
    }

    /**
     * Извлекает сотрудника из JWT-токена.
     *
     * @param token JWT-токен.
     * @return Объект сотрудника.
     * @throws IllegalArgumentException Если токен пустой, невалиден, истек или пользователь не найден.
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.error("Извлечение сотрудника из токена не удалось: токен пустой");
            throw new IllegalArgumentException("Токен обязателен");
        }

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String guidEmployee = claims.getSubject();
            if (guidEmployee == null || guidEmployee.length() != 36 ||
                    !guidEmployee.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
                logger.error("Извлечение сотрудника из токена не удалось: недействительный guid_employee");
                throw new IllegalArgumentException("Недействительный идентификатор сотрудника в токене");
            }

            Optional<Session> sessionOpt = sessionRepository.findByToken(token);
            if (sessionOpt.isEmpty() || sessionOpt.get().getExpiresAt().isBefore(LocalDateTime.now())) {
                logger.error("Извлечение сотрудника из токена не удалось: токен недействителен или истек");
                throw new IllegalArgumentException("Токен недействителен или истек");
            }

            Optional<Employee> employeeOpt = employeeRepository.findByGuidEmployee(guidEmployee);
            if (employeeOpt.isEmpty()) {
                logger.error("Извлечение сотрудника из токена не удалось: пользователь не найден для guid_employee {}", guidEmployee);
                throw new IllegalArgumentException("Пользователь не найден для идентификатора: " + guidEmployee);
            }

            Employee employee = employeeOpt.get();
            if (employee.getEmployeeStatus() == null || "Неактивен".equals(employee.getEmployeeStatus().getStatus())) {
                logger.error("Извлечение сотрудника из токена не удалось: учетная запись неактивна для guid_employee {}", guidEmployee);
                throw new IllegalArgumentException("Учетная запись неактивна");
            }
            logger.info("Сотрудник успешно извлечен из токена: guid_employee={}", guidEmployee);
            return employee;
        } catch (ExpiredJwtException e) {
            logger.error("Извлечение сотрудника из токена не удалось: токен истек");
            throw new IllegalArgumentException("Токен истек");
        } catch (JwtException e) {
            logger.error("Извлечение сотрудника из токена не удалось: недействительный токен");
            throw new IllegalArgumentException("Недействительный токен");
        }
    }

    /**
     * Проверяет JWT-токен и возвращает информацию о сотруднике.
     *
     * @param token JWT-токен.
     * @return Объект с информацией о сотруднике (фамилия, имя, отчество, роль, фото, статус).
     * @throws IllegalArgumentException Если токен пустой, невалиден, истек, пользователь не найден или статус не определен.
     */
    @Transactional(readOnly = true)
    public VerifyResponse verifyToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.error("Верификация токена не удалась: токен пустой");
            throw new IllegalArgumentException("Токен обязателен");
        }

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String guidEmployee = claims.getSubject();
            if (guidEmployee == null || guidEmployee.length() != 36 ||
                    !guidEmployee.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
                logger.error("Верификация токена не удалась: недействительный guid_employee");
                throw new IllegalArgumentException("Недействительный идентификатор сотрудника в токене");
            }

            Optional<Session> sessionOpt = sessionRepository.findByToken(token);
            if (sessionOpt.isEmpty() || sessionOpt.get().getExpiresAt().isBefore(LocalDateTime.now())) {
                logger.error("Верификация токена не удалась: токен недействителен или истек");
                throw new IllegalArgumentException("Токен недействителен или истек");
            }

            Optional<Employee> employeeOpt = employeeRepository.findByGuidEmployee(guidEmployee);
            if (employeeOpt.isEmpty()) {
                logger.error("Верификация токена не удалась: пользователь не найден для guid_employee {}", guidEmployee);
                throw new IllegalArgumentException("Пользователь не найден для идентификатора: " + guidEmployee);
            }

            Employee employee = employeeOpt.get();
            if (employee.getEmployeeStatus() == null || "Неактивен".equals(employee.getEmployeeStatus().getStatus())) {
                logger.error("Верификация токена не удалась: учетная запись неактивна для guid_employee {}", guidEmployee);
                throw new IllegalArgumentException("Учетная запись неактивна");
            }

            String post = employee.getEmployeePost() != null ? employee.getEmployeePost().getPost() : null;
            String permission = employee.getEmployeePost() != null && employee.getEmployeePost().getEmployeePermission() != null
                    ? employee.getEmployeePost().getEmployeePermission().getPermission()
                    : null;
            String status = employee.getEmployeeStatus() != null ? employee.getEmployeeStatus().getStatus() : null;

            if (status == null) {
                logger.error("Верификация токена не удалась: статус сотрудника не определен для guid_employee {}", guidEmployee);
                throw new IllegalArgumentException("Статус сотрудника не определен");
            }

            logger.info("Токен успешно верифицирован: guid_employee={}", guidEmployee);
            return new VerifyResponse(
                    employee.getSurname(),
                    employee.getFirstName(),
                    employee.getPatronymic(),
                    permission,
                    employee.getProfilePicture(),
                    status
            );
        } catch (ExpiredJwtException e) {
            logger.error("Верификация токена не удалась: токен истек");
            throw new IllegalArgumentException("Токен истек");
        } catch (JwtException e) {
            logger.error("Верификация токена не удалась: недействительный токен");
            throw new IllegalArgumentException("Недействительный токен");
        }
    }

    /**
     * Генерирует JWT-токен для сотрудника.
     *
     * @param guidEmployee Уникальный идентификатор сотрудника в формате UUID.
     * @return Сгенерированный JWT-токен.
     */
    private String generateJwtToken(String guidEmployee) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        SecretKey key = Keys
                .hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .setSubject(guidEmployee)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
        logger.info("JWT-токен сгенерирован для guid_employee={}", guidEmployee);
        return token;
    }

    /**
     * Обрабатывает запрос на сброс пароля, генерируя код и отправляя его на email.
     *
     * @param request Запрос с email пользователя. Email обязателен.
     * @return Ответ с подтверждением отправки кода.
     * @throws IllegalArgumentException Если email пустой, пользователь не найден или учетная запись неактивна.
     * @throws IllegalStateException Если не удалось отправить письмо.
     */
    @Transactional
    public PasswordResetResponse requestPasswordReset(PasswordResetRequest request) {
        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            logger.error("Запрос на сброс пароля не удался: email пустой");
            throw new IllegalArgumentException("Email обязателен");
        }

        Optional<Employee> employeeOpt = employeeRepository.findByLoginOrEmail(null, email);
        if (employeeOpt.isEmpty()) {
            logger.error("Запрос на сброс пароля не удался: пользователь не найден для email {}", email);
            throw new IllegalArgumentException("Пользователь с таким email не найден");
        }

        Employee employee = employeeOpt.get();
        if (employee.getEmployeeStatus() == null || "Неактивен".equals(employee.getEmployeeStatus().getStatus())) {
            logger.error("Запрос на сброс пароля не удался: учетная запись неактивна для email {}", email);
            throw new IllegalArgumentException("Учетная запись неактивна");
        }

        String code = generateSixDigitCode();
        passwordResetTokenRepository.deleteByEmployee(employee);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
        PasswordResetToken token = new PasswordResetToken(code, employee, expiresAt);
        passwordResetTokenRepository.save(token);

        try {
            sendPasswordResetEmail(email, code, employee.getFirstName());
            logger.info("Код для сброса пароля отправлен на {}", email);
            return new PasswordResetResponse("Код для сброса пароля отправлен на " + email);
        } catch (MessagingException e) {
            logger.error("Не удалось отправить письмо для сброса пароля на {}: {}", email, e.getMessage());
            throw new IllegalStateException("Ошибка при отправке письма для сброса пароля");
        }
    }

    /**
     * Подтверждает сброс пароля, обновляя пароль сотрудника.
     *
     * @param request Запрос с кодом сброса и новым паролем. Оба поля обязательны.
     * @return Ответ с подтверждением успешного сброса пароля.
     * @throws IllegalArgumentException Если код или пароль пустые, код недействителен, истек или пользователь не найден.
     */
    @Transactional
    public PasswordResetResponse confirmPasswordReset(PasswordResetConfirmRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();
        if (token == null || newPassword == null) {
            logger.error("Подтверждение сброса пароля не удалось: код или новый пароль пустые");
            throw new IllegalArgumentException("Код и новый пароль обязательны");
        }

        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            logger.error("Подтверждение сброса пароля не удалось: недействительный код {}", token);
            throw new IllegalArgumentException("Недействительный или истекший код сброса");
        }

        PasswordResetToken resetToken = tokenOpt.get();
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            logger.error("Подтверждение сброса пароля не удалось: код истек для токена {}", token);
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Код истек");
        }

        Optional<Employee> employeeOpt = employeeRepository.findByGuidEmployee(resetToken.getEmployee().getGuidEmployee());
        if (employeeOpt.isEmpty()) {
            logger.error("Подтверждение сброса пароля не удалось: пользователь не найден для guid {}", resetToken.getEmployee().getGuidEmployee());
            throw new IllegalArgumentException("Пользователь не найден");
        }

        Employee employee = employeeOpt.get();
        if (employee.getEmployeeStatus() == null || "Неактивен".equals(employee.getEmployeeStatus().getStatus())) {
            logger.error("Подтверждение сброса пароля не удалось: учетная запись неактивна для guid {}", employee.getGuidEmployee());
            throw new IllegalArgumentException("Учетная запись неактивна");
        }

        if (newPassword.length() < 8 || newPassword.length() > 50) {
            logger.error("Подтверждение сброса пароля не удалось: длина нового пароля {} символов", newPassword.length());
            throw new IllegalArgumentException("Новый пароль должен содержать от 8 до 50 символов");
        }
        if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&._,])[A-Za-z\\d@$!%*#?&._,]{8,50}$")) {
            logger.error("Подтверждение сброса пароля не удалось: новый пароль не соответствует требованиям");
            throw new IllegalArgumentException(
                    "Новый пароль должен содержать минимум одну букву, одну цифру и один специальный символ (@, $, !, %, *, #, ?, &, ., _, ,)");
        }

        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);

        passwordResetTokenRepository.delete(resetToken);
        sessionRepository.deleteByEmployee(employee);

        logger.info("Пароль успешно сброшен для сотрудника {}", employee.getGuidEmployee());
        return new PasswordResetResponse("Пароль успешно сброшен");
    }

    /**
     * Генерирует шестизначный код для сброса пароля.
     *
     * @return Шестизначный код в виде строки.
     */
    private String generateSixDigitCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        logger.debug("Сгенерирован код для сброса пароля: {}", code);
        return String.valueOf(code);
    }

    /**
     * Отправляет письмо с кодом для сброса пароля.
     *
     * @param to        Email получателя.
     * @param code      Код для сброса пароля.
     * @param firstName Имя сотрудника (может быть null).
     * @throws MessagingException Если не удалось отправить письмо.
     */
    private void sendPasswordResetEmail(String to, String code, String firstName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Сброс пароля PlanTime");

        String htmlContent = """
                    <!DOCTYPE html>
                           <html>
                           <head>
                               <meta charset="UTF-8">
                               <meta name="viewport" content="width=device-width, initial-scale=1.0">
                               <title>Сброс пароля PlanTime</title>
                               <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap" rel="stylesheet">
                               <style>
                                   body {
                                       font-family: 'Roboto', Arial, sans-serif;
                                       line-height: 1.6;
                                       color: #333;
                                       background-color: #E3F2FD;
                                       margin: 0;
                                       padding: 0;
                                   }
                                   .container {
                                       max-width: 600px;
                                       margin: 20px auto;
                                       padding: 0;
                                       background-color: #FFFFFF;
                                       border-radius: 10px;
                                       box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
                                   }
                                   .header {
                                       background-color: #1E88E5;
                                       color: #FFFFFF;
                                       padding: 20px;
                                       text-align: center;
                                       border-top-left-radius: 10px;
                                       border-top-right-radius: 10px;
                                   }
                                   .header h2 {
                                       margin: 0;
                                       font-size: 24px;
                                       font-weight: 700;
                                   }
                                   .content {
                                       padding: 30px;
                                       background-color: #F5FAFF;
                                   }
                                   .content p {
                                       margin: 0 0 15px;
                                       font-size: 16px;
                                   }
                                   .code {
                                       font-size: 28px;
                                       font-weight: 700;
                                       color: #1E88E5;
                                       text-align: center;
                                       margin: 25px 0;
                                       background-color: #E3F2FD;
                                       padding: 15px;
                                       border-radius: 8px;
                                       letter-spacing: 2px;
                                   }
                                   .footer {
                                       text-align: center;
                                       font-size: 12px;
                                       color: #78909C;
                                       padding: 20px;
                                       background-color: #F5FAFF;
                                       border-bottom-left-radius: 10px;
                                       border-bottom-right-radius: 10px;
                                   }
                                   @media only screen and (max-width: 600px) {
                                       .container {
                                           margin: 10px;
                                           border-radius: 8px;
                                       }
                                       .content {
                                           padding: 20px;
                                       }
                                       .code {
                                           font-size: 24px;
                                           padding: 10px;
                                       }
                                   }
                               </style>
                           </head>
                           <body>
                               <div class="container">
                                   <div class="header">
                                       <h2>Сброс пароля PlanTime</h2>
                                   </div>
                                   <div class="content">
                                       <p>Уважаемый(ая) %s,</p>
                                       <p>Вы запросили сброс пароля для вашей учетной записи в PlanTime. Ваш код для сброса пароля:</p>
                                       <div class="code">%s</div>
                                       <p>Пожалуйста, используйте этот код в течение 15 минут, чтобы завершить процесс сброса пароля.</p>
                                       <p>Если вы не запрашивали сброс пароля, проигнорируйте это письмо.</p>
                                   </div>
                                   <div class="footer">
                                       <p>PlanTime © 2025. Все права защищены.</p>
                                   </div>
                               </div>
                           </body>
                           </html>
                """.formatted(firstName != null ? firstName : "Пользователь", code);

        helper.setText(htmlContent, true);
        mailSender.send(message);
        logger.info("Письмо с кодом сброса пароля отправлено на {}", to);
    }
}