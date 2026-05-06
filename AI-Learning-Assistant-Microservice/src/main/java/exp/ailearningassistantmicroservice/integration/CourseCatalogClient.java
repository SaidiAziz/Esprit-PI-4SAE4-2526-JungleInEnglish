package exp.ailearningassistantmicroservice.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "COURSE-CATALOG-SERVICE", url = "${course-service.base-url:http://localhost:8089}")
public interface CourseCatalogClient {

    @GetMapping("/api/courses")
    List<CourseCatalogItem> getAllCourses();

    @GetMapping("/api/courses/{id}")
    CourseCatalogItem getCourseById(@PathVariable("id") Long id);

    @GetMapping("/api/courses/search")
    List<CourseCatalogItem> getCoursesByLevel(@RequestParam("level") String level);
}
