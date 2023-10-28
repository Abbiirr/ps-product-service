package com.example.product_service.service;

import com.example.product_service.dto.StudentDto;
import com.example.product_service.entity.Student;
import com.example.product_service.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class HomeService {

    private final StudentRepository studentRepository;

    public HomeService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAllData() {
        Iterable<Student> studentIterable = studentRepository.findAll();
        return StreamSupport.stream(studentIterable.spliterator(), false)
                .collect(Collectors.toList());
    }


    public String getData(String studentId) {
        Optional<Student> optionalStudent = studentRepository.findById(studentId);

        if (optionalStudent.isPresent()) {
            Student student = optionalStudent.get();
            // You can access the student data here
            return "Student ID: " + student.getId() + ", Name: " + student.getName();
        } else {
            return "Student not found";
        }
    }

    public String makeData(StudentDto studentDto) {
        Student student = new Student(studentDto.getId(), studentDto.getName(),
                Student.Gender.valueOf(studentDto.getGender()), studentDto.getAge());
        studentRepository.save(student);
        return "Student data created successfully";
    }
}
