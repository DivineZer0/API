package plantime.ru.API.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "service")
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_service")
    private Integer idService;

    @Column(name = "service", length = 120)
    private String service;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "note", length = 255)
    private String note;

    // getters and setters
    public Integer getIdService() { return idService; }
    public void setIdService(Integer idService) { this.idService = idService; }

    public String getService() { return service; }
    public void setService(String name) { this.service = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}