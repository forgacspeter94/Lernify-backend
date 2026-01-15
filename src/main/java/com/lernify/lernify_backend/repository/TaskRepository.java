package com.lernify.lernify_backend.repository;

import com.lernify.lernify_backend.model.Task;
import com.lernify.lernify_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser(User user);
}