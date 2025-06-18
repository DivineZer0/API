package plantime.ru.API.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ContractDTO {

    private Integer idContract;

    @NotNull(message = "Дата заключения обязательна")
    private String dateOfConclusion;

    @Size(max = 100, message = "Номер контракта не более 100 символов")
    private String contractNumber;

    @NotNull(message = "Клиент обязателен")
    private Integer idCustomer;

    @DecimalMin(value = "0.00", message = "Стоимость должна быть неотрицательной")
    private BigDecimal cost;

    private String datePayment;

    @Size(max = 200, message = "Путь к файлу не более 200 символов")
    private String pathFile;

    @NotNull(message = "Статус оплаты обязателен")
    private Integer idPaymentStatus;

    // Геттеры и сеттеры

    public Integer getIdContract() { return idContract; }
    public void setIdContract(Integer idContract) { this.idContract = idContract; }

    public String getDateOfConclusion() { return dateOfConclusion; }
    public void setDateOfConclusion(String dateOfConclusion) { this.dateOfConclusion = dateOfConclusion; }

    public String getContractNumber() { return contractNumber; }
    public void setContractNumber(String contractNumber) { this.contractNumber = contractNumber; }

    public Integer getIdCustomer() { return idCustomer; }
    public void setIdCustomer(Integer idCustomer) { this.idCustomer = idCustomer; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }

    public String getDatePayment() { return datePayment; }
    public void setDatePayment(String datePayment) { this.datePayment = datePayment; }

    public String getPathFile() { return pathFile; }
    public void setPathFile(String pathFile) { this.pathFile = pathFile; }

    public Integer getIdPaymentStatus() { return idPaymentStatus; }
    public void setIdPaymentStatus(Integer idPaymentStatus) { this.idPaymentStatus = idPaymentStatus; }
}