package esprit.tn.jungleevents.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

@Entity
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La date d'inscription est obligatoire")
    private LocalDate registrationDate;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    private ParticipationStatus status;

    @NotNull(message = "Le statut de paiement est obligatoire")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;


    @PositiveOrZero(message = "Le montant payé ne peut pas être négatif")
    private Double amountPaid;

    @NotNull(message = "La méthode de paiement est obligatoire")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;


    private Long userId;      // ← ID du User MS (le vrai, ex: 3)

    private String userEmail;


    @ManyToOne
    @NotNull(message = "La session est obligatoire")
    private Session session;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public ParticipationStatus getStatus() {
        return status;
    }

    public void setStatus(ParticipationStatus status) {
        this.status = status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
