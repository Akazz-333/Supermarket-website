package com.supermarket.inventory.service;

import com.supermarket.inventory.dto.req.LoginRequest;
import com.supermarket.inventory.dto.req.RegisterRequest;
import com.supermarket.inventory.dto.resp.AuthResponse;
import com.supermarket.inventory.dto.resp.UserResponse;
import com.supermarket.inventory.entity.Role;
import com.supermarket.inventory.entity.User;
import com.supermarket.inventory.entity.UserRole;
import com.supermarket.inventory.exception.DuplicateResourceException;
import com.supermarket.inventory.repository.RoleRepository;
import com.supermarket.inventory.repository.UserRepository;
import com.supermarket.inventory.security.JwtTokenProvider;
import com.supermarket.inventory.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private Role staffRole;
    private User user;

    @BeforeEach
    void setUp() {
        staffRole = new Role(1L, UserRole.STAFF);
        user = new User("staff@example.com", "encodedPassword", "Staff Member", staffRole);
        user.setId(5L);
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("Staff Member", "staff@example.com", "Password123!", UserRole.STAFF);

        when(userRepository.existsByEmail("staff@example.com")).thenReturn(false);
        when(roleRepository.findByName(UserRole.STAFF)).thenReturn(Optional.of(staffRole));
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("staff@example.com", response.getEmail());
        assertEquals(UserRole.STAFF, response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest request = new RegisterRequest("Staff Member", "staff@example.com", "Password123!", UserRole.STAFF);

        when(userRepository.existsByEmail("staff@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("staff@example.com", "Password123!");
        Authentication authentication = mock(Authentication.class);
        UserPrincipal principal = UserPrincipal.create(user);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt.token.here");
        when(userRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("staff@example.com", response.getUser().getEmail());
    }
}
