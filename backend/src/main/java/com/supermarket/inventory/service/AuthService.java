package com.supermarket.inventory.service;

import com.supermarket.inventory.dto.req.LoginRequest;
import com.supermarket.inventory.dto.req.RegisterRequest;
import com.supermarket.inventory.dto.resp.AuthResponse;
import com.supermarket.inventory.dto.resp.UserResponse;
import com.supermarket.inventory.entity.Role;
import com.supermarket.inventory.entity.User;
import com.supermarket.inventory.entity.UserRole;
import com.supermarket.inventory.exception.BadRequestException;
import com.supermarket.inventory.exception.DuplicateResourceException;
import com.supermarket.inventory.exception.ResourceNotFoundException;
import com.supermarket.inventory.repository.RoleRepository;
import com.supermarket.inventory.repository.UserRepository;
import com.supermarket.inventory.security.JwtTokenProvider;
import com.supermarket.inventory.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email address is already registered: " + request.getEmail());
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                role
        );

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(userPrincipal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new AuthResponse(token, mapToUserResponse(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new BadRequestException("No authenticated user found in context");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userPrincipal.getId()));

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().getName(),
                user.getCreatedAt()
        );
    }
}
