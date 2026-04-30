package exp.ailearningassistantmicroservice.service;

import exp.ailearningassistantmicroservice.entities.LearningPath;

import java.util.List;

public interface LearningPathService {

    LearningPath generateOrUpdate(Long userId, Long courseId);

    LearningPath generateOrUpdateForAdmin(Long userId, Long courseId);

    LearningPath updateProgress(Long id, Long userId, double progress);

    LearningPath getById(Long id);

    List<LearningPath> getByUserId(Long userId);

    LearningPath getByUserIdAndCourseId(Long userId, Long courseId);

    void delete(Long id, Long userId);
}
