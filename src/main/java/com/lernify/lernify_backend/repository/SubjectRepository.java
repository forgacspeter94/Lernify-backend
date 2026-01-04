package com.lernify.lernify_backend.repository;

import com.lernify.lernify_backend.model.Subject;
import com.lernify.lernify_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByUser(User user);
}
