package plantime.ru.API.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import plantime.ru.API.dto.TaskTreeDTO;
import plantime.ru.API.entity.TaskTree;
import plantime.ru.API.repository.TaskTreeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskTreeService {
    @Autowired
    private TaskTreeRepository repo;

    public List<TaskTreeDTO> getByProjectId(Integer projectId) {
        return repo.findByIdProject(projectId)
                .stream()
                .map(t -> new TaskTreeDTO(
                        t.getIdTaskTree(), t.getIdTask(), t.getIdProject(), t.getLevel(), t.getSublevel()
                ))
                .collect(Collectors.toList());
    }
}