package com.lernify.lernify_backend.controller;

import com.lernify.lernify_backend.model.Subject;
import com.lernify.lernify_backend.model.User;
import com.lernify.lernify_backend.repository.SubjectRepository;
import com.lernify.lernify_backend.repository.UserRepository;
import com.lernify.lernify_backend.util.JwtUtil;
import io.jsonwebtoken.JwtException;
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

    /** M-9: Create subject */
    @PostMapping
    public Subject createSubject(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {

        String username = JwtUtil.validateTokenAndGetUsername(authHeader.substring(7));
        User user = userRepository.findByUsername(username).orElseThrow();

        Subject subject = new Subject();
        subject.setName(body.get("name"));
        subject.setUser(user);

        return subjectRepository.save(subject);
    }

    /** Dashboard: List subjects */
    @GetMapping
    public List<Subject> getSubjects(
            @RequestHeader("Authorization") String authHeader) {

        String username = JwtUtil.validateTokenAndGetUsername(authHeader.substring(7));
        User user = userRepository.findByUsername(username).orElseThrow();

        return subjectRepository.findByUser(user);
    }
}
