package exp.ailearningassistantmicroservice.service;

import exp.ailearningassistantmicroservice.entities.SkillType;
import exp.ailearningassistantmicroservice.integration.CourseCatalogItem;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CourseCatalogLookupService {

    List<CourseCatalogItem> findRecommendedCourses(String level, SkillType focusSkill, Long preferredCourseId, int limit);

    Map<Long, CourseCatalogItem> getCourseMap(Collection<Long> courseIds);
}
