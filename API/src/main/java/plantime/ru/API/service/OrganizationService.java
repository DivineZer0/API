package plantime.ru.API.service;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import plantime.ru.API.dto.OrganizationDTO;
import plantime.ru.API.dto.OrganizationDetailsDTO;
import plantime.ru.API.entity.Customer;
import plantime.ru.API.entity.Organization;
import plantime.ru.API.repository.OrganizationRepository;
import plantime.ru.API.repository.CustomerRepository;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class OrganizationService {
    private final OrganizationRepository repository;
    private final CustomerRepository customerRepository;

    public OrganizationService(OrganizationRepository repository, CustomerRepository customerRepository) {
        this.repository = repository;
        this.customerRepository = customerRepository;
    }

    public List<OrganizationDTO> findAllWithFilter(OrganizationDTO filter) {
        Organization probe = new Organization();
        probe.setShort_name(filter.getShort_name());
        probe.setLong_name(filter.getLong_name());
        probe.setInn(filter.getInn());
        probe.setEmail(filter.getEmail());
        probe.setPhone_number(filter.getPhone_number());

        Example<Organization> example = Example.of(probe);

        return repository.findAll(example).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public OrganizationDetailsDTO findById(Integer id) {
        Organization org = repository.findById(id).orElseThrow();
        return toDetailsDTO(org);
    }

    public OrganizationDetailsDTO createOrUpdate(Integer id, OrganizationDetailsDTO dto) {
        // Валидация
        String err = validateOrganizationFields(dto, id);
        if (err != null) throw new IllegalArgumentException(err);

        Organization org = id != null
                ? repository.findById(id).orElse(new Organization())
                : new Organization();

        if (id != null) org.setId_organization(id);

        org.setShort_name(dto.getShort_name());
        org.setLong_name(dto.getLong_name());
        org.setInn(dto.getInn());
        org.setKpp(dto.getKpp());
        org.setOgrn(dto.getOgrn());
        org.setEmail(dto.getEmail());
        org.setAddress(dto.getAddress());
        org.setNote(dto.getNote());
        org.setPhone_number(dto.getPhone_number());

        Organization saved = repository.save(org);
        return toDetailsDTO(saved);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    // Получение контрагентов по организации
    public List<Customer> getCustomersByOrganization(Integer organizationId) {
        Organization org = repository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Организация не найдена"));
        return customerRepository.findAllByOrganization(org, Sort.by("surname"));
    }

    // --- Mapping helpers ---

    public OrganizationDTO toDTO(Organization org) {
        OrganizationDTO dto = new OrganizationDTO();
        dto.setId_organization(org.getId_organization());
        dto.setShort_name(org.getShort_name());
        dto.setLong_name(org.getLong_name());
        dto.setInn(org.getInn());
        dto.setEmail(org.getEmail());
        dto.setPhone_number(org.getPhone_number());
        return dto;
    }

    public OrganizationDetailsDTO toDetailsDTO(Organization org) {
        OrganizationDetailsDTO dto = new OrganizationDetailsDTO();
        dto.setId_organization(org.getId_organization());
        dto.setShort_name(org.getShort_name());
        dto.setLong_name(org.getLong_name());
        dto.setInn(org.getInn());
        dto.setKpp(org.getKpp());
        dto.setOgrn(org.getOgrn());
        dto.setEmail(org.getEmail());
        dto.setAddress(org.getAddress());
        dto.setNote(org.getNote());
        dto.setPhone_number(org.getPhone_number());
        return dto;
    }

    // --- Валидация и проверка на дублирование ---

    public String validateOrganizationFields(OrganizationDetailsDTO dto, Integer idForUpdate) {
        // Краткое название, полное название
        if (!StringUtils.hasText(dto.getShort_name()) || dto.getShort_name().length() < 2 || dto.getShort_name().length() > 40)
            return "Краткое название обязательно (2-40 символов)";
        if (!StringUtils.hasText(dto.getLong_name()) || dto.getLong_name().length() < 2 || dto.getLong_name().length() > 120)
            return "Полное название обязательно (2-120 символов)";

        // ИНН: 10 или 12 цифр + контрольная сумма
        if (!isValidInn(dto.getInn()))
            return "Некорректный ИНН (10 или 12 цифр, контрольная сумма)";

        // КПП: 9 цифр
        if (StringUtils.hasText(dto.getKpp()) && !Pattern.matches("^\\d{9}$", dto.getKpp()))
            return "КПП должен содержать 9 цифр";

        // ОГРН: 13 или 15 цифр + контрольная сумма
        if (StringUtils.hasText(dto.getOgrn()) && !isValidOgrn(dto.getOgrn()))
            return "Некорректный ОГРН (13 или 15 цифр, контрольная сумма)";

        // Телефон: +7(XXX)XXX-XX-XX
        if (StringUtils.hasText(dto.getPhone_number()) && !Pattern.matches("^\\+7\\(\\d{3}\\)\\d{3}-\\d{2}-\\d{2}$", dto.getPhone_number()))
            return "Телефон должен быть в формате +7(XXX)XXX-XX-XX";

        // Email
        if (StringUtils.hasText(dto.getEmail()) && !Pattern.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", dto.getEmail()))
            return "Некорректный email";

        // Дублирование: ИНН или ОГРН уже существуют (кроме текущей)
        List<Organization> existing = repository.findAll();
        for (Organization org : existing) {
            if (idForUpdate != null && org.getId_organization().equals(idForUpdate)) continue;
            if (dto.getInn() != null && dto.getInn().equals(org.getInn()))
                return "Организация с таким ИНН уже существует";
            if (StringUtils.hasText(dto.getOgrn()) && dto.getOgrn().equals(org.getOgrn()))
                return "Организация с таким ОГРН уже существует";
        }

        return null; // Всё ок
    }

    // ИНН: 10 или 12 цифр + контрольная сумма
    public static boolean isValidInn(String inn) {
        if (inn == null) return false;
        if (!inn.matches("^\\d{10}$") && !inn.matches("^\\d{12}$")) return false;
        if (inn.length() == 10) {
            int[] n = {2, 4, 10, 3, 5, 9, 4, 6, 8};
            int s = 0;
            for (int i = 0; i < 9; i++) s += Character.getNumericValue(inn.charAt(i)) * n[i];
            int k = (s % 11) % 10;
            return Character.getNumericValue(inn.charAt(9)) == k;
        } else if (inn.length() == 12) {
            int[] n1 = {7, 2, 4, 10, 3, 5, 9, 4, 6, 8, 0};
            int[] n2 = {3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8, 0};
            int s1 = 0, s2 = 0;
            for (int i = 0; i < 10; i++) s1 += Character.getNumericValue(inn.charAt(i)) * n1[i];
            for (int i = 0; i < 11; i++) s2 += Character.getNumericValue(inn.charAt(i)) * n2[i];
            int k1 = (s1 % 11) % 10;
            int k2 = (s2 % 11) % 10;
            return Character.getNumericValue(inn.charAt(10)) == k1 && Character.getNumericValue(inn.charAt(11)) == k2;
        }
        return false;
    }

    // ОГРН: 13 или 15 цифр + контрольная сумма
    public static boolean isValidOgrn(String ogrn) {
        if (ogrn == null) return false;
        if (!ogrn.matches("^\\d{13}$") && !ogrn.matches("^\\d{15}$")) return false;
        int n = ogrn.length() == 13 ? 12 : 14;
        try {
            String base = ogrn.substring(0, n);
            int control = Integer.parseInt(ogrn.substring(n));
            int mod = ogrn.length() == 13 ? 11 : 13;
            long baseNum = Long.parseLong(base);
            int k = (int)((baseNum % mod) % 10);
            return control == k;
        } catch (Exception e) {
            return false;
        }
    }
}