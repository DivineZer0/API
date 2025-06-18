package plantime.ru.API.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import plantime.ru.API.entity.DutySchedule;
import plantime.ru.API.entity.TypeAbsence;

import java.time.LocalDate;
import java.util.List;

/**
 * Репозиторий для работы с расписанием дежурств/отсутствий.
 */
public interface DutyScheduleRepository extends JpaRepository<DutySchedule, Long> {
    /**
     * Получить все записи с сотрудником, отделом и типом отсутствия.
     */
    @Query("SELECT ds FROM DutySchedule ds LEFT JOIN FETCH ds.employee e LEFT JOIN FETCH e.employeeDepartment LEFT JOIN FETCH ds.typeOfAbsence")
    List<DutySchedule> findAllWithEmployee();

    /**
     * Проверить пересечение периодов для сотрудника.
     */
    @Query("""
        SELECT CASE WHEN COUNT(ds) > 0 THEN true ELSE false END FROM DutySchedule ds
        WHERE ds.employee.guidEmployee = :employeeId
          AND ((ds.dateStart <= :dateEnd AND ds.dateEnd >= :dateStart))
          AND (:excludeId IS NULL OR ds.idDutySchedule <> :excludeId)
        """)
    boolean existsIntersecting(@Param("employeeId") String employeeId,
                               @Param("dateStart") LocalDate dateStart,
                               @Param("dateEnd") LocalDate dateEnd,
                               @Param("excludeId") Long excludeId);

    /**
     * Проверить, есть ли записи с данным типом отсутствия.
     */
    boolean existsByTypeOfAbsence(TypeAbsence typeOfAbsence);
}