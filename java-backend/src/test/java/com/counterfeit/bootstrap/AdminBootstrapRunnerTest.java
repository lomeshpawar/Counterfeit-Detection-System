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
    void createsFirstAdministratorWithEncodedPasswordAndNormalizedEmail() {
        when(userRepository.countByRole("ADMIN")).thenReturn(0L);
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn("encoded-password");
        AdminBootstrapRunner runner = runner(true, NAME, " Admin@Example.com ", PASSWORD);

        runner.run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals(NAME, saved.getName());
        assertEquals(EMAIL, saved.getEmail());
        assertEquals("ADMIN", saved.getRole());
        assertEquals("encoded-password", saved.getPassword());
        verify(userRepository).existsByEmail(EMAIL);
        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    void rejectsBootstrapEmailAlreadyUsedByAnExistingUser() {
        when(userRepository.countByRole("ADMIN")).thenReturn(0L);
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);
        AdminBootstrapRunner runner = runner(true, NAME, EMAIL, PASSWORD);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> runner.run(null));

        assertEquals(
                "ADMIN_BOOTSTRAP_EMAIL already belongs to an existing account. Choose an unused email address.",
                exception.getMessage());
        verify(userRepository).existsByEmail(EMAIL);
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
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
