package com.felipemarquesdev.bus_payment_manager.entities;

import com.felipemarquesdev.bus_payment_manager.dtos.payment.PaymentRequestDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "TB_PAYMENT")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String month;

    private String year;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "total_to_be_paid")
    private BigDecimal totalToBePaid;

    @Column(name = "tuition_amount")
    private BigDecimal tuitionAmount;

    @OneToMany(mappedBy = "payment")
    private List<FinancialHelp> financialHelps;

    @OneToMany(mappedBy = "payment", fetch = FetchType.LAZY)
    private List<Tuition> tuitions;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Payment(PaymentRequestDTO dto, BigDecimal totalToBePaid, BigDecimal tuitionAmount) {
        this.month = dto.month();
        this.year = dto.year();
        this.totalAmount = dto.totalAmount();
        this.totalToBePaid = totalToBePaid;
        this.tuitionAmount = tuitionAmount;
    }
}
