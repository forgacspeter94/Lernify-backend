package com.lernify.lernify_backend.controller;

import com.lernify.lernify_backend.model.FileItem;
import com.lernify.lernify_backend.model.Subject;
import com.lernify.lernify_backend.repository.FileRepository;
import com.lernify.lernify_backend.repository.SubjectRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "http://localhost:4200")
public class FileController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "doc", "docx", "ppt", "pptx", "txt", "jpg", "xlsx", "pdf"
    );

    private final FileRepository fileRepository;
    private final SubjectRepository subjectRepository;

    public FileController(FileRepository fileRepository, SubjectRepository subjectRepository) {
        this.fileRepository = fileRepository;
        this.subjectRepository = subjectRepository;
    }

    /** M-10: List files for subject */
    @GetMapping("/{subjectId}")
    public List<FileItem> getFiles(@PathVariable Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        return fileRepository.findBySubject(subject);
    }

    @PostMapping("/{subjectId}/upload")
    @Transactional   // ← this starts a transaction → fixes PostgreSQL LOB error
    public FileItem uploadFile(
            @PathVariable Long subjectId,
            @RequestParam("file") MultipartFile file) throws IOException {

        Subject subject = subjectRepository.findById(subjectId).orElseThrow();

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("Invalid file name");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("File type not allowed");
        }

        FileItem fileItem = new FileItem();
        fileItem.setFilename(originalFilename);
        fileItem.setContent(file.getBytes());  // assuming you switched to byte[]
        fileItem.setSubject(subject);

        return fileRepository.save(fileItem);
    }
  
    /** Delete file */
    @DeleteMapping("/{fileId}")
    public void deleteFile(@PathVariable Long fileId) {
        fileRepository.deleteById(fileId);
    }
}
