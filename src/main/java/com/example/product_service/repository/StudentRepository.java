package com.example.product_service.repository;

import com.example.product_service.entity.Student;
import lombok.NoArgsConstructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends CrudRepository<Student, String> {}
