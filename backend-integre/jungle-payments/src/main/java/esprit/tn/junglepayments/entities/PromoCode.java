package esprit.tn.junglepayments.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "promo_code")
@Data
public class PromoCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    private PromoCodeType type;

    private Double value;

    private LocalDate expirationDate;

    private Integer maxUses;

    private Integer usesCount = 0;

    private Long eventId;

    private Double minAmount;

    // null = code global/admin ; non-null = code fidélité personnel
    private String ownerEmail;
}
