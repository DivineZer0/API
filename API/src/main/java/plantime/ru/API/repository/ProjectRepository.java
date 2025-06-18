package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import plantime.ru.API.entity.Project;
import plantime.ru.API.entity.ProjectStatus;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    boolean existsByProjectNameAndGuidExecutor(String projectName, String guidExecutor);
    boolean existsByProjectStatus(ProjectStatus projectStatus);
}