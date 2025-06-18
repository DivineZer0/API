package plantime.ru.API.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import plantime.ru.API.entity.Customer;
import plantime.ru.API.entity.Organization;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    List<Customer> findAllByOrganization(Organization organization, Sort sort);
    List<Customer> findAll(Sort sort);

    // Поиск по ФИО и телефону для фильтрации
    List<Customer> findByOrganizationAndSurnameContainingIgnoreCaseOrOrganizationAndFirstNameContainingIgnoreCaseOrOrganizationAndPhoneNumberContaining(
            Organization org1, String surname,
            Organization org2, String firstName,
            Organization org3, String phoneNumber,
            Sort sort
    );

    // Проверка дубликата по всем полям
    Optional<Customer> findByFirstNameAndSurnameAndPatronymicAndEmailAndPhoneNumberAndNoteAndOrganization(
            String firstName,
            String surname,
            String patronymic,
            String email,
            String phoneNumber,
            String note,
            Organization organization
    );
}