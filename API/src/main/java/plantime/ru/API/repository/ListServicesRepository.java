package plantime.ru.API.repository;

import plantime.ru.API.entity.ListServices;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListServicesRepository extends JpaRepository<ListServices, Integer> {
    List<ListServices> findByIdTask(Integer idTask);

    // Новый метод для поиска по id услуги
    List<ListServices> findByIdService(Integer idService);
}