package plantime.ru.API.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для передачи данных о статусе оплаты.
 * Используется для передачи данных между слоями приложения и валидации входящего запроса.
 */
public class PaymentStatusDTO {

    /**
     * Уникальный идентификатор статуса оплаты.
     */
    private Integer idPaymentStatus;

    /**
     * Название статуса оплаты.
     */
    @NotBlank(message = "Название статуса оплаты обязательно для заполнения.")
    @Size(min = 2, max = 30, message = "Название статуса оплаты должно содержать от 2 до 30 символов.")
    private String status;

    /**
     * Конструктор по умолчанию.
     */
    public PaymentStatusDTO() {
    }

    /**
     * Конструктор с параметрами.
     *
     * @param idPaymentStatus Идентификатор статуса оплаты.
     * @param status          Название статуса оплаты.
     */
    public PaymentStatusDTO(Integer idPaymentStatus, String status) {
        this.idPaymentStatus = idPaymentStatus;
        this.status = status;
    }

    /**
     * Получить идентификатор статуса оплаты.
     *
     * @return idPaymentStatus Идентификатор статуса оплаты.
     */
    public Integer getIdPaymentStatus() {
        return idPaymentStatus;
    }

    /**
     * Установить идентификатор статуса оплаты.
     *
     * @param idPaymentStatus Идентификатор статуса оплаты.
     */
    public void setIdPaymentStatus(Integer idPaymentStatus) {
        this.idPaymentStatus = idPaymentStatus;
    }

    /**
     * Получить название статуса оплаты.
     *
     * @return status Название статуса оплаты.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Установить название статуса оплаты.
     *
     * @param status Название статуса оплаты.
     */
    public void setStatus(String status) {
        this.status = status;
    }
}