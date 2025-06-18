package plantime.ru.API.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plantime.ru.API.dto.ServiceDTO;
import plantime.ru.API.entity.Employee;
import plantime.ru.API.entity.ListServices;
import plantime.ru.API.entity.Log;
import plantime.ru.API.repository.LogRepository;
import plantime.ru.API.repository.ServiceRepository;
import plantime.ru.API.repository.ListServicesRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления услугами.
 * Обеспечивает бизнес-логику для CRUD-операций, включая проверку уникальности,
 * проверку схожести названия и логирование действий.
 */
@Service
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final LogRepository logRepository;
    private final ListServicesRepository listServicesRepository;
    private static final Logger logger = LoggerFactory.getLogger(ServiceService.class);

    public ServiceService(ServiceRepository serviceRepository, LogRepository logRepository, ListServicesRepository listServicesRepository) {
        this.serviceRepository = serviceRepository;
        this.logRepository = logRepository;
        this.listServicesRepository = listServicesRepository;
    }

    /**
     * Получает список всех услуг с фильтрацией по диапазону цены и сортировкой.
     *
     * @param minPrice Минимальная цена.
     * @param maxPrice Максимальная цена.
     * @param sortBy   Поле сортировки.
     * @param order    Направление сортировки ("asc" или "desc").
     * @return Список DTO услуг.
     */
    public List<ServiceDTO> getAllServices(BigDecimal minPrice, BigDecimal maxPrice, String sortBy, String order) {
        // Защита от некорректных полей сортировки (оставить только допустимые!)
        if (!"service".equalsIgnoreCase(sortBy) && !"price".equalsIgnoreCase(sortBy)) {
            sortBy = "service";
        }
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);

        List<plantime.ru.API.entity.Service> services =
                serviceRepository.findByPriceBetween(minPrice, maxPrice, sort);

        return services.stream()
                .map(s -> new ServiceDTO(s.getIdService(), s.getService(), s.getPrice(), s.getNote()))
                .collect(Collectors.toList());
    }

    /**
     * Создаёт новую услугу с проверкой уникальности и схожести названия.
     * Если найдено похожее название, требует подтверждения через forceCreate.
     *
     * @param serviceDTO   DTO с данными услуги.
     * @param authEmployee Аутентифицированный сотрудник.
     * @param forceCreate  Флаг подтверждения создания при схожести.
     * @return DTO созданной услуги.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public ServiceDTO createService(ServiceDTO serviceDTO, Employee authEmployee, Boolean forceCreate) {
        String name = serviceDTO.getService().trim();

        List<plantime.ru.API.entity.Service> allServices = serviceRepository.findAll();
        Optional<String> similarName = allServices.stream()
                .map(plantime.ru.API.entity.Service::getService)
                .filter(existingName -> stringSimilarity(existingName, name) >= 0.85)
                .findFirst();

        if (serviceRepository.existsByServiceIgnoreCase(name)) {
            logRepository.save(new Log(authEmployee, "Попытка создать дубликат услуги: " + name, LocalDateTime.now()));
            logger.warn("Дубликат услуги: {}", name);
            throw new IllegalArgumentException("Услуга с таким названием уже существует.");
        }

        if (similarName.isPresent()) {
            if (Boolean.TRUE.equals(forceCreate)) {
                logRepository.save(new Log(authEmployee, "Создана услуга с похожим названием: " + name + ", похоже на: " + similarName.get(), LocalDateTime.now()));
                logger.info("Создана услуга с похожим названием: {}, guid_employee={}", name, authEmployee.getGuidEmployee());
            } else {
                logRepository.save(new Log(authEmployee, "Попытка создать услугу с похожим названием: " + name + ", похоже на: " + similarName.get(), LocalDateTime.now()));
                logger.warn("Попытка создания услуги с похожим названием: {}, похоже на: {}, guid_employee={}", name, similarName.get(), authEmployee.getGuidEmployee());
                throw new IllegalArgumentException(
                        String.format("Обнаружено похожее название услуги: «%s». Если вы уверены, что хотите создать новую услугу.", similarName.get())
                );
            }
        }

        plantime.ru.API.entity.Service service = new plantime.ru.API.entity.Service();
        service.setService(name);
        service.setPrice(serviceDTO.getPrice());
        service.setNote(serviceDTO.getNote());
        plantime.ru.API.entity.Service saved = serviceRepository.save(service);
        logRepository.save(new Log(authEmployee, "Создана услуга: " + name, LocalDateTime.now()));
        logger.info("Создана услуга: {}", name);
        return new ServiceDTO(saved.getIdService(), saved.getService(), saved.getPrice(), saved.getNote());
    }

    /**
     * Обновляет услугу по id, с проверкой уникальности и схожести названия.
     * Если найдено похожее название, требует подтверждения через forceUpdate.
     *
     * @param id           Идентификатор услуги.
     * @param serviceDTO   DTO с новыми данными.
     * @param authEmployee Аутентифицированный сотрудник (для логирования).
     * @param forceUpdate  Флаг подтверждения обновления при схожести.
     * @return DTO обновлённой услуги.
     * @throws IllegalArgumentException В случае ошибок валидации или необходимости подтверждения.
     */
    @Transactional
    public ServiceDTO updateService(Integer id, ServiceDTO serviceDTO, Employee authEmployee, Boolean forceUpdate) {
        Optional<plantime.ru.API.entity.Service> existing = serviceRepository.findById(id);
        if (existing.isEmpty()) {
            logRepository.save(new Log(authEmployee, "Не удалось обновить услугу: id " + id + " не найден", LocalDateTime.now()));
            throw new IllegalArgumentException("Услуга с id " + id + " не найдена.");
        }

        plantime.ru.API.entity.Service service = existing.get();
        String name = serviceDTO.getService().trim();

        List<plantime.ru.API.entity.Service> allServices = serviceRepository.findAll();
        Optional<String> similarName = allServices.stream()
                .filter(s -> !s.getIdService().equals(id))
                .map(plantime.ru.API.entity.Service::getService)
                .filter(existingName -> stringSimilarity(existingName, name) >= 0.85)
                .findFirst();

        if (serviceRepository.existsByServiceIgnoreCase(name) && !service.getService().equalsIgnoreCase(name)) {
            logRepository.save(new Log(authEmployee, "Попытка обновить услугу на дубликат: " + name, LocalDateTime.now()));
            throw new IllegalArgumentException("Услуга с таким названием уже существует.");
        }

        if (similarName.isPresent()) {
            if (Boolean.TRUE.equals(forceUpdate)) {
                logRepository.save(new Log(authEmployee, "Обновлена услуга на похожее название: " + name + ", похоже на: " + similarName.get(), LocalDateTime.now()));
                logger.info("Обновлена услуга на похожее название: {}, guid_employee={}", name, authEmployee.getGuidEmployee());
            } else {
                logRepository.save(new Log(authEmployee, "Попытка обновить услугу на похожее название: " + name + ", похоже на: " + similarName.get(), LocalDateTime.now()));
                logger.warn("Попытка обновления услуги на похожее название: {}, похоже на: {}, guid_employee={}", name, similarName.get(), authEmployee.getGuidEmployee());
                throw new IllegalArgumentException(
                        String.format("Обнаружено похожее название услуги: «%s». Если вы уверены, что хотите обновить услугу до этого названия.", similarName.get())
                );
            }
        }

        service.setService(name);
        service.setPrice(serviceDTO.getPrice());
        service.setNote(serviceDTO.getNote());
        plantime.ru.API.entity.Service updated = serviceRepository.save(service);
        logRepository.save(new Log(authEmployee, "Обновлена услуга: " + name, LocalDateTime.now()));
        logger.info("Обновлена услуга: {}", name);
        return new ServiceDTO(updated.getIdService(), updated.getService(), updated.getPrice(), updated.getNote());
    }

    /**
     * Удаляет услугу по id.
     *
     * @param id           Идентификатор услуги.
     * @param authEmployee Аутентифицированный сотрудник (для логирования).
     * @throws IllegalArgumentException Если услуга не найдена или используется в списках услуг.
     */
    @Transactional
    public void deleteService(Integer id, Employee authEmployee) {
        // Проверяем существование услуги
        Optional<plantime.ru.API.entity.Service> existing = serviceRepository.findById(id);
        if (existing.isEmpty()) {
            logRepository.save(new Log(authEmployee,
                    "Не удалось удалить услугу: id " + id + " не найден",
                    LocalDateTime.now()));
            throw new IllegalArgumentException("Услуга с указанным идентификатором не найдена.");
        }

        // Проверяем, используется ли услуга в ListServices
        List<ListServices> listServices = listServicesRepository.findByIdService(id);
        if (!listServices.isEmpty()) {
            logRepository.save(new Log(authEmployee,
                    "Не удалось удалить услугу: id " + id + " используется в задачах",
                    LocalDateTime.now()));
            throw new IllegalArgumentException("Невозможно удалить услугу, так как она используется в задачах.");
        }

        // Если проверки пройдены - удаляем услугу
        serviceRepository.deleteById(id);
        logRepository.save(new Log(authEmployee,
                "Удалена услуга с id: " + id,
                LocalDateTime.now()));
        logger.info("Удалена услуга с id: {}", id);
    }

    /**
     * Возвращает степень схожести между двумя строками (от 0 до 1).
     * Используется алгоритм Левенштейна.
     *
     * @param s1 Первая строка.
     * @param s2 Вторая строка.
     * @return Коэффициент схожести.
     */
    public static double stringSimilarity(String s1, String s2) {
        s1 = s1.trim().toLowerCase();
        s2 = s2.trim().toLowerCase();
        if (s1.equals(s2)) return 1.0;
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        int dist = levenshteinDistance(s1, s2);
        return 1.0 - (double) dist / maxLen;
    }

    /**
     * Алгоритм Левенштейна — расстояние между строками.
     *
     * @param s1 Первая строка.
     * @param s2 Вторая строка.
     * @return Расстояние Левенштейна.
     */
    private static int levenshteinDistance(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= s2.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        s1.charAt(i - 1) == s2.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[s2.length()];
    }
}