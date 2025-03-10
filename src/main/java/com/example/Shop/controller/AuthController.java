package com.example.Shop.controller;

import com.example.Shop.Service.JwtService;
import com.example.Shop.model.Role;
import com.example.Shop.model.User;
import com.example.Shop.repository.RoleRepository;
import com.example.Shop.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.BeanDefinitionDsl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;

    private RoleRepository roleRepository;

    public AuthController(UserRepository userRepository,
                          JwtService jwtService,
                          PasswordEncoder passwordEncoder,
                          RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user){
        Map<String, Object> response = new HashMap<>();
        var foundUser = userRepository.findByUsername(user.getUsername());
        if(foundUser.isPresent()){
            response.put("success",false);
            response.put("message", "Username is already exist");
            return ResponseEntity.badRequest().body(response);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Optional<Role> role = roleRepository.findByName("ROLE_USER");
        if(role.isEmpty()){
            Role newUserRole = new Role();
            newUserRole.setName("ROLE_USER");
            roleRepository.save(newUserRole);
            role=Optional.of(newUserRole);
        }

        user.setRoles(Set.of(role.get()));

        userRepository.save(user);
        response.put("success",true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User user, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        var foundUser = userRepository.findByUsername(user.getUsername());
        if (foundUser.isEmpty() || !passwordEncoder.matches(user.getPassword(), foundUser.get().getPassword())) {
            responseBody.put("success", false);
            responseBody.put("message", "Username or password is incorrect");
            return ResponseEntity.badRequest().body(responseBody);
        }

        String token = jwtService.generateJwtToken(foundUser.get());

        // Встановлюємо токен у cookie
        Cookie cookie = new Cookie("token", token);  // Створюємо cookie з токеном
        cookie.setHttpOnly(true);  // Встановлюємо HttpOnly для запобігання доступу до куки через JavaScript
        cookie.setSecure(true);  // Встановлюємо Secure для використання тільки через HTTPS (якщо на продакшн-сервері)
        cookie.setPath("/");  // Вказуємо шлях доступу до куки (можна зробити доступною для всіх сторінок)
        cookie.setMaxAge(60 * 60);  // Встановлюємо час життя куки (1 година)

        response.addCookie(cookie); // Додаємо куку до відповіді
        responseBody.put("success", true);
        responseBody.put("message", "Login Successful");
        responseBody.put("token", token);

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();

        // Видаляємо токен, знищуємо cookie
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);  // Термін дії куки встановлюємо на 0, щоб вона була видалена

        // Додаємо куку до відповіді
        response.addCookie(cookie);

        // Повертаємо відповідь із успіхом
        responseBody.put("success", true);
        responseBody.put("message", "Logout Successful");

        return ResponseEntity.ok(responseBody);
    }

}
