package nc.sgcb.labs.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nc.sgcb.labs.commons.domain.Iban;

@Entity
@Table(name = "benficiaries",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"iban", "userId"}),
        @UniqueConstraint(columnNames = {"label", "userId"})})
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Beneficiary {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "beneficiarySeq")
  @SequenceGenerator(name = "beneficiarySeq", sequenceName = "benficiaries_seq", allocationSize = 1)
  @EqualsAndHashCode.Include
  @ToString.Include
  private Long id;

  @Column(nullable = false)
  @ToString.Include
  private String label;

  @Column(nullable = false)
  @ToString.Include
  private Iban iban;

  @Column(nullable = false)
  private String customerId;

}
