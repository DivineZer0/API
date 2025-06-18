package plantime.ru.API.repository;

import plantime.ru.API.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistRepository extends JpaRepository<Checklist, Integer> {
    List<Checklist> findByTask_IdTask(Integer idTask);
}