package nc.sgcb.labs.customer.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "customers")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(generator = "customerSeq")
    @SequenceGenerator(name = "customerSeq", sequenceName = "customers_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(nullable = false, name="firstname")
    @ToString.Include
    private String firstName;

    @Column(nullable = false, name="lastname")
    @ToString.Include
    private String lastName;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private String birthLocation;

}