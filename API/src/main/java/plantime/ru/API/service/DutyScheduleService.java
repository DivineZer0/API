package plantime.ru.API.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plantime.ru.API.dto.DutyScheduleDTO;
import plantime.ru.API.entity.*;
import plantime.ru.API.repository.DutyScheduleRepository;
import plantime.ru.API.repository.EmployeeRepository;
import plantime.ru.API.repository.TypeAbsenceRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для управления расписанием дежурств/отсутствий сотрудников.
 * Обеспечивает бизнес-логику для фильтрации, создания, обновления и удаления записей расписания,
 * а также работы с типами отсутствий и поиска сотрудников по ФИО.
 */
@Service
public class DutyScheduleService {

    private final DutyScheduleRepository dutyScheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final TypeAbsenceRepository typeAbsenceRepository;

    public DutyScheduleService(
            DutyScheduleRepository dutyScheduleRepository,
            EmployeeRepository employeeRepository,
            TypeAbsenceRepository typeAbsenceRepository) {
        this.dutyScheduleRepository = dutyScheduleRepository;
        this.employeeRepository = employeeRepository;
        this.typeAbsenceRepository = typeAbsenceRepository;
    }

    /**
     * Возвращает список расписаний с фильтрацией по отделу, сотруднику и типу отсутствия.
     *
     * @param department    Название отдела
     * @param employeeName  ФИО сотрудника
     * @param typeOfAbsence Тип отсутствия
     * @return Отфильтрованный список DutyScheduleDTO
     */
    @Transactional(readOnly = true)
    public List<DutyScheduleDTO> getFilteredSchedules(String department, String employeeName, String typeOfAbsence) {
        List<DutySchedule> allSchedules = dutyScheduleRepository.findAllWithEmployee();

        return allSchedules.stream()
                .filter(ds -> department == null || department.isBlank()
                        || (ds.getEmployee().getEmployeeDepartment() != null
                        && ds.getEmployee().getEmployeeDepartment().getDepartment().toLowerCase().contains(department.trim().toLowerCase())))
                .filter(ds -> employeeName == null || employeeName.isBlank()
                        || getFullEmployeeName(ds.getEmployee()).toLowerCase().contains(employeeName.trim().toLowerCase()))
                .filter(ds -> typeOfAbsence == null || typeOfAbsence.isBlank()
                        || (ds.getTypeOfAbsence() != null
                        && ds.getTypeOfAbsence().getTypeOfAbsence().toLowerCase().contains(typeOfAbsence.trim().toLowerCase())))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает список расписаний за указанный период и с фильтрацией по отделу.
     * В результирующий список попадают только записи с типом отсутствия "Дежурство" или "Отпуск".
     *
     * @param start       Дата начала периода
     * @param end         Дата окончания периода
     * @param department  Название отдела
     * @return Список DutyScheduleDTO за выбранный период и для выбранного отдела
     */
    public List<DutyScheduleDTO> getSchedulesForPeriod(LocalDate start, LocalDate end, String department) {
        List<DutySchedule> allSchedules = dutyScheduleRepository.findAllWithEmployee();
        return allSchedules.stream()
                .filter(ds -> (start == null || !ds.getDateEnd().isBefore(start)) && (end == null || !ds.getDateStart().isAfter(end)))
                .filter(ds -> department == null || department.isBlank()
                        || (ds.getEmployee().getEmployeeDepartment() != null
                        && ds.getEmployee().getEmployeeDepartment().getDepartment().toLowerCase().contains(department.trim().toLowerCase())))
                .filter(ds -> {
                    String type = ds.getTypeOfAbsence() != null ? ds.getTypeOfAbsence().getTypeOfAbsence().toLowerCase() : "";
                    return type.equals("дежурство") || type.equals("отпуск");
                })
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Создаёт новую запись расписания.
     * Проверяет существование сотрудника, типа отсутствия и отсутствие пересечения дат.
     *
     * @param dto   DTO с данными для создания записи
     * @param admin Аутентифицированный сотрудник-администратор
     * @return Созданный DutyScheduleDTO
     */
    @Transactional
    public DutyScheduleDTO createSchedule(DutyScheduleDTO dto, Employee admin) {
        Employee employee = findEmployeeByFio(dto.getEmployeeName());
        if (employee == null) {
            throw new IllegalArgumentException("Сотрудник не найден: " + dto.getEmployeeName());
        }
        TypeAbsence typeAbsence = typeAbsenceRepository.findByTypeOfAbsenceIgnoreCase(dto.getTypeOfAbsence())
                .orElseThrow(() -> new IllegalArgumentException("Тип отсутствия не найден: " + dto.getTypeOfAbsence()));

        boolean intersects = dutyScheduleRepository.existsIntersecting(
                employee.getGuidEmployee(),
                dto.getDateStart(),
                dto.getDateEnd(),
                null
        );
        if (intersects) {
            throw new IllegalArgumentException("Пересечение дат для сотрудника " + dto.getEmployeeName());
        }

        DutySchedule entity = new DutySchedule();
        entity.setEmployee(employee);
        entity.setDateStart(dto.getDateStart());
        entity.setDateEnd(dto.getDateEnd());
        entity.setTypeOfAbsence(typeAbsence);
        entity.setDescription(dto.getDescription());

        DutySchedule saved = dutyScheduleRepository.save(entity);
        return toDTO(saved);
    }

    /**
     * Обновляет существующую запись расписания.
     * Проверяет существование сотрудника, типа отсутствия и отсутствие пересечения дат.
     *
     * @param id     Идентификатор редактируемой записи
     * @param dto    DTO с обновлёнными данными
     * @param admin  Аутентифицированный сотрудник-администратор
     * @return Обновлённый DutyScheduleDTO
     */
    @Transactional
    public DutyScheduleDTO updateSchedule(Long id, DutyScheduleDTO dto, Employee admin) {
        DutySchedule entity = dutyScheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Запись не найдена"));

        Employee employee = findEmployeeByFio(dto.getEmployeeName());
        if (employee == null) {
            throw new IllegalArgumentException("Сотрудник не найден: " + dto.getEmployeeName());
        }
        TypeAbsence typeAbsence = typeAbsenceRepository.findByTypeOfAbsenceIgnoreCase(dto.getTypeOfAbsence())
                .orElseThrow(() -> new IllegalArgumentException("Тип отсутствия не найден: " + dto.getTypeOfAbsence()));

        boolean intersects = dutyScheduleRepository.existsIntersecting(
                employee.getGuidEmployee(),
                dto.getDateStart(),
                dto.getDateEnd(),
                id
        );
        if (intersects) {
            throw new IllegalArgumentException("Пересечение дат для сотрудника " + dto.getEmployeeName());
        }

        entity.setEmployee(employee);
        entity.setDateStart(dto.getDateStart());
        entity.setDateEnd(dto.getDateEnd());
        entity.setTypeOfAbsence(typeAbsence);
        entity.setDescription(dto.getDescription());

        DutySchedule saved = dutyScheduleRepository.save(entity);
        return toDTO(saved);
    }

    /**
     * Удаляет расписание по идентификатору.
     *
     * @param id    Идентификатор записи
     * @param admin Аутентифицированный сотрудник-администратор
     */
    @Transactional
    public void deleteSchedule(Long id, Employee admin) {
        if (!dutyScheduleRepository.existsById(id)) {
            throw new IllegalArgumentException("Запись не найдена");
        }
        dutyScheduleRepository.deleteById(id);
    }

    /**
     * Возвращает все возможные типы отсутствий.
     *
     * @return Список всех типов отсутствий
     */
    @Transactional(readOnly = true)
    public List<String> getAllAbsenceTypes() {
        return typeAbsenceRepository.findAll()
                .stream()
                .map(TypeAbsence::getTypeOfAbsence)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Ищет сотрудника по ФИО.
     *
     * @param fio ФИО сотрудника
     * @return Сущность Employee или null, если не найден
     */
    @Transactional(readOnly = true)
    public Employee findEmployeeByFio(String fio) {
        if (fio == null) return null;
        String[] parts = fio.trim().split("\\s+");
        if (parts.length < 2) return null;
        String surname = parts[0];
        String firstName = parts[1];
        String patronymic = parts.length > 2 ? parts[2] : null;
        return employeeRepository.findBySurnameAndFirstNameAndPatronymic(
                surname, firstName, patronymic
        ).orElse(null);
    }

    /**
     * Преобразует сущность DutySchedule в DTO.
     *
     * @param ds Сущность DutySchedule
     * @return DutyScheduleDTO
     */
    public DutyScheduleDTO toDTO(DutySchedule ds) {
        String fio = getFullEmployeeName(ds.getEmployee());
        return new DutyScheduleDTO(
                ds.getIdDutySchedule(),
                fio,
                ds.getDateStart(),
                ds.getDateEnd(),
                ds.getTypeOfAbsence() != null ? ds.getTypeOfAbsence().getTypeOfAbsence() : null,
                ds.getDescription()
        );
    }

    /**
     * Собирает ФИО сотрудника в одну строку.
     *
     * @param emp Сущность Employee
     * @return ФИО строкой
     */
    private String getFullEmployeeName(Employee emp) {
        StringBuilder sb = new StringBuilder();
        if (emp.getSurname() != null) sb.append(emp.getSurname());
        if (emp.getFirstName() != null) sb.append(" ").append(emp.getFirstName());
        if (emp.getPatronymic() != null && !emp.getPatronymic().isBlank())
            sb.append(" ").append(emp.getPatronymic());
        return sb.toString().trim();
    }
}