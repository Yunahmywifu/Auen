package com.example.lab1.controller;

import com.example.lab1.model.PendingRegistration;
import com.example.lab1.model.User;
import com.example.lab1.repository.UserRepository;
import com.example.lab1.service.CustomUserDetailsService;
import com.example.lab1.service.EmailService;
import com.example.lab1.service.PendingRegistrationStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Controller
@RequestMapping("/auth")
public class GoogleRegistrationController {

    private static final Logger log = LoggerFactory.getLogger(GoogleRegistrationController.class);

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String supabaseAnonKey;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final PendingRegistrationStore pendingStore;
    private final EmailService emailService;

    public GoogleRegistrationController(UserRepository userRepository,
                                         PasswordEncoder passwordEncoder,
                                         CustomUserDetailsService userDetailsService,
                                         PendingRegistrationStore pendingStore,
                                         EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.pendingStore = pendingStore;
        this.emailService = emailService;
    }

    @GetMapping("/google")
    public String googleAuth(HttpServletRequest request) {
        String redirectUrl = supabaseUrl + "/auth/v1/authorize?provider=google&redirect_to="
                + getBaseUrl(request) + "/auth/fragment";
        return "redirect:" + redirectUrl;
    }

    @GetMapping("/fragment")
    public String fragmentPage() {
        return "auth-fragment";
    }

    private String getBaseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName()
                + (request.getServerPort() != 80 && request.getServerPort() != 443
                ? ":" + request.getServerPort() : "");
    }

    @GetMapping("/token")
    public String handleToken(@RequestParam(value = "access_token", required = false) String accessToken,
                               HttpServletRequest request) {
        if (accessToken == null || accessToken.isBlank()) {
            return "redirect:/login?error";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("apikey", supabaseAnonKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                    supabaseUrl + "/auth/v1/user",
                    HttpMethod.GET,
                    entity,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                return "redirect:/login?error";
            }

            String email = (String) userResponse.getBody().get("email");
            if (email == null || email.isBlank()) {
                return "redirect:/login?error";
            }

            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                loginUser(existingUser.get().getUsername(), request);
                return "redirect:/index";
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("oauth_email", email);
            return "redirect:/auth/setup";

        } catch (Exception ex) {
            log.error("Ошибка /auth/token: {}", ex.getMessage(), ex);
            return "redirect:/login?error";
        }
    }

    @GetMapping("/setup")
    public String setupPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("oauth_email");
        if (email == null) {
            return "redirect:/login";
        }
        model.addAttribute("email", email);
        return "auth-setup";
    }

    @PostMapping("/setup")
    public String handleSetup(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               HttpSession session,
                               Model model) {

        String email = (String) session.getAttribute("oauth_email");
        if (email == null) {
            return "redirect:/login";
        }

        model.addAttribute("email", email);

        if (username == null || username.trim().length() < 3) {
            model.addAttribute("error", "Имя пользователя должно содержать минимум 3 символа");
            return "auth-setup";
        }
        if (userRepository.findByUsername(username.trim()).isPresent()) {
            model.addAttribute("error", "Это имя пользователя уже занято");
            return "auth-setup";
        }

        if (password == null || password.length() < 8) {
            model.addAttribute("error", "Пароль должен содержать минимум 8 символов");
            return "auth-setup";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают");
            return "auth-setup";
        }

        String code = String.valueOf(100000 + new Random().nextInt(900000));

        PendingRegistration pending = new PendingRegistration(
                email,
                username.trim(),
                passwordEncoder.encode(password),
                code,
                LocalDateTime.now().plusMinutes(10)
        );
        pendingStore.save(pending);

        try {
            emailService.sendConfirmationCode(email, code);
        } catch (Exception ex) {
            log.error("Не удалось отправить email: {}", ex.getMessage(), ex);
            model.addAttribute("error", "Не удалось отправить письмо. Попробуйте ещё раз.");
            return "auth-setup";
        }

        session.setAttribute("oauth_email", email);
        return "redirect:/auth/confirm";
    }

    @GetMapping("/confirm")
    public String confirmPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("oauth_email");
        if (email == null) {
            return "redirect:/login";
        }
        model.addAttribute("email", email);
        return "auth-confirm";
    }

    @PostMapping("/confirm")
    public String handleConfirm(@RequestParam String code,
                                 HttpSession session,
                                 HttpServletRequest request,
                                 Model model) {

        String email = (String) session.getAttribute("oauth_email");
        if (email == null) {
            return "redirect:/login";
        }

        model.addAttribute("email", email);

        PendingRegistration pending = pendingStore.get(email);

        if (pending == null) {
            model.addAttribute("error", "Сессия истекла. Пройдите регистрацию заново.");
            return "auth-confirm";
        }
        if (!pending.getConfirmationCode().equals(code.trim())) {
            model.addAttribute("error", "Неверный код. Попробуйте ещё раз.");
            return "auth-confirm";
        }
        if (pending.isExpired()) {
            pendingStore.remove(email);
            model.addAttribute("error", "Код истёк. Пройдите регистрацию заново.");
            return "auth-confirm";
        }

        User user = new User();
        user.setUsername(pending.getUsername());
        user.setPassword(pending.getEncodedPassword());
        user.setRole("ROLE_USER");
        user.setEmail(pending.getEmail());
        userRepository.save(user);

        loginUser(pending.getUsername(), request);

        pendingStore.remove(email);
        session.removeAttribute("oauth_email");

        return "redirect:/index";
    }

    private void loginUser(String username, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext);
    }
}
