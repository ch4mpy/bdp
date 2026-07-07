package nc.sgcb.labs.commons.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Amount {

    @Column(nullable = false)
    private String currencyIso3;

    /**
     * In minor unit (i.e. 1000 for 1000 XPF, 10.00 USD, 1.000 KWD)
     */
    @Column(nullable = false)
    private Long amount;
}
