package nc.sgcb.labs.account.domain;

import jakarta.persistence.*;
import lombok.*;
import nc.sgcb.labs.commons.domain.Amount;

@Entity
@Table(name = "transfers")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MoneyTransfer {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private String number;

    @Column(nullable = false)
    @ToString.Include
    private String fromAccountNumber;

    @Column(nullable = false)
    @ToString.Include
    private String toAccountNumber;

    @Embedded
    private Amount amount;

    private String label;
}
