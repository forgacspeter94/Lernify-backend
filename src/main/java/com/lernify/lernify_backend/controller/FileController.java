package com.lernify.lernify_backend.controller;

import com.lernify.lernify_backend.model.FileItem;
import com.lernify.lernify_backend.model.Subject;
import com.lernify.lernify_backend.repository.FileRepository;
import com.lernify.lernify_backend.repository.SubjectRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "http://localhost:4200")
public class FileController {

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

    /** Add file (metadata only for now) */
    @PostMapping("/{subjectId}")
    public FileItem addFile(
            @PathVariable Long subjectId,
            @RequestBody Map<String, String> body) {

        Subject subject = subjectRepository.findById(subjectId).orElseThrow();

        FileItem file = new FileItem();
        file.setFilename(body.get("filename"));
        file.setFilePath(body.get("filePath"));
        file.setSubject(subject);

        return fileRepository.save(file);
    }

    /** Delete file */
    @DeleteMapping("/{fileId}")
    public void deleteFile(@PathVariable Long fileId) {
        fileRepository.deleteById(fileId);
    }
}
