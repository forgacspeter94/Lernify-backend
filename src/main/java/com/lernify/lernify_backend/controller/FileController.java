package com.lernify.lernify_backend.controller;

import com.lernify.lernify_backend.model.FileItem;
import com.lernify.lernify_backend.model.Subject;
import com.lernify.lernify_backend.repository.FileRepository;
import com.lernify.lernify_backend.repository.SubjectRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "http://localhost:4200")
public class FileController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "doc", "docx", "ppt", "pptx", "txt", "jpg", "jpeg", "png", "xlsx", "xls", "pdf"
    );

    private final FileRepository fileRepository;
    private final SubjectRepository subjectRepository;

    public FileController(FileRepository fileRepository, SubjectRepository subjectRepository) {
        this.fileRepository = fileRepository;
        this.subjectRepository = subjectRepository;
    }

    /** List files for subject */
    @GetMapping("/{subjectId}")
    public List<FileItem> getFiles(@PathVariable Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        return fileRepository.findBySubject(subject);
    }

    /** Download file */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        Optional<FileItem> fileOpt = fileRepository.findById(fileId);
        
        if (fileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        FileItem file = fileOpt.get();
        
        HttpHeaders headers = new HttpHeaders();
        
        // Set content type based on file extension
        String contentType = determineContentType(file.getFilename());
        headers.setContentType(MediaType.parseMediaType(contentType));
        
        // Set content disposition to attachment to force download
        headers.setContentDispositionFormData("attachment", file.getFilename());
        
        // Set content length
        headers.setContentLength(file.getContent().length);
        
        return new ResponseEntity<>(file.getContent(), headers, HttpStatus.OK);
    }

    /** Upload file with validation */
    @PostMapping("/{subjectId}/upload")
    @Transactional
    public ResponseEntity<?> uploadFile(
            @PathVariable Long subjectId,
            @RequestParam("file") MultipartFile file) {

        try {
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new RuntimeException("Subject not found"));

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid file name"));
            }

            // Extract file extension
            String extension = "";
            int lastDotIndex = originalFilename.lastIndexOf('.');
            if (lastDotIndex > 0) {
                extension = originalFilename.substring(lastDotIndex + 1).toLowerCase();
            }

            // Validate file type
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(Map.of(
                                "error", "File type not supported",
                                "message", "Allowed file types: " + String.join(", ", ALLOWED_EXTENSIONS),
                                "uploadedType", extension
                        ));
            }

            // Check file size (e.g., max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(Map.of(
                                "error", "File too large",
                                "message", "Maximum file size is 10MB"
                        ));
            }

            FileItem fileItem = new FileItem();
            fileItem.setFilename(originalFilename);
            fileItem.setContent(file.getBytes());
            fileItem.setSubject(subject);

            FileItem savedFile = fileRepository.save(fileItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFile);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }

    /** Rename file */
    @PutMapping("/{fileId}/rename")
    public ResponseEntity<?> renameFile(
            @PathVariable Long fileId,
            @RequestBody Map<String, String> body) {

        String newFilename = body.get("filename");
        if (newFilename == null || newFilename.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Filename cannot be empty"));
        }

        Optional<FileItem> fileOpt = fileRepository.findById(fileId);
        
        if (fileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "File not found"));
        }

        FileItem file = fileOpt.get();
        file.setFilename(newFilename.trim());
        FileItem updated = fileRepository.save(file);
        
        return ResponseEntity.ok(updated);
    }

    /** Delete file */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId) {
        Optional<FileItem> fileOpt = fileRepository.findById(fileId);
        
        if (fileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "File not found"));
        }

        fileRepository.delete(fileOpt.get());
        return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
    }

    /**
     * Determine content type based on file extension
     */
    private String determineContentType(String filename) {
        String extension = "";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = filename.substring(lastDotIndex + 1).toLowerCase();
        }

        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt" -> "text/plain";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            default -> "application/octet-stream";
        };
    }
}