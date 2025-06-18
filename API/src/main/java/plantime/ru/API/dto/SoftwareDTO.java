package plantime.ru.API.dto;

import plantime.ru.API.entity.Software;

public class SoftwareDTO {
    private Integer idSoftware;
    private String software;     // Название ПО
    private String description;  // Описание ПО
    private String logo;         // Логотип (путь/URL)

    public SoftwareDTO() {}

    public SoftwareDTO(Integer idSoftware, String software, String description, String logo) {
        this.idSoftware = idSoftware;
        this.software = software;
        this.description = description;
        this.logo = logo;
    }

    public static SoftwareDTO fromEntity(Software entity) {
        if (entity == null) return null;
        return new SoftwareDTO(
                entity.getIdSoftware(),
                entity.getSoftware(),
                entity.getDescription(),
                entity.getLogo()
        );
    }

    public Integer getIdSoftware() { return idSoftware; }
    public void setIdSoftware(Integer idSoftware) { this.idSoftware = idSoftware; }

    public String getSoftware() { return software; }
    public void setSoftware(String software) { this.software = software; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
}