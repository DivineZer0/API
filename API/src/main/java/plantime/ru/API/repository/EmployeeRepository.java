    package plantime.ru.API.repository;

    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import plantime.ru.API.entity.*;

    import java.math.BigDecimal;
    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.Optional;

    /**
     * Репозиторий для работы с сущностью {@link Employee} в базе данных.
     */
    public interface EmployeeRepository extends JpaRepository<Employee, String> {

        /**
         * Проверяет существование сотрудника с указанным логином (без учёта регистра).
         *
         * @param login Логин сотрудника.
         * @return true, если сотрудник существует, иначе false.
         */
        boolean existsByLoginIgnoreCase(String login);

        /**
         * Проверяет существование сотрудника с указанным email (без учёта регистра).
         *
         * @param email Электронная почта сотрудника.
         * @return true, если сотрудник существует, иначе false.
         */
        boolean existsByEmailIgnoreCase(String email);

        /**
         * Находит сотрудников с фильтрацией и поиском.
         *
         * @param startDate Начальная дата рождения.
         * @param endDate Конечная дата рождения.
         * @param genderId Идентификатор пола.
         * @param postId Идентификатор должности.
         * @param statusId Идентификатор статуса.
         * @param minHourlyRate Минимальная почасовая ставка.
         * @param maxHourlyRate Максимальная почасовая ставка.
         * @param departmentId Идентификатор отдела.
         * @param search Поисковая строка для ФИО или email.
         * @return Список сотрудников, соответствующих критериям.
         */
        @Query("SELECT e FROM Employee e " +
                "WHERE (:startDate IS NULL OR e.dateOfBirth >= :startDate) " +
                "AND (:endDate IS NULL OR e.dateOfBirth <= :endDate) " +
                "AND (:genderId IS NULL OR e.employeeGender.idEmployeeGender = :genderId) " +
                "AND (:postId IS NULL OR e.employeePost.idEmployeePost = :postId) " +
                "AND (:statusId IS NULL OR e.employeeStatus.idEmployeeStatus = :statusId) " +
                "AND (:minHourlyRate IS NULL OR e.hourlyRate >= :minHourlyRate) " +
                "AND (:maxHourlyRate IS NULL OR e.hourlyRate <= :maxHourlyRate) " +
                "AND (:departmentId IS NULL OR e.employeeDepartment.idEmployeeDepartment = :departmentId) " +
                "AND (:search IS NULL OR " +
                "LOWER(CONCAT(e.surname, ' ', e.firstName, ' ', COALESCE(e.patronymic, ''))) LIKE LOWER(CONCAT('%', :search, '%')) " +
                "OR LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')))")
        List<Employee> findEmployeesWithFilters(
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate,
                @Param("genderId") Integer genderId,
                @Param("postId") Integer postId,
                @Param("statusId") Integer statusId,
                @Param("minHourlyRate") BigDecimal minHourlyRate,
                @Param("maxHourlyRate") BigDecimal maxHourlyRate,
                @Param("departmentId") Integer departmentId,
                @Param("search") String search);

        /**
         * Находит сотрудника по логину или email.
         *
         * @param login Логин сотрудника.
         * @param email Электронная почта сотрудника.
         * @return {@link Optional} с найденным сотрудником или пустой, если сотрудник не найден.
         */
        Optional<Employee> findByLoginOrEmail(String login, String email);

        /**
         * Находит сотрудника по уникальному идентификатору.
         *
         * @param guidEmployee Уникальный идентификатор сотрудника.
         * @return {@link Optional} с найденным сотрудником или пустой, если сотрудник не найден.
         */
        Optional<Employee> findByGuidEmployee(String guidEmployee);

        /**
         * Проверяет, используется ли отдел сотрудниками.
         *
         * @param departmentId ID отдела для проверки.
         * @return true, если отдел используется хотя бы одним сотрудником, иначе false.
         */
        boolean existsByEmployeeDepartment(EmployeeDepartment  department);

        /**
         * Проверяет, используется ли пол сотрудниками.
         */
        boolean existsByEmployeeGender(EmployeeGender gender);

        /**
         * Проверяет, используется ли должность сотрудниками.
         */
        boolean existsByEmployeePost(EmployeePost post);

        /**
         * Проверяет, используется ли статус сотрудниками.
         */
        boolean existsByEmployeeStatus(EmployeeStatus status);

        @Query("""
            SELECT e FROM Employee e
            WHERE e.surname = :surname
              AND e.firstName = :firstName
              AND (:patronymic IS NULL OR e.patronymic = :patronymic)
            """)
        Optional<Employee> findBySurnameAndFirstNameAndPatronymic(String surname, String firstName, String patronymic);
    }