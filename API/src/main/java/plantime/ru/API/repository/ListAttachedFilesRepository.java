package plantime.ru.API.repository;

import plantime.ru.API.entity.ListAttachedFiles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListAttachedFilesRepository extends JpaRepository<ListAttachedFiles, Integer> {
    List<ListAttachedFiles> findByNote_IdNote(Integer idNote);
}