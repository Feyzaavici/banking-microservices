package org.example.transactionservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.transactionservice.enums.TransactionStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 26)
    private String senderIban;

    @Column(nullable = false, length = 26)
    private String receiverIban;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        if (status == null){
            status = TransactionStatus.PENDING;
        }
    }
}
