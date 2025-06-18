package plantime.ru.API.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import plantime.ru.API.entity.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Репозиторий для работы с услугами.
 */
public interface ServiceRepository extends JpaRepository<Service, Integer> {

    /**
     * Получить услуги в диапазоне цены.
     */
    @Query("SELECT s FROM Service s WHERE (:minPrice IS NULL OR s.price >= :minPrice) AND (:maxPrice IS NULL OR s.price <= :maxPrice)")
    List<Service> findByPriceBetween(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Sort sort
    );

    /**
     * Проверяет существование услуги с указанным названием.
     */
    boolean existsByServiceIgnoreCase(String service);
}