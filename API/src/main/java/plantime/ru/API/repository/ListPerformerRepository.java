package plantime.ru.API.repository;

import plantime.ru.API.entity.ListPerformer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListPerformerRepository extends JpaRepository<ListPerformer, Integer> {
    List<ListPerformer> findByTask_IdTask(Integer idTask);
}