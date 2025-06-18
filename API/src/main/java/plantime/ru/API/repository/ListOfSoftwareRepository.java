package plantime.ru.API.repository;

import plantime.ru.API.entity.ListOfSoftware;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ListOfSoftwareRepository extends JpaRepository<ListOfSoftware, Integer> {
    List<ListOfSoftware> findByIdProject(Integer idProject);
    boolean existsByIdProjectAndIdSoftware(Integer idProject, Integer idSoftware);
    Optional<ListOfSoftware> findByIdProjectAndIdSoftware(Integer idProject, Integer idSoftware);
}