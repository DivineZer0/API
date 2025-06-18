package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import plantime.ru.API.entity.Software;

import java.util.List;

public interface SoftwareRepository extends JpaRepository<Software, Integer> {
    boolean existsBySoftwareIgnoreCase(String software);
    List<Software> findAll(Sort sort);
}