package nc.sgcb.labs.card.payment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "payments")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CardPayment {

    @Id
    @GeneratedValue(generator = "cardPaymentSeq")
    @SequenceGenerator(name = "cardPaymentSeq", sequenceName = "payment_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private Instant timestamp;

    @Embedded
    private nc.sgcb.labs.commons.domain.Amount amount;

    @ManyToOne
    @JoinColumn(name = "card_number", nullable = false, updatable = false)
    private Card card;

    @Column(nullable = false)
    private String destAccountNumber;
}
