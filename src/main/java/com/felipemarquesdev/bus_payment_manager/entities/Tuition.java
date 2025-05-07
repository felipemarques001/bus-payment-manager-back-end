package com.felipemarquesdev.bus_payment_manager.entities;

import com.felipemarquesdev.bus_payment_manager.enums.PaymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "TB_TUITION")
public class Tuition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "is_paid")
    private Boolean isPaid;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    public Tuition(Payment payment, Student student) {
        this.paymentType = null;
        this.isPaid = false;
        this.paidAt = null;
        this.payment = payment;
        this.student = student;
    }
}
