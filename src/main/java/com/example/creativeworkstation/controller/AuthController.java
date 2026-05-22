package com.example.creativeworkstation.controller;

import com.example.creativeworkstation.entity.User;
import com.example.creativeworkstation.repository.UserRepository;
import com.example.creativeworkstation.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "账号名和密码不能为空"));
        }

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "账号名已存在"));
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 实际生产环境应该加密
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "注册成功"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpSession session) {
        String username = request.get("username");
        String password = request.get("password");

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null || !user.getPassword().equals(password)) {
            return ResponseEntity.badRequest().body(Map.of("error", "账号名或密码错误"));
        }

        SessionUtil.setCurrentUser(session, user.getId());
        return ResponseEntity.ok(Map.of("message", "登录成功", "userId", user.getId(), "username", user.getUsername()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        SessionUtil.logout(session);
        return ResponseEntity.ok(Map.of("message", "退出成功"));
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAuth(HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        User user = userRepository.findById(SessionUtil.getCurrentUserId(session)).orElse(null);
        return ResponseEntity.ok(Map.of("userId", user.getId(), "username", user.getUsername()));
    }
}
