package esprit.tn.jungleevents.services;

import esprit.tn.jungleevents.DTO.ParticipationDTO;
import esprit.tn.jungleevents.DTO.PaymentDTO;
import esprit.tn.jungleevents.DTO.UserDTO;
import esprit.tn.jungleevents.entities.Participation;
import esprit.tn.jungleevents.entities.ParticipationStatus;
import esprit.tn.jungleevents.entities.Session;
import esprit.tn.jungleevents.repositories.ParticipationRepository;
import esprit.tn.jungleevents.repositories.PaymentFeignClient;
import esprit.tn.jungleevents.repositories.SessionRepository;
import esprit.tn.jungleevents.repositories.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParticipationServiceImpl implements ParticipationService {

    private final ParticipationRepository participationRepository;
    private final SessionRepository sessionRepository;

    @Autowired
    private PaymentFeignClient paymentFeignClient;

    @Autowired
    private UserFeignClient userFeignClient; // ✅ NOUVEAU




    public ParticipationServiceImpl(ParticipationRepository participationRepository,
                                    SessionRepository sessionRepository
                                    ) {
        this.participationRepository = participationRepository;
        this.sessionRepository = sessionRepository;

    }

    // ========================= ADD =========================

    @Override
    public ParticipationDTO addParticipation(ParticipationDTO dto) {

        // ✅ ÉTAPE 1 : Résoudre le vrai userId depuis User MS via email
        String email = dto.getUserEmail();
        Long resolvedUserId = dto.getUserId();
        String resolvedUserName = dto.getUserName();

        if (email != null && !email.isEmpty()) {
            try {
                UserDTO userFromMS = userFeignClient.getUserByEmail(email);
                resolvedUserId = userFromMS.getId();       // ← id=3 (le vrai)
                resolvedUserName = userFromMS.getFirstName() + " " + userFromMS.getLastName();
                System.out.println("✅ User résolu depuis User MS : id=" + resolvedUserId);
            } catch (Exception e) {
                System.err.println("⚠️ User MS injoignable : " + e.getMessage());
                // on continue avec le userId du DTO si fourni
            }
        }

        // ✅ ÉTAPE 2 : Vérifier doublon avec le vrai userId
        boolean alreadyExists = participationRepository
                .existsByUserIdAndSessionId(resolvedUserId, dto.getSessionId());
        if (alreadyExists) {
            throw new RuntimeException("Utilisateur déjà inscrit à cette session");
        }

        // ✅ ÉTAPE 3 : Récupérer la session
        Session session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        if (session.getAvailableSeats() <= 0) {
            throw new RuntimeException("Plus de places disponibles");
        }

        session.setAvailableSeats(session.getAvailableSeats() - 1);
        sessionRepository.save(session);

        // ✅ ÉTAPE 4 : Créer la participation avec le vrai userId
        Participation participation = new Participation();
        participation.setUserId(resolvedUserId);      // ← id=3 ✓
        participation.setUserEmail(email);            // ← stocker l'email aussi
        participation.setSession(session);
        participation.setRegistrationDate(dto.getRegistrationDate());
        participation.setStatus(dto.getStatus() != null ?
                dto.getStatus() : ParticipationStatus.REGISTERED);
        participation.setPaymentStatus(dto.getPaymentStatus());
        participation.setAmountPaid(dto.getAmountPaid());
        participation.setPaymentMethod(dto.getPaymentMethod());

        Participation saved = participationRepository.save(participation);

        // ✅ ÉTAPE 5 : Si paiement en espèces ou virement, créer un enregistrement dans jungle-payments
        if (dto.getPaymentMethod() == esprit.tn.jungleevents.entities.PaymentMethod.CASH ||
            dto.getPaymentMethod() == esprit.tn.jungleevents.entities.PaymentMethod.BANK_TRANSFER) {
            PaymentDTO paymentData = new PaymentDTO();
            paymentData.setEventId(session.getEvent().getId());
            paymentData.setParticipantEmail(dto.getUserEmail());
            paymentData.setAmount(dto.getAmountPaid());
            paymentData.setPaymentMethod(dto.getPaymentMethod());
            paymentData.setStatus(dto.getPaymentStatus());
            paymentData.setParticipantId(resolvedUserId);
            paymentData.setSessionId(dto.getSessionId());
            paymentData.setParticipantName(resolvedUserName);

            try {
                paymentFeignClient.createPayment(paymentData);
                System.out.println("✅ Payment record created in jungle-payments for " + dto.getPaymentMethod());
            } catch (Exception e) {
                System.err.println("⚠️ Failed to create payment record: " + e.getMessage());
            }
        }

        return mapToDTO(saved, resolvedUserName);
    }

    // ========================= GET ALL =========================

    @Override
    public List<ParticipationDTO> getAllParticipations() {
        return participationRepository.findAll()
                .stream()
                .map(p -> mapToDTO(p, null))
                .toList();
    }

    // ========================= GET BY SESSION =========================

    @Override
    public List<ParticipationDTO> getBySession(Long sessionId) {
        return participationRepository.findBySessionId(sessionId)
                .stream()
                .map(p -> mapToDTO(p, null))
                .toList();
    }

    // ========================= DELETE =========================

    @Override
    public void deleteParticipation(Long id) {
        participationRepository.deleteById(id);
    }

    // ========================= UPDATE =========================

    @Override
    public ParticipationDTO updateParticipation(Long id, ParticipationDTO dto) {
        Participation existing = participationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participation introuvable"));

        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        if (dto.getPaymentStatus() != null) existing.setPaymentStatus(dto.getPaymentStatus());
        if (dto.getAmountPaid() != null) existing.setAmountPaid(dto.getAmountPaid());
        if (dto.getPaymentMethod() != null) existing.setPaymentMethod(dto.getPaymentMethod());
        if (dto.getUserEmail() != null) existing.setUserEmail(dto.getUserEmail());
        if (dto.getUserId() != null) existing.setUserId(dto.getUserId());

        if (dto.getSessionId() != null) {
            Session session = sessionRepository.findById(dto.getSessionId())
                    .orElseThrow(() -> new RuntimeException("Session introuvable"));
            existing.setSession(session);
        }

        return mapToDTO(participationRepository.save(existing), null);
    }


    // ========================= GET BY ID =========================

    @Override
    public ParticipationDTO getParticipationById(Long id) {
        Participation p = participationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participation introuvable : " + id));
        return mapToDTO(p, null);
    }

    @Override
    public ParticipationDTO getByEmailAndEventId(String email, Long eventId) {
        // ✅ plus besoin de chercher dans UserRepository local
        Participation participation = participationRepository
                .findByUserEmailAndSessionEventId(email, eventId)
                .orElseThrow(() -> new RuntimeException(
                        "Participation introuvable pour email=" + email));
        return mapToDTO(participation, null);
    }

    // ========================= MAPPER =========================

    private ParticipationDTO mapToDTO(Participation p, String userName) {
        ParticipationDTO dto = new ParticipationDTO();
        dto.setId(p.getId());
        dto.setRegistrationDate(p.getRegistrationDate());
        dto.setStatus(p.getStatus());
        dto.setPaymentStatus(p.getPaymentStatus());
        dto.setAmountPaid(p.getAmountPaid());
        dto.setPaymentMethod(p.getPaymentMethod());
        dto.setUserId(p.getUserId());           // ✅ champ simple
        dto.setUserEmail(p.getUserEmail());     // ✅ champ simple
        dto.setSessionId(p.getSession().getId());
        dto.setUserName(userName);              // ✅ vient du User MS

        if (p.getSession() != null && p.getSession().getEvent() != null) {
            dto.setEventTitle(p.getSession().getEvent().getTitle());
        }
        return dto;
    }
}

