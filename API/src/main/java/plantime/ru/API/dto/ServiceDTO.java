package plantime.ru.API.dto;

import java.math.BigDecimal;

public class ServiceDTO {
    private Integer idListServices;
    private Integer idService;
    private String service;
    private Integer count;
    private BigDecimal price;

    private String note;

    public ServiceDTO() {}

    // Конструктор для TaskService (с количеством)
    public ServiceDTO(Integer idListServices, Integer idService, String name, Integer count, BigDecimal price) {
        this.idListServices = idListServices;
        this.idService = idService;
        this.service = name;
        this.count = count;
        this.price = price;
    }

    // Если нужен другой конструктор (по вашей старой логике)
    public ServiceDTO(Integer idService, String name, BigDecimal price, String note) {
        this.idService = idService;
        this.service = name;
        this.price = price;
        // note не используется в TaskService, но можете добавить поле если нужно
    }

    public Integer getIdListServices() { return idListServices; }
    public void setIdListServices(Integer idListServices) { this.idListServices = idListServices; }

    public Integer getIdService() { return idService; }
    public void setIdService(Integer idService) { this.idService = idService; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}