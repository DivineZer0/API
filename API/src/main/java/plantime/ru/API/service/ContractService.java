package plantime.ru.API.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import plantime.ru.API.entity.Contract;
import plantime.ru.API.entity.PaymentStatus;
import plantime.ru.API.dto.ContractDTO;
import plantime.ru.API.repository.ContractRepository;
import plantime.ru.API.repository.PaymentStatusRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContractService {
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private PaymentStatusRepository paymentStatusRepository;

    // Получить все контракты с фильтрами
    public List<ContractDTO> getContracts(
            Integer customerId,
            Integer paymentStatusId,
            Integer organizationId, // Не реализован, добавьте при необходимости
            LocalDate dateFrom,
            LocalDate dateTo,
            LocalDate paymentDateFrom,
            LocalDate paymentDateTo,
            BigDecimal minCost,
            BigDecimal maxCost
    ) {
        List<Contract> contracts = contractRepository.findAll();
        return contracts.stream()
                .filter(c -> customerId == null || c.getIdCustomer().equals(customerId))
                .filter(c -> paymentStatusId == null || (c.getPaymentStatus() != null && c.getPaymentStatus().getIdPaymentStatus().equals(paymentStatusId)))
                // organizationId фильтруется через кастомное поле, если оно есть
                .filter(c -> dateFrom == null || (c.getDateOfConclusion() != null && !c.getDateOfConclusion().isBefore(dateFrom)))
                .filter(c -> dateTo == null || (c.getDateOfConclusion() != null && !c.getDateOfConclusion().isAfter(dateTo)))
                .filter(c -> paymentDateFrom == null || (c.getDatePayment() != null && !c.getDatePayment().isBefore(paymentDateFrom)))
                .filter(c -> paymentDateTo == null || (c.getDatePayment() != null && !c.getDatePayment().isAfter(paymentDateTo)))
                .filter(c -> minCost == null || (c.getCost() != null && c.getCost().compareTo(minCost) >= 0))
                .filter(c -> maxCost == null || (c.getCost() != null && c.getCost().compareTo(maxCost) <= 0))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Получить контракт по id
    public ContractDTO getContract(Integer id) {
        return contractRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    // Создать контракт
    public ContractDTO createContract(ContractDTO dto, Object authEmployee) {
        Contract entity = fromDTO(dto);
        if (dto.getIdPaymentStatus() != null) {
            PaymentStatus ps = paymentStatusRepository.findById(dto.getIdPaymentStatus()).orElse(null);
            entity.setPaymentStatus(ps);
        } else {
            entity.setPaymentStatus(null);
        }
        Contract saved = contractRepository.save(entity);
        return toDTO(saved);
    }

    // Обновить контракт
    public ContractDTO updateContract(Integer id, ContractDTO dto, Object authEmployee) {
        Contract entity = fromDTO(dto);
        entity.setIdContract(id); // гарантируем обновление по id
        if (dto.getIdPaymentStatus() != null) {
            PaymentStatus ps = paymentStatusRepository.findById(dto.getIdPaymentStatus()).orElse(null);
            entity.setPaymentStatus(ps);
        } else {
            entity.setPaymentStatus(null);
        }
        Contract updated = contractRepository.save(entity);
        return toDTO(updated);
    }

    // Удалить контракт
    public void deleteContract(Integer id, Object authEmployee) {
        contractRepository.deleteById(id);
    }

    // Преобразование в DTO
    public ContractDTO toDTO(Contract c) {
        ContractDTO dto = new ContractDTO();
        dto.setIdContract(c.getIdContract());
        dto.setDateOfConclusion(c.getDateOfConclusion() == null ? null : c.getDateOfConclusion().toString());
        dto.setContractNumber(c.getContractNumber());
        dto.setIdCustomer(c.getIdCustomer());
        dto.setCost(c.getCost());
        dto.setDatePayment(c.getDatePayment() == null ? null : c.getDatePayment().toString());
        dto.setPathFile(c.getPathFile());
        dto.setIdPaymentStatus(c.getPaymentStatus() == null ? null : c.getPaymentStatus().getIdPaymentStatus());
        return dto;
    }

    // Преобразование из DTO в Entity
    public Contract fromDTO(ContractDTO dto) {
        Contract c = new Contract();
        c.setIdContract(dto.getIdContract());
        c.setDateOfConclusion(dto.getDateOfConclusion() == null ? null : java.time.LocalDate.parse(dto.getDateOfConclusion()));
        c.setContractNumber(dto.getContractNumber());
        c.setIdCustomer(dto.getIdCustomer());
        c.setCost(dto.getCost());
        c.setDatePayment(dto.getDatePayment() == null ? null : java.time.LocalDate.parse(dto.getDatePayment()));
        c.setPathFile(dto.getPathFile());
        // PaymentStatus связывается отдельно (см. выше)
        return c;
    }
}