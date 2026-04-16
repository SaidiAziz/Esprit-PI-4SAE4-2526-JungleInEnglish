package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Course;
import tn.esprit.examen.nomPrenomClasseExamen.exceptions.CourseNotFoundException;
import tn.esprit.examen.nomPrenomClasseExamen.repositories.CourseRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public Course createCourse(Course course) {
        if (course.getActive() == null) {
            course.setActive(Boolean.TRUE);
        }
        return courseRepository.save(course);
    }

    @Override
    public Course updateCourse(Long id, Course course) {
        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));

        existing.setTitle(course.getTitle());
        existing.setLevel(course.getLevel());
        existing.setDescription(course.getDescription());
        existing.setDurationHours(course.getDurationHours());
        existing.setStartDate(course.getStartDate());
        existing.setEndDate(course.getEndDate());
        existing.setPrice(course.getPrice());
        existing.setMaxStudents(course.getMaxStudents());
        existing.setActive(course.getActive());

        return courseRepository.save(existing);
    }

    @Override
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new CourseNotFoundException(id);
        }
        courseRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getCoursesByLevel(String level) {
        return courseRepository.findByLevelIgnoreCase(level);
    }
}

