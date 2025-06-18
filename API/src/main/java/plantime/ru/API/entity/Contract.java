package plantime.ru.API.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contract")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contract")
    private Integer idContract;

    @Column(name = "date_of_conclusion")
    private LocalDate dateOfConclusion;

    @Column(name = "contract_number", length = 100)
    private String contractNumber;

    @Column(name = "id_customer", nullable = false)
    private Integer idCustomer;

    @Column(name = "cost", precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "date_payment")
    private LocalDate datePayment;

    @Column(name = "path_file", length = 200)
    private String pathFile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_payment_status", nullable = false)
    private PaymentStatus paymentStatus;
}