package com.c4soft.resthero.customer.domain;

import org.hibernate.envers.Audited;
import com.c4soft.resthero.commons.domain.Iban;
import com.c4soft.resthero.commons.jpa.IbanStringAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Audited
// LAB:3.1:REMOVE:START
@Entity
// LAB:3.1:REMOVE:END
@Table(name = "benficiaries",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"iban", "userId"}),
        @UniqueConstraint(columnNames = {"label", "userId"})})
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Beneficiary {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "beneficiarySeq")
  @SequenceGenerator(name = "beneficiarySeq", sequenceName = "benficiaries_seq", allocationSize = 1)
  @EqualsAndHashCode.Include
  @ToString.Include
  private Long id;

  @Column(nullable = false)
  @ToString.Include
  @Setter
  private String label;

  @Column(nullable = false)
  @Convert(converter = IbanStringAttributeConverter.class)
  @ToString.Include
  @Setter
  private Iban iban;

  @Column(nullable = false)
  private String customerId;

  public static Beneficiary of(String customerId, Iban iban, String label) {
    return Beneficiary.builder().label(label).iban(iban).customerId(customerId).build();
  }

}
