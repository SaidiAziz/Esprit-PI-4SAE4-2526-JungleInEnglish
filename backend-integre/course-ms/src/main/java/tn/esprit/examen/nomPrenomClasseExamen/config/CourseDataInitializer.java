package tn.esprit.examen.nomPrenomClasseExamen.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Course;
import tn.esprit.examen.nomPrenomClasseExamen.repositories.CourseRepository;

import java.time.LocalDate;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CourseDataInitializer {

    private final CourseRepository courseRepository;

    @Bean
    CommandLineRunner seedCourses() {
        return args -> {
            if (courseRepository.count() > 0) {
                return;
            }

            List<Course> courses = List.of(
                    Course.builder()
                            .title("English Grammar Foundations")
                            .level("BEGINNER")
                            .description("Build sentence structure, tenses, and grammar confidence through guided lessons.")
                            .durationHours(24)
                            .startDate(LocalDate.now().plusDays(7))
                            .endDate(LocalDate.now().plusDays(67))
                            .price(0.0)
                            .maxStudents(30)
                            .active(true)
                            .build(),
                    Course.builder()
                            .title("Listening Essentials Lab")
                            .level("BEGINNER")
                            .description("Improve listening comprehension with audio drills, dictation, and slow-paced conversations.")
                            .durationHours(18)
                            .startDate(LocalDate.now().plusDays(10))
                            .endDate(LocalDate.now().plusDays(55))
                            .price(0.0)
                            .maxStudents(25)
                            .active(true)
                            .build(),
                    Course.builder()
                            .title("Speaking Confidence Workshop")
                            .level("INTERMEDIATE")
                            .description("Practice oral fluency, conversation rhythm, and pronunciation in interactive speaking sessions.")
                            .durationHours(30)
                            .startDate(LocalDate.now().plusDays(5))
                            .endDate(LocalDate.now().plusDays(75))
                            .price(25.0)
                            .maxStudents(20)
                            .active(true)
                            .build(),
                    Course.builder()
                            .title("Intermediate Listening and Conversation")
                            .level("INTERMEDIATE")
                            .description("Combine active listening exercises with guided conversation tasks for better comprehension.")
                            .durationHours(28)
                            .startDate(LocalDate.now().plusDays(12))
                            .endDate(LocalDate.now().plusDays(82))
                            .price(20.0)
                            .maxStudents(24)
                            .active(true)
                            .build(),
                    Course.builder()
                            .title("Advanced Pronunciation and Fluency")
                            .level("ADVANCED")
                            .description("Fine-tune speaking fluency, pronunciation precision, and real-world oral performance.")
                            .durationHours(32)
                            .startDate(LocalDate.now().plusDays(14))
                            .endDate(LocalDate.now().plusDays(84))
                            .price(40.0)
                            .maxStudents(18)
                            .active(true)
                            .build(),
                    Course.builder()
                            .title("Advanced Grammar and Writing Mastery")
                            .level("ADVANCED")
                            .description("Master complex grammar patterns, cohesion, and high-level written expression.")
                            .durationHours(26)
                            .startDate(LocalDate.now().plusDays(9))
                            .endDate(LocalDate.now().plusDays(69))
                            .price(35.0)
                            .maxStudents(18)
                            .active(true)
                            .build()
            );

            courseRepository.saveAll(courses);
        };
    }
}
