package plantime.ru.API.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import plantime.ru.API.dto.EmployeeDTO;
import plantime.ru.API.entity.*;
import plantime.ru.API.repository.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeePostRepository postRepository;
    private final EmployeeStatusRepository statusRepository;
    private final EmployeeDepartmentRepository departmentRepository;
    private final EmployeeGenderRepository genderRepository;
    private final LogRepository logRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    private static final String PHOTO_DIR = "src/main/resources/profile_pictures/";

    public EmployeeService(
            EmployeeRepository employeeRepository,
            EmployeePostRepository postRepository,
            EmployeeStatusRepository statusRepository,
            EmployeeDepartmentRepository departmentRepository,
            EmployeeGenderRepository genderRepository,
            LogRepository logRepository,
            PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.postRepository = postRepository;
        this.statusRepository = statusRepository;
        this.departmentRepository = departmentRepository;
        this.genderRepository = genderRepository;
        this.logRepository = logRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Получает список сотрудников с фильтрацией и поиском.
     */
    public List<EmployeeDTO> getAllEmployees(
            Employee authEmployee,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer genderId,
            Integer postId,
            Integer statusId,
            BigDecimal minHourlyRate,
            BigDecimal maxHourlyRate,
            String department,
            String search) {
        try {
            Integer departmentId = null;
            if (department != null && !department.isBlank()) {
                EmployeeDepartment dep = departmentRepository.findByDepartment(department)
                        .orElse(null);
                if (dep != null) {
                    departmentId = dep.getIdEmployeeDepartment();
                } else {
                    throw new IllegalArgumentException("Отдел с наименованием '" + department + "' не найден");
                }
            }
            List<Employee> employees = employeeRepository.findEmployeesWithFilters(
                    startDate, endDate, genderId, postId, statusId,
                    minHourlyRate, maxHourlyRate, departmentId, search);
            if (employees.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Список сотрудников пуст", LocalDateTime.now()));
                logger.info("Список сотрудников пуст, guid_employee={}", authEmployee.getGuidEmployee());
                return List.of();
            }
            List<EmployeeDTO> employeeDTOs = employees.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            logRepository.save(new Log(authEmployee, "Получен список сотрудников, количество: " + employees.size(), LocalDateTime.now()));
            logger.info("Успешно получен список сотрудников, количество: {}, guid_employee={}", employees.size(), authEmployee.getGuidEmployee());
            return employeeDTOs;
        } catch (Exception e) {
            logger.error("Ошибка при получении списка сотрудников: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось получить список сотрудников", e);
        }
    }

    /**
     * Создаёт нового сотрудника.
     * Всегда добавляется с паролем "Asd11016".
     * Фото сохраняется в resources, в БД только имя файла.
     * Проверка на совпадение по ФИО, телефону, email, отделу и должности.
     */
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO, MultipartFile photo, Employee authEmployee) throws IOException {
        try {
            validateEmployeeData(employeeDTO, null);

            // Проверка на совпадение по ФИО, телефону, email, отделу и должности
            if (existsSimilarEmployee(employeeDTO)) {
                throw new IllegalArgumentException("Сотрудник с такими ФИО, телефоном, email, отделом и должностью уже существует");
            }

            String guid = UUID.randomUUID().toString();
            Employee employee = new Employee();
            populateEmployeeFromDTO(employee, employeeDTO);

            employee.setGuidEmployee(guid);
            employee.setPassword(passwordEncoder.encode("Asd11016"));

            // Сохраняем фото и устанавливаем имя файла
            String photoFilename = saveProfilePhoto(photo, guid);
            employee.setProfilePicture(photoFilename);

            Employee savedEmployee = employeeRepository.save(employee);
            logRepository.save(new Log(authEmployee, "Создан сотрудник: " + savedEmployee.getLogin(), LocalDateTime.now()));
            logger.info("Успешно создан сотрудник: {}, guid_employee={}", savedEmployee.getLogin(), authEmployee.getGuidEmployee());
            return mapToDTO(savedEmployee);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при создании сотрудника: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при создании сотрудника: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось создать сотрудника", e);
        }
    }

    /**
     * Обновляет данные сотрудника.
     * Фото можно не передавать.
     */
    @Transactional
    public EmployeeDTO updateEmployee(String guid, EmployeeDTO employeeDTO, MultipartFile photo, Employee authEmployee) throws IOException {
        try {
            Optional<Employee> existing = employeeRepository.findById(guid);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось обновить сотрудника: сотрудник с guid " + guid + " не найден", LocalDateTime.now()));
                logger.error("Сотрудник с guid {} не найден, guid_employee={}", guid, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Сотрудник с guid " + guid + " не найден");
            }
            Employee employee = existing.get();
            validateEmployeeData(employeeDTO, employee);

            // Проверка на совпадение по ФИО, телефону, email, отделу и должности (кроме самого себя)
            if (existsSimilarEmployeeForUpdate(employeeDTO, guid)) {
                throw new IllegalArgumentException("Сотрудник с такими ФИО, телефоном, email, отделом и должностью уже существует");
            }

            populateEmployeeFromDTO(employee, employeeDTO);

            // Если фото передано — сохранить и обновить имя файла
            if (photo != null && !photo.isEmpty()) {
                String photoFilename = saveProfilePhoto(photo, guid);
                employee.setProfilePicture(photoFilename);
            }

            Employee updatedEmployee = employeeRepository.save(employee);
            logRepository.save(new Log(authEmployee, "Обновлён сотрудник: " + updatedEmployee.getLogin(), LocalDateTime.now()));
            logger.info("Успешно обновлён сотрудник: {}, guid_employee={}", updatedEmployee.getLogin(), authEmployee.getGuidEmployee());
            return mapToDTO(updatedEmployee);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при обновлении сотрудника: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при обновлении сотрудника: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось обновить сотрудника", e);
        }
    }

    @Transactional
    public void deleteEmployee(String guid, Employee authEmployee) {
        try {
            Optional<Employee> existing = employeeRepository.findById(guid);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось удалить сотрудника: сотрудник с guid " + guid + " не найден", LocalDateTime.now()));
                logger.error("Сотрудник с guid {} не найден, guid_employee={}", guid, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Сотрудник с guid " + guid + " не найден");
            }
            employeeRepository.deleteById(guid);
            employeeRepository.flush();
            logRepository.save(new Log(authEmployee, "Удалён сотрудник с guid: " + guid, LocalDateTime.now()));
            logger.info("Успешно удалён сотрудник с guid: {}, guid_employee={}", guid, authEmployee.getGuidEmployee());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Нарушение целостности данных при удалении сотрудника с guid {}: {}, guid_employee={}", guid, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось удалить сотрудника с guid " + guid + " из-за зависимостей в базе данных", e);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при удалении сотрудника: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw e;
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при удалении сотрудника с guid {}: {}, guid_employee={}", guid, e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось удалить сотрудника", e);
        }
    }

    /**
     * Смена пароля сотрудника по GUID.
     */
    @Transactional
    public void changePassword(String guid, String newPassword, Employee authEmployee) {
        try {
            Optional<Employee> existing = employeeRepository.findById(guid);
            if (existing.isEmpty()) {
                logRepository.save(new Log(authEmployee, "Не удалось сменить пароль: сотрудник с guid " + guid + " не найден", LocalDateTime.now()));
                logger.error("Сотрудник с guid {} не найден, guid_employee={}", guid, authEmployee.getGuidEmployee());
                throw new IllegalArgumentException("Сотрудник с guid " + guid + " не найден");
            }
            Employee employee = existing.get();
            employee.setPassword(passwordEncoder.encode(newPassword));
            employeeRepository.save(employee);
            logRepository.save(new Log(authEmployee, "Сменён пароль сотрудника с guid: " + guid, LocalDateTime.now()));
            logger.info("Успешно сменён пароль сотрудника с guid: {}, guid_employee={}", guid, authEmployee.getGuidEmployee());
        } catch (Exception e) {
            logger.error("Ошибка при смене пароля сотрудника: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось сменить пароль сотрудника", e);
        }
    }

    /**
     * Проверяет на совпадение сотрудника по ФИО, телефону, email, отделу и должности.
     */
    private boolean existsSimilarEmployee(EmployeeDTO dto) {
        return employeeRepository.findAll().stream().anyMatch(e ->
                e.getSurname().equalsIgnoreCase(dto.getSurname())
                        && e.getFirstName().equalsIgnoreCase(dto.getFirstName())
                        && ((e.getPatronymic() == null && (dto.getPatronymic() == null || dto.getPatronymic().isBlank())) ||
                        (e.getPatronymic() != null && dto.getPatronymic() != null &&
                                e.getPatronymic().equalsIgnoreCase(dto.getPatronymic())))
                        && e.getPhoneNumber().equals(dto.getPhoneNumber())
                        && e.getEmail().equalsIgnoreCase(dto.getEmail())
                        && e.getEmployeeDepartment().getIdEmployeeDepartment().equals(dto.getIdEmployeeDepartment())
                        && e.getEmployeePost().getIdEmployeePost().equals(dto.getIdEmployeePost())
        );
    }

    private boolean existsSimilarEmployeeForUpdate(EmployeeDTO dto, String guid) {
        return employeeRepository.findAll().stream().anyMatch(e ->
                !e.getGuidEmployee().equals(guid)
                        && e.getSurname().equalsIgnoreCase(dto.getSurname())
                        && e.getFirstName().equalsIgnoreCase(dto.getFirstName())
                        && ((e.getPatronymic() == null && (dto.getPatronymic() == null || dto.getPatronymic().isBlank())) ||
                        (e.getPatronymic() != null && dto.getPatronymic() != null &&
                                e.getPatronymic().equalsIgnoreCase(dto.getPatronymic())))
                        && e.getPhoneNumber().equals(dto.getPhoneNumber())
                        && e.getEmail().equalsIgnoreCase(dto.getEmail())
                        && e.getEmployeeDepartment().getIdEmployeeDepartment().equals(dto.getIdEmployeeDepartment())
                        && e.getEmployeePost().getIdEmployeePost().equals(dto.getIdEmployeePost())
        );
    }

    private String saveProfilePhoto(MultipartFile photo, String guid) throws IOException {
        if (photo == null || photo.isEmpty()) throw new IllegalArgumentException("Фото обязательно");
        File dir = new File(PHOTO_DIR);
        if (!dir.exists()) dir.mkdirs();
        String extension = "";
        String originalFilename = photo.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        String filename = guid + extension;
        File file = new File(dir, filename);
        Files.copy(photo.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    private void validateEmployeeData(EmployeeDTO dto, Employee existing) {
        String login = dto.getLogin().trim();
        String email = dto.getEmail().trim();
        String surname = dto.getSurname().trim();
        String firstName = dto.getFirstName().trim();
        String patronymic = dto.getPatronymic() != null ? dto.getPatronymic().trim() : null;

        if (!surname.matches("^[a-zA-Zа-яА-Я\\s-]*$") || !firstName.matches("^[a-zA-Zа-яА-Я\\s-]*$") ||
                (patronymic != null && !patronymic.matches("^[a-zA-Zа-яА-Я\\s-]*$"))) {
            throw new IllegalArgumentException("ФИО не должно содержать цифры");
        }

        if (existing == null || !login.equalsIgnoreCase(existing.getLogin())) {
            if (employeeRepository.existsByLoginIgnoreCase(login)) {
                throw new IllegalArgumentException("Логин '" + login + "' уже используется");
            }
        }
        if (existing == null || !email.equalsIgnoreCase(existing.getEmail())) {
            if (employeeRepository.existsByEmailIgnoreCase(email)) {
                throw new IllegalArgumentException("Электронная почта '" + email + "' уже используется");
            }
        }
        if (!postRepository.existsById(dto.getIdEmployeePost())) {
            throw new IllegalArgumentException("Должность с id " + dto.getIdEmployeePost() + " не найдена");
        }
        if (!statusRepository.existsById(dto.getIdEmployeeStatus())) {
            throw new IllegalArgumentException("Статус с id " + dto.getIdEmployeeStatus() + " не найден");
        }
        if (!departmentRepository.existsById(dto.getIdEmployeeDepartment())) {
            throw new IllegalArgumentException("Отдел с id " + dto.getIdEmployeeDepartment() + " не найден");
        }
        if (!genderRepository.existsById(dto.getIdEmployeeGender())) {
            throw new IllegalArgumentException("Пол с id " + dto.getIdEmployeeGender() + " не найден");
        }
    }

    private void populateEmployeeFromDTO(Employee employee, EmployeeDTO dto) {
        employee.setLogin(dto.getLogin().trim());
        employee.setEmail(dto.getEmail().trim());
        employee.setSurname(dto.getSurname().trim());
        employee.setFirstName(dto.getFirstName().trim());
        employee.setPatronymic(dto.getPatronymic() != null ? dto.getPatronymic().trim() : null);
        employee.setProfilePicture(dto.getProfilePicture() != null ? dto.getProfilePicture().trim() : null); // будет заменено если фото передано
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setHourlyRate(dto.getHourlyRate());
        employee.setNote(dto.getNote());
        employee.setEmployeePost(postRepository.findById(dto.getIdEmployeePost()).orElseThrow());
        employee.setEmployeeStatus(statusRepository.findById(dto.getIdEmployeeStatus()).orElseThrow());
        employee.setEmployeeDepartment(departmentRepository.findById(dto.getIdEmployeeDepartment()).orElseThrow());
        employee.setEmployeeGender(genderRepository.findById(dto.getIdEmployeeGender()).orElseThrow());
    }

    private EmployeeDTO mapToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO(
                employee.getGuidEmployee(), employee.getLogin(), employee.getEmail(), null,
                employee.getSurname(), employee.getFirstName(), employee.getPatronymic(),
                employee.getProfilePicture(), employee.getLastAuthorization(), employee.getDateOfBirth(),
                employee.getPhoneNumber(), employee.getHourlyRate(), employee.getNote(),
                employee.getEmployeePost().getIdEmployeePost(),
                employee.getEmployeeStatus().getIdEmployeeStatus(),
                employee.getEmployeeDepartment().getIdEmployeeDepartment(),
                employee.getEmployeeGender().getIdEmployeeGender()
        );
        // Названия связанных сущностей
        dto.setDepartmentName(employee.getEmployeeDepartment() != null ? employee.getEmployeeDepartment().getDepartment() : null);
        dto.setPostName(employee.getEmployeePost() != null ? employee.getEmployeePost().getPost() : null);
        dto.setStatusName(employee.getEmployeeStatus() != null ? employee.getEmployeeStatus().getStatus() : null);
        dto.setGenderName(employee.getEmployeeGender() != null ? employee.getEmployeeGender().getGender() : null);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<String> getShortEmployeesByDepartment(String department) {
        Integer departmentId = null;
        if (department != null && !department.isBlank()) {
            EmployeeDepartment dep = departmentRepository.findByDepartment(department)
                    .orElse(null);
            if (dep != null) {
                departmentId = dep.getIdEmployeeDepartment();
            } else {
                throw new IllegalArgumentException("Отдел с наименованием '" + department + "' не найден");
            }
        }
        List<Employee> employees = employeeRepository.findEmployeesWithFilters(
                null, null, null, null, null, null, null, departmentId, null
        );
        return employees.stream()
                .map(emp -> String.format(
                        "%s %s%s (%s)",
                        emp.getSurname(),
                        emp.getFirstName(),
                        emp.getPatronymic() != null && !emp.getPatronymic().isBlank() ? " " + emp.getPatronymic() : "",
                        emp.getPhoneNumber()
                ))
                .collect(Collectors.toList());
    }
}