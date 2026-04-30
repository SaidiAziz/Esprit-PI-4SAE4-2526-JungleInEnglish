package tn.esprit.administrationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.administrationservice.dto.SessionStatsDTO;
import tn.esprit.administrationservice.dto.TutorSessionStatsDTO;
import tn.esprit.administrationservice.entity.SessionStatus;
import tn.esprit.administrationservice.repository.SessionRepository;
import tn.esprit.administrationservice.dto.MonthlySessionStatsDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionStatisticsServiceImpl implements SessionStatisticsService {

  private final SessionRepository sessionRepository;

  @Override
  public SessionStatsDTO getGlobalStatistics() {
    long total = sessionRepository.countAllSessions();
    long planned = sessionRepository.countByStatus(SessionStatus.PLANNED);
    long completed = sessionRepository.countByStatus(SessionStatus.COMPLETED);
    long canceled = sessionRepository.countByStatus(SessionStatus.CANCELED);

    double completionRate = 0.0;
    if (total > 0) {
      completionRate = (completed * 100.0) / total;
    }

    return new SessionStatsDTO(total, planned, completed, canceled, completionRate);
  }

  @Override
  public List<TutorSessionStatsDTO> getSessionsCountByTutor() {
    return sessionRepository.countSessionsByTutor();
  }

  @Override
  public List<MonthlySessionStatsDTO> getSessionsCountByMonth() {
    return sessionRepository.countSessionsByMonth();
  }
}
