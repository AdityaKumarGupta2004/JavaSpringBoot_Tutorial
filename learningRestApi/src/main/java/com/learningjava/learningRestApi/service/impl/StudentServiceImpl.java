package com.learningjava.learningRestApi.service.impl;

import com.learningjava.learningRestApi.dto.StudentDto;
import com.learningjava.learningRestApi.entity.Student;
import com.learningjava.learningRestApi.repository.StudentRepository;
import com.learningjava.learningRestApi.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService{

    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<StudentDto> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        return students
                .stream()
                .map(student -> modelMapper.map(student, StudentDto.class))
                .toList();
    }

    @Override
    public StudentDto getStudentByid(Long id) {

        Student student = studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Student not found with ID: "+id));
        return modelMapper.map(student, StudentDto.class);
    }




}
