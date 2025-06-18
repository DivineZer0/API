package plantime.ru.API.repository;

import plantime.ru.API.entity.Task;
import plantime.ru.API.entity.TaskStatus;
import plantime.ru.API.entity.TaskType;
import plantime.ru.API.entity.TaskRecurrence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    boolean existsByTaskType(TaskType taskType);
    boolean existsByTaskStatus(TaskStatus taskStatus);
    boolean existsByTaskRecurrence(TaskRecurrence taskRecurrence);
}