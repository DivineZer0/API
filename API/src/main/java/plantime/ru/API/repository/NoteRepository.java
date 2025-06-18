package plantime.ru.API.repository;

import plantime.ru.API.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Integer> {
    List<Note> findByTask_IdTask(Integer idTask);
}