package plantime.ru.API.dto;

import lombok.Data;

@Data
public class OrganizationDTO {
    private Integer id_organization;
    private String short_name;
    private String long_name;
    private String inn;
    private String email;
    private String phone_number;
}