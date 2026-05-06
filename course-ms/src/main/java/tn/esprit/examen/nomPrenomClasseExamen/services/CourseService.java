package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.entities.Course;

import java.util.List;

public interface CourseService {

    Course createCourse(Course course);

    Course updateCourse(Long id, Course course);

    void deleteCourse(Long id);

    Course getCourseById(Long id);

    List<Course> getAllCourses();

    List<Course> getCoursesByLevel(String level);
}

