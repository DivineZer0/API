package plantime.ru.API.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Сущность, представляющая статус оплаты в системе PlanTime.
 * Используется для хранения информации о статусах оплат.
 */
@Entity
@Table(name = "payment_status")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "idPaymentStatus")
public class PaymentStatus {

    /**
     * Уникальный идентификатор статуса оплаты.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_payment_status", nullable = false)
    private Integer idPaymentStatus;

    /**
     * Название статуса оплаты.
     */
    @Column(name = "status", nullable = false, length = 30)
    @NotNull(message = "Название статуса оплаты обязательно для заполнения.")
    @Size(min = 2, max = 30, message = "Название статуса оплаты должно содержать от 2 до 30 символов.")
    private String status;
}