package com.lernify.lernify_backend.repository;

import com.lernify.lernify_backend.model.FileItem;
import com.lernify.lernify_backend.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<FileItem, Long> {
    List<FileItem> findBySubject(Subject subject);
}
