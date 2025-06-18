package plantime.ru.API.dto;

import lombok.Data;

@Data
public class OrganizationDetailsDTO {
    private Integer id_organization;
    private String short_name;
    private String long_name;
    private String inn;
    private String kpp;
    private String ogrn;
    private String email;
    private String address;
    private String note;
    private String phone_number;
}