package com.faculty.controller;

import com.faculty.model.Course;
import model.dao.CourseDAO;
import java.util.List;

public class CourseController {
    private final CourseDAO courseDAO = new CourseDAO();

    public List<Course> getAllCourses() {
        return courseDAO.getAllCourses();
    }

    public List<Course> getCoursesByStudentUserId(int userId) {
        return courseDAO.getCoursesByStudentUserId(userId);
    }

    public List<Course> getCoursesByLecturerId(int lecturerId) {
        return courseDAO.getCoursesByLecturerId(lecturerId);
    }

    public void saveBatch(List<Course> addedList, List<Course> editedList, List<Course> deletedList) throws Exception {
        for (Course c : addedList) courseDAO.addCourse(c);
        for (Course c : editedList) courseDAO.updateCourse(c);
        for (Course c : deletedList) courseDAO.deleteCourse(c.getCourseCode());
    }

    public List<Course> getCoursesByYearAndSemester(int year, int semester) {
        return courseDAO.getCoursesByYearAndSemester(year, semester);
    }
    
    public List<Course> getCoursesByDegreeYearAndSemester(int degreeId, int year, int semester) {
        return courseDAO.getCoursesByDegreeYearAndSemester(degreeId, year, semester);
    }

    public void enrollStudent(int studentId, int courseId) throws Exception {
        courseDAO.enrollStudent(studentId, courseId);
    }
}





