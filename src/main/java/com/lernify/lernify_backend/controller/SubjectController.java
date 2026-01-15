package com.lernify.lernify_backend.controller;

import com.lernify.lernify_backend.model.Subject;
import com.lernify.lernify_backend.model.User;
import com.lernify.lernify_backend.repository.SubjectRepository;
import com.lernify.lernify_backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/subjects")
@CrossOrigin(origins = "http://localhost:4200")
public class SubjectController {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public SubjectController(SubjectRepository subjectRepository, UserRepository userRepository) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    /** Create subject */
    @PostMapping
    public Subject createSubject(@RequestBody Map<String, String> body) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username).orElseThrow();

        Subject subject = new Subject();
        subject.setName(body.get("name"));
        subject.setUser(user);

        return subjectRepository.save(subject);
    }

    /** List subjects */
    @GetMapping
    public List<Subject> getSubjects() {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username).orElseThrow();

        return subjectRepository.findByUser(user);
    }

    // Helper method to get current username from SecurityContext
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}