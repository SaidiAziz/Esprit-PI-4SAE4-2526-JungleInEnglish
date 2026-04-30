package esprit.tn.jungleevents.services;

import esprit.tn.jungleevents.DTO.ParticipationDTO;

import java.util.List;


public interface ParticipationService {

    ParticipationDTO addParticipation(ParticipationDTO dto);

    List<ParticipationDTO> getAllParticipations();

    List<ParticipationDTO> getBySession(Long sessionId);

    void deleteParticipation(Long id);

    ParticipationDTO updateParticipation(Long id, ParticipationDTO dto);

    ParticipationDTO getParticipationById(Long id);
    ParticipationDTO getByEmailAndEventId(String email, Long eventId);
}
