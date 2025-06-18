package plantime.ru.API.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Сущность, представляющая сессию пользователя в системе PlanTime.
 */
@Entity
@Table(name = "session")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "token")
public class Session {

    /**
     * Уникальный JWT-токен сессии.
     */
    @Id
    @Column(name = "token", length = 255, nullable = false)
    @NotNull(message = "Токен обязателен")
    @Size(max = 255, message = "Токен не должен превышать 255 символов")
    private String token;

    /**
     * Дата и время истечения срока действия сессии.
     */
    @Column(name = "expires_at", nullable = false)
    @NotNull(message = "Дата истечения обязательна")
    private LocalDateTime expiresAt;

    /**
     * Сотрудник, связанный с сессией.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guid_employee", nullable = false)
    @NotNull(message = "Сущность сотрудника обязательна")
    private Employee employee;
}