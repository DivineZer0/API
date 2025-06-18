package plantime.ru.API.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Сущность, представляющая токен для сброса пароля в системе PlanTime.
 */
@Entity
@Table(name = "password_reset_token")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "token")
public class PasswordResetToken {

    /**
     * Уникальный токен для сброса пароля.
     */
    @Id
    @Column(name = "token", length = 6, nullable = false)
    @NotNull(message = "Токен обязателен")
    @Size(max = 6, message = "Токен должен содержать 6 символов")
    private String token;

    /**
     * Сотрудник, связанный с токеном.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guid_employee", nullable = false)
    @NotNull(message = "Сущность сотрудника обязательна")
    private Employee employee;

    /**
     * Дата и время истечения срока действия токена.
     */
    @Column(name = "expires_at", nullable = false)
    @NotNull(message = "Дата истечения срока действия обязательна")
    private LocalDateTime expiresAt;
}