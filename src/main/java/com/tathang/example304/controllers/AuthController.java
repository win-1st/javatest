package com.tathang.example304.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.tathang.example304.dto.LoginDto;
import com.tathang.example304.dto.RegisterDto;
import com.tathang.example304.model.ERole;
import com.tathang.example304.model.Role;
import com.tathang.example304.model.User;
import com.tathang.example304.payload.response.JwtResponse;
import com.tathang.example304.repository.RoleRepository;
import com.tathang.example304.repository.UserRepository;
import com.tathang.example304.security.jwt.JwtUtils;
import com.tathang.example304.security.services.UserDetailsImpl;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    // ✅ THÊM ENDPOINT GET ĐỂ TEST
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        System.out.println("=== AUTH TEST GET ENDPOINT CALLED ===");
        return ResponseEntity.ok("Auth API is working! GET request successful");
    }

    // ✅ THÊM ENDPOINT GET KHÁC
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Server is running! Auth controller is healthy.");
    }

    // ✅ THÊM SLASH VÀO ĐẦU CÁC POST MAPPING
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {
        System.out.println("=== REGISTER ===");
        System.out.println("Username: " + registerDto.getUsername());
        System.out.println("Email: " + registerDto.getEmail());
        System.out.println("Password: " + registerDto.getPassword());
        System.out.println("Requested Roles: " + registerDto.getRoles());

        if (userRepository.existsByUsername(registerDto.getUsername())) {
            return new ResponseEntity<>("Username is taken!", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            return new ResponseEntity<>("Email is already in use!", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Set<Role> roles = new HashSet<>();

        // XỬ LÝ ROLE THEO REQUEST
        if (registerDto.getRoles() == null || registerDto.getRoles().isEmpty()) {
            // Nếu không có role nào được chỉ định, mặc định là USER
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: USER Role is not found."));
            roles.add(userRole);
            System.out.println("Assigning default USER role");
        } else {
            // Xử lý các role được chỉ định
            for (String roleName : registerDto.getRoles()) {
                try {
                    // Chuyển đổi role name sang ERole enum
                    String roleEnumName = "ROLE_" + roleName.toUpperCase();
                    ERole roleEnum = ERole.valueOf(roleEnumName);

                    Role role = roleRepository.findByName(roleEnum)
                            .orElseThrow(() -> new RuntimeException("Error: Role " + roleName + " is not found."));
                    roles.add(role);
                    System.out.println("Assigning role: " + roleName);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid role requested: " + roleName);
                    return new ResponseEntity<>("Invalid role: " + roleName, HttpStatus.BAD_REQUEST);
                }
            }
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        System.out.println("User registered successfully with ID: " + savedUser.getId());
        System.out.println("Assigned roles: " + roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));
        return new ResponseEntity<>("User registered success!", HttpStatus.OK);
    }

    // ✅ THÊM SLASH VÀO ĐẦU
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        try {
            System.out.println("🔐 LOGIN ATTEMPT ==================================");
            System.out.println("Username: " + loginDto.getUsername());

            // 🆕 KIỂM TRA USER TỒN TẠI VÀ LẤY FULL NAME
            User user = userRepository.findByUsername(loginDto.getUsername())
                    .orElseThrow(() -> {
                        System.out.println("❌ USER NOT FOUND: " + loginDto.getUsername());
                        return new RuntimeException("User not found");
                    });

            System.out.println("✅ User found: " + user.getUsername());
            System.out.println("👤 Full Name: " + user.getFullName()); // 🆕 THÊM LOG FULL NAME
            System.out.println("👥 Roles count: " + user.getRoles().size());
            user.getRoles().forEach(role -> System.out.println("   - Role: " + role.getName()));

            // Authentication
            System.out.println("🔄 Attempting authentication...");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

            System.out.println("🎉 Authentication SUCCESS!");

            // Generate JWT
            System.out.println("🔑 Generating JWT token...");
            String jwt = jwtUtils.generateJwtToken(authentication);
            System.out.println("✅ JWT Token generated, length: " + jwt.length());

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            System.out.println("✅ Login successful! User: " + userDetails.getUsername());
            System.out.println("✅ Full Name: " + user.getFullName()); // 🆕 LOG FULL NAME
            System.out.println("✅ Roles: " + roles);
            System.out.println("==================================================");

            // 🆕 TRẢ VỀ FULL NAME TRONG RESPONSE - DÙNG CONSTRUCTOR MỚI
            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    user.getFullName(), // 🆕 THÊM FULL NAME
                    userDetails.getEmail(),
                    roles));

        } catch (Exception e) {
            System.out.println("❌ LOGIN FAILED: " + e.getMessage());
            e.printStackTrace();
            System.out.println("==================================================");
            return new ResponseEntity<>("Invalid username or password! Error: " + e.getMessage(),
                    HttpStatus.UNAUTHORIZED);
        }
    }
}