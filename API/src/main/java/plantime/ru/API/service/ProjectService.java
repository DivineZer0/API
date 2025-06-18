package plantime.ru.API.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plantime.ru.API.entity.Project;
import plantime.ru.API.dto.ProjectDTO;
import plantime.ru.API.entity.ProjectStatus;
import plantime.ru.API.repository.ProjectRepository;
import plantime.ru.API.repository.CustomerRepository;
import plantime.ru.API.repository.ContractRepository;
import plantime.ru.API.repository.ProjectStatusRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository repo;
    @Autowired
    private CustomerRepository customerRepo;
    @Autowired
    private ContractRepository contractRepo;
    @Autowired
    private ProjectStatusRepository projectStatusRepo;

    public List<ProjectDTO> findAll() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ProjectDTO findById(Integer id) {
        return repo.findById(id).map(this::toDTO).orElse(null);
    }

    @Transactional
    public ProjectDTO save(ProjectDTO dto, boolean isCreate) {
        validate(dto, isCreate);
        if (isCreate && repo.existsByProjectNameAndGuidExecutor(dto.getProjectName(), dto.getGuidExecutor())) {
            throw new IllegalArgumentException("Проект с таким названием и исполнителем уже существует");
        }
        Project saved;
        try {
            saved = repo.save(fromDTO(dto));
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Ошибка сохранения проекта: " + ex.getMessage());
        }
        return toDTO(saved);
    }

    @Transactional
    public void delete(Integer id) {
        if (!repo.existsById(id))
            throw new IllegalArgumentException("Проект с таким id не найден");
        repo.deleteById(id);
    }

    private void validate(ProjectDTO dto, boolean isCreate) {
        if (dto == null)
            throw new IllegalArgumentException("Данные проекта не могут быть пустыми");
        if (dto.getProjectName() == null || dto.getProjectName().trim().isEmpty())
            throw new IllegalArgumentException("Название проекта обязательно");
        if (dto.getProjectName().length() > 100)
            throw new IllegalArgumentException("Название проекта должно быть не длиннее 100 символов");
        if (dto.getGuidExecutor() == null || dto.getGuidExecutor().trim().isEmpty())
            throw new IllegalArgumentException("GUID исполнителя обязателен");
        if (dto.getGuidExecutor().length() > 36)
            throw new IllegalArgumentException("GUID исполнителя: не более 36 символов");
        if (dto.getDescription() != null && dto.getDescription().length() > 200)
            throw new IllegalArgumentException("Описание должно быть не длиннее 200 символов");

        if (dto.getIdCustomer() == null)
            throw new IllegalArgumentException("ID клиента обязателен");
        if (!customerRepo.existsById(dto.getIdCustomer()))
            throw new IllegalArgumentException("Клиент с таким id не найден");

        if (dto.getIdContract() == null)
            throw new IllegalArgumentException("ID контракта обязателен");
        if (!contractRepo.existsById(dto.getIdContract()))
            throw new IllegalArgumentException("Контракт с таким id не найден");

        if (dto.getIdProjectStatus() == null)
            throw new IllegalArgumentException("ID статуса проекта обязателен");
        if (!projectStatusRepo.existsById(dto.getIdProjectStatus()))
            throw new IllegalArgumentException("Статус проекта с таким id не найден");

        if (dto.getProjectPrice() != null && dto.getProjectPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Стоимость проекта не может быть отрицательной");

        if (dto.getDateCreate() != null && !isValidDate(dto.getDateCreate()))
            throw new IllegalArgumentException("Некорректная дата создания");
        if (dto.getTimeCreate() != null && !isValidTime(dto.getTimeCreate()))
            throw new IllegalArgumentException("Некорректное время создания");
        if (dto.getDateCompletion() != null && !isValidDate(dto.getDateCompletion()))
            throw new IllegalArgumentException("Некорректная дата завершения");
        if (dto.getTimeCompletion() != null && !isValidTime(dto.getTimeCompletion()))
            throw new IllegalArgumentException("Некорректное время завершения");
    }

    private boolean isValidDate(String str) {
        try { java.time.LocalDate.parse(str); return true; }
        catch (Exception e) { return false; }
    }
    private boolean isValidTime(String str) {
        try { java.time.LocalTime.parse(str); return true; }
        catch (Exception e) { return false; }
    }

    public ProjectDTO toDTO(Project p) {
        if (p == null) return null;
        return new ProjectDTO(
                p.getIdProject(),
                p.getProjectName(),
                p.getGuidExecutor(),
                p.getDescription(),
                p.getDateCreate() == null ? null : p.getDateCreate().toString(),
                p.getTimeCreate() == null ? null : p.getTimeCreate().toString(),
                p.getDateCompletion() == null ? null : p.getDateCompletion().toString(),
                p.getTimeCompletion() == null ? null : p.getTimeCompletion().toString(),
                p.getIdCustomer(),
                p.getIdContract(),
                p.getProjectStatus() == null ? null : p.getProjectStatus().getIdProjectStatus(),
                p.getProjectPrice()
        );
    }

    public Project fromDTO(ProjectDTO dto) {
        Project p = new Project();
        p.setIdProject(dto.getIdProject());
        p.setProjectName(dto.getProjectName());
        p.setGuidExecutor(dto.getGuidExecutor());
        p.setDescription(dto.getDescription());
        p.setDateCreate(dto.getDateCreate() == null ? null : java.time.LocalDate.parse(dto.getDateCreate()));
        p.setTimeCreate(dto.getTimeCreate() == null ? null : java.time.LocalTime.parse(dto.getTimeCreate()));
        p.setDateCompletion(dto.getDateCompletion() == null ? null : java.time.LocalDate.parse(dto.getDateCompletion()));
        p.setTimeCompletion(dto.getTimeCompletion() == null ? null : java.time.LocalTime.parse(dto.getTimeCompletion()));
        p.setIdCustomer(dto.getIdCustomer());
        p.setIdContract(dto.getIdContract());
        if (dto.getIdProjectStatus() != null) {
            ProjectStatus status = projectStatusRepo.findById(dto.getIdProjectStatus()).orElse(null);
            p.setProjectStatus(status);
        } else {
            p.setProjectStatus(null);
        }
        p.setProjectPrice(dto.getProjectPrice());
        return p;
    }
}