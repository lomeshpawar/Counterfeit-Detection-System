package com.counterfeit.bootstrap;

import com.counterfeit.entity.User;
import com.counterfeit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {

    private static final String NAME = "Initial Admin";
    private static final String EMAIL = "admin@example.com";
    private static final String PASSWORD = "a-unique-bootstrap-password";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void doesNothingWhenBootstrapIsDisabled() {
        AdminBootstrapRunner runner = runner(false, "", "", "");

        runner.run(null);

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void doesNotOverwriteAnExistingAdministrator() {
        when(userRepository.countByRole("ADMIN")).thenReturn(1L);
        AdminBootstrapRunner runner = runner(true, "", "", "");

        runner.run(null);

        verify(userRepository).countByRole("ADMIN");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void createsFirstAdministratorWithEncodedPassword() {
        when(userRepository.countByRole("ADMIN")).thenReturn(0L);
        when(passwordEncoder.encode(PASSWORD)).thenReturn("encoded-password");
        AdminBootstrapRunner runner = runner(true, NAME, EMAIL, PASSWORD);

        runner.run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals(NAME, saved.getName());
        assertEquals(EMAIL, saved.getEmail());
        assertEquals("ADMIN", saved.getRole());
        assertEquals("encoded-password", saved.getPassword());
        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    void rejectsMissingBootstrapValuesWhenNoAdministratorExists() {
        when(userRepository.countByRole("ADMIN")).thenReturn(0L);
        AdminBootstrapRunner runner = runner(true, NAME, "", PASSWORD);

        assertThrows(IllegalStateException.class, () -> runner.run(null));

        verify(userRepository).countByRole("ADMIN");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void rejectsShortBootstrapPassword() {
        when(userRepository.countByRole("ADMIN")).thenReturn(0L);
        AdminBootstrapRunner runner = runner(true, NAME, EMAIL, "short-password");

        assertThrows(IllegalStateException.class, () -> runner.run(null));

        verify(userRepository).countByRole("ADMIN");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    private AdminBootstrapRunner runner(boolean enabled, String name, String email, String password) {
        return new AdminBootstrapRunner(
                userRepository,
                passwordEncoder,
                enabled,
                name,
                email,
                password
        );
    }
}
