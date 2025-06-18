package plantime.ru.API.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "software")
public class Software {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_software")
    private Integer idSoftware;

    @Column(name = "software", length = 120)
    private String software;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "logo", length = 255)
    private String logo;

    // Конструктор без параметров (нужен для JPA)
    public Software() {}

    // Конструктор со всеми параметрами
    public Software(Integer idSoftware, String software, String description, String logo) {
        this.idSoftware = idSoftware;
        this.software = software;
        this.description = description;
        this.logo = logo;
    }

    // Геттеры и сеттеры
    public Integer getIdSoftware() { return idSoftware; }
    public void setIdSoftware(Integer idSoftware) { this.idSoftware = idSoftware; }

    public String getSoftware() { return software; }
    public void setSoftware(String software) { this.software = software; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
}