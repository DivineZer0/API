package plantime.ru.API.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plantime.ru.API.dto.CustomerDTO;
import plantime.ru.API.entity.Customer;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.entity.Organization;
import plantime.ru.API.entity.Log;
import plantime.ru.API.repository.CustomerRepository;
import plantime.ru.API.repository.OrganizationRepository;
import plantime.ru.API.repository.LogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrganizationRepository organizationRepository;
    private final LogRepository logRepository;
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    public CustomerService(CustomerRepository customerRepository, OrganizationRepository organizationRepository, LogRepository logRepository) {
        this.customerRepository = customerRepository;
        this.organizationRepository = organizationRepository;
        this.logRepository = logRepository;
    }

    /**
     * Получение контрагентов с возможностью отбора, фильтрации и двух видов представления.
     */
    public List<CustomerDTO> getCustomers(Integer organizationId, String view, String sortBy, String order, String search) {
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort;
        switch (sortBy) {
            case "fio":
                sort = Sort.by(direction, "surname").and(Sort.by(direction, "firstName"));
                break;
            case "phoneNumber":
                sort = Sort.by(direction, "phoneNumber");
                break;
            default:
                sort = Sort.by(direction, "surname");
        }

        List<Customer> customers;

        if (organizationId != null) {
            Organization org = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new IllegalArgumentException("Организация не найдена"));
            if (search != null && !search.trim().isEmpty()) {
                String s = search.trim();
                customers = customerRepository.findByOrganizationAndSurnameContainingIgnoreCaseOrOrganizationAndFirstNameContainingIgnoreCaseOrOrganizationAndPhoneNumberContaining(
                        org, s, org, s, org, s, sort
                );
            } else {
                customers = customerRepository.findAllByOrganization(org, sort);
            }
        } else {
            customers = customerRepository.findAll(sort);
            if (search != null && !search.trim().isEmpty()) {
                String s = search.trim().toLowerCase();
                customers = customers.stream().filter(c ->
                        (c.getSurname() != null && c.getSurname().toLowerCase().contains(s))
                                || (c.getFirstName() != null && c.getFirstName().toLowerCase().contains(s))
                                || (c.getPhoneNumber() != null && c.getPhoneNumber().contains(s))
                ).collect(Collectors.toList());
            }
        }

        // "short" view — только ФИО, телефон, организация
        if ("short".equalsIgnoreCase(view)) {
            return customers.stream()
                    .map(c -> new CustomerDTO(
                            c.getIdCustomer(),
                            (c.getSurname() == null ? "" : c.getSurname()) + " " + (c.getFirstName() == null ? "" : c.getFirstName()) +
                                    (c.getPatronymic() == null ? "" : " " + c.getPatronymic()),
                            "", "", // surname и patronymic не нужны в short-view
                            null, // email
                            c.getPhoneNumber(),
                            null, // note
                            c.getOrganization().getId_organization(),
                            c.getOrganization().getShort_name()
                    ))
                    .collect(Collectors.toList());
        }

        // "full" view — все данные
        return customers.stream()
                .map(c -> new CustomerDTO(
                        c.getIdCustomer(), c.getFirstName(), c.getSurname(), c.getPatronymic(),
                        c.getEmail(), c.getPhoneNumber(), c.getNote(),
                        c.getOrganization().getId_organization(), c.getOrganization().getShort_name()
                )).collect(Collectors.toList());
    }

    @Transactional
    public CustomerDTO createCustomer(CustomerDTO dto, Employee authEmployee) {
        try {
            Organization org = organizationRepository.findById(dto.getIdOrganization())
                    .orElseThrow(() -> new IllegalArgumentException("Организация не найдена"));
            // Проверка на дублирование по всем полям
            Optional<Customer> duplicate = customerRepository
                    .findByFirstNameAndSurnameAndPatronymicAndEmailAndPhoneNumberAndNoteAndOrganization(
                            dto.getFirstName(),
                            dto.getSurname(),
                            dto.getPatronymic(),
                            dto.getEmail(),
                            dto.getPhoneNumber(),
                            dto.getNote(),
                            org
                    );
            if (duplicate.isPresent()) {
                throw new IllegalArgumentException("Клиент с такими данными уже существует");
            }
            Customer customer = new Customer();
            customer.setFirstName(dto.getFirstName());
            customer.setSurname(dto.getSurname());
            customer.setPatronymic(dto.getPatronymic());
            customer.setEmail(dto.getEmail());
            customer.setPhoneNumber(dto.getPhoneNumber());
            customer.setNote(dto.getNote());
            customer.setOrganization(org);
            Customer saved = customerRepository.save(customer);
            logRepository.save(new Log(authEmployee, "Создан клиент: " + dto.getSurname(), LocalDateTime.now()));
            return new CustomerDTO(saved.getIdCustomer(), saved.getFirstName(), saved.getSurname(), saved.getPatronymic(),
                    saved.getEmail(), saved.getPhoneNumber(), saved.getNote(), org.getId_organization(), org.getShort_name());
        } catch (Exception e) {
            logger.error("Ошибка при создании клиента: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось создать клиента", e);
        }
    }

    @Transactional
    public CustomerDTO updateCustomer(Integer id, CustomerDTO dto, Employee authEmployee) {
        try {
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));
            Organization org = organizationRepository.findById(dto.getIdOrganization())
                    .orElseThrow(() -> new IllegalArgumentException("Организация не найдена"));

            // Проверка на дублирование по всем полям (кроме текущего клиента)
            Optional<Customer> duplicate = customerRepository
                    .findByFirstNameAndSurnameAndPatronymicAndEmailAndPhoneNumberAndNoteAndOrganization(
                            dto.getFirstName(),
                            dto.getSurname(),
                            dto.getPatronymic(),
                            dto.getEmail(),
                            dto.getPhoneNumber(),
                            dto.getNote(),
                            org
                    );
            if (duplicate.isPresent() && !duplicate.get().getIdCustomer().equals(id)) {
                throw new IllegalArgumentException("Клиент с такими данными уже существует");
            }

            customer.setFirstName(dto.getFirstName());
            customer.setSurname(dto.getSurname());
            customer.setPatronymic(dto.getPatronymic());
            customer.setEmail(dto.getEmail());
            customer.setPhoneNumber(dto.getPhoneNumber());
            customer.setNote(dto.getNote());
            customer.setOrganization(org);
            Customer saved = customerRepository.save(customer);
            logRepository.save(new Log(authEmployee, "Обновлён клиент: " + dto.getSurname(), LocalDateTime.now()));
            return new CustomerDTO(saved.getIdCustomer(), saved.getFirstName(), saved.getSurname(), saved.getPatronymic(),
                    saved.getEmail(), saved.getPhoneNumber(), saved.getNote(), org.getId_organization(), org.getShort_name());
        } catch (Exception e) {
            logger.error("Ошибка при обновлении клиента: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось обновить клиента", e);
        }
    }

    @Transactional
    public void deleteCustomer(Integer id, Employee authEmployee) {
        try {
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));
            customerRepository.deleteById(id);
            customerRepository.flush();
            logRepository.save(new Log(authEmployee, "Удалён клиент с id: " + id, LocalDateTime.now()));
        } catch (Exception e) {
            logger.error("Ошибка при удалении клиента: {}, guid_employee={}", e.getMessage(), authEmployee.getGuidEmployee());
            throw new IllegalArgumentException("Не удалось удалить клиента", e);
        }
    }
}