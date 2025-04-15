package com.example.post.controller;

import com.example.post.entity.User;
import com.example.post.repository.UserRepository;
import com.example.post.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TempUserController {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/join")
    public void join(String nation, String language, String address, String name) {
        User user = new User();
        user.setNation(nation);
        user.setLanguage(language);
        user.setAddress(address);
        user.setName(name);
        user.setRole("ROLE_USER");
        userRepository.save(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(long userId, HttpServletResponse res) {
        User user = userRepository.findById(userId).get();
        String accessToken = jwtUtil.createToken(userId, user.getRole(), "access");
        res.addHeader("access-token", accessToken);
        return ResponseEntity.ok("");
    }
}
