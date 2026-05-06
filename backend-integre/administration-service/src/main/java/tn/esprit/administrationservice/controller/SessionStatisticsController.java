package tn.esprit.administrationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import tn.esprit.administrationservice.dto.SessionStatsDTO;
import tn.esprit.administrationservice.dto.TutorSessionStatsDTO;
import tn.esprit.administrationservice.dto.MonthlySessionStatsDTO;
import tn.esprit.administrationservice.service.SessionStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sessions/statistics")
public class SessionStatisticsController {
  private final SessionStatisticsService sessionStatisticsService;

  public SessionStatisticsController(SessionStatisticsService sessionStatisticsService) {
    this.sessionStatisticsService = sessionStatisticsService;
  }

  @GetMapping("/global")
  public SessionStatsDTO getGlobalStatistics() {
    return sessionStatisticsService.getGlobalStatistics();
  }

  @GetMapping("/by-tutor")
  public List<TutorSessionStatsDTO> getStatisticsByTutor() {
    return sessionStatisticsService.getSessionsCountByTutor();
  }

  @GetMapping("/by-month")
  public List<MonthlySessionStatsDTO> getStatisticsByMonth() {
    return sessionStatisticsService.getSessionsCountByMonth();
  }
}
