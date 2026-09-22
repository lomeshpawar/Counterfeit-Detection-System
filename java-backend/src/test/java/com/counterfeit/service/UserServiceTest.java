package com.counterfeit.service;

import com.counterfeit.dto.RegisterRequest;
import com.counterfeit.entity.User;
import com.counterfeit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_hashesPasswordAndSavesUser() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("secret123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed-password");

        User saved = new User();
        saved.setName(request.getName());
        saved.setEmail(request.getEmail());
        saved.setPassword("hashed-password");
        saved.setRole("USER");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.registerUser(request);

        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("hashed-password", result.getPassword());
        assertEquals("USER", result.getRole());
        verify(passwordEncoder).encode("secret123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_rejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Existing User");
        request.setEmail("existing@example.com");
        request.setPassword("secret123");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(new User()));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.registerUser(request)
        );

        assertEquals("Email already registered!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }
}
