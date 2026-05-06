package tn.esprit.administrationservice.service;

import tn.esprit.administrationservice.dto.SessionStatsDTO;
import tn.esprit.administrationservice.dto.TutorSessionStatsDTO;
import tn.esprit.administrationservice.dto.MonthlySessionStatsDTO;

import java.util.List;

public interface SessionStatisticsService {

  SessionStatsDTO getGlobalStatistics();

  List<TutorSessionStatsDTO> getSessionsCountByTutor();
  List<MonthlySessionStatsDTO> getSessionsCountByMonth();
}
