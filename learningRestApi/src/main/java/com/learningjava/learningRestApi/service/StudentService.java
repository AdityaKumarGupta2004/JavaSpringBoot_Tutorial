package com.learningjava.learningRestApi.service;

import com.learningjava.learningRestApi.dto.StudentDto;

import java.util.List;

public interface StudentService {
    List<StudentDto> getAllStudents();

    StudentDto getStudentByid(Long id);
}
