package plantime.ru.API.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "organization")
@Data
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_organization;

    @Column(length = 40)
    private String short_name;

    @Column(length = 120)
    private String long_name;

    @Column(length = 12)
    private String inn;

    @Column(length = 9)
    private String kpp;

    @Column(length = 15)
    private String ogrn;

    @Column(length = 120)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(length = 16)
    private String phone_number;
}