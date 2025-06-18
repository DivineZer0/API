package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import plantime.ru.API.entity.TaskTree;
import java.util.List;

public interface TaskTreeRepository extends JpaRepository<TaskTree, Integer> {
    List<TaskTree> findByIdProject(Integer idProject);
}