package exp.ailearningassistantmicroservice.service;

import exp.ailearningassistantmicroservice.dto.response.RecommendationResponse;
import exp.ailearningassistantmicroservice.entities.Recommendation;
import exp.ailearningassistantmicroservice.entities.RecommendationType;
import exp.ailearningassistantmicroservice.entities.SkillType;
import exp.ailearningassistantmicroservice.integration.CourseCatalogItem;
import exp.ailearningassistantmicroservice.mapper.AiMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationResponseAssemblerTest {

    @Mock
    private AiMapper aiMapper;

    @Mock
    private CourseCatalogLookupService courseCatalogLookupService;

    @InjectMocks
    private RecommendationResponseAssembler recommendationResponseAssembler;

    @Test
    void convertsRecommendationListUsingCourseCatalogLookup() {
        Recommendation first = recommendation(10L, 100L);
        Recommendation second = recommendation(11L, 101L);

        CourseCatalogItem firstCourse = new CourseCatalogItem();
        firstCourse.setId(100L);
        CourseCatalogItem secondCourse = new CourseCatalogItem();
        secondCourse.setId(101L);

        RecommendationResponse firstResponse = RecommendationResponse.builder().id(10L).build();
        RecommendationResponse secondResponse = RecommendationResponse.builder().id(11L).build();

        when(courseCatalogLookupService.getCourseMap(List.of(100L, 101L)))
                .thenReturn(Map.of(100L, firstCourse, 101L, secondCourse));
        when(aiMapper.toResponse(first, firstCourse)).thenReturn(firstResponse);
        when(aiMapper.toResponse(second, secondCourse)).thenReturn(secondResponse);

        List<RecommendationResponse> result = recommendationResponseAssembler.toResponses(List.of(first, second));

        assertEquals(List.of(firstResponse, secondResponse), result);
    }

    @Test
    void convertsSingleRecommendationUsingSingleItemLookup() {
        Recommendation recommendation = recommendation(12L, 102L);
        CourseCatalogItem course = new CourseCatalogItem();
        course.setId(102L);
        RecommendationResponse response = RecommendationResponse.builder().id(12L).build();

        when(courseCatalogLookupService.getCourseMap(List.of(102L))).thenReturn(Map.of(102L, course));
        when(aiMapper.toResponse(recommendation, course)).thenReturn(response);

        RecommendationResponse result = recommendationResponseAssembler.toResponse(recommendation);

        assertEquals(response, result);
        verify(aiMapper).toResponse(recommendation, course);
        verify(courseCatalogLookupService).getCourseMap(List.of(102L));
    }

    private Recommendation recommendation(Long id, Long contentId) {
        return Recommendation.builder()
                .id(id)
                .userId(1L)
                .type(RecommendationType.COURSE)
                .contentId(contentId)
                .reason("Focus on speaking")
                .focusSkill(SkillType.SPEAKING)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
