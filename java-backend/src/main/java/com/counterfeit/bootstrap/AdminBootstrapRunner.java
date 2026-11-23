package com.counterfeit.bootstrap;

import com.counterfeit.entity.User;
import com.counterfeit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Creates the first administrator only when an operator explicitly enables
 * bootstrap and supplies credentials through the runtime environment.
 *
 * Disable the bootstrap flag and remove the password variable after setup.
 */
@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final boolean enabled;
    private final String name;
    private final String email;

    public AdminBootstrapRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Environment environment,
            @Value("${app.admin-bootstrap.enabled:false}") boolean enabled,
            @Value("${app.admin-bootstrap.name:}") String name,
            @Value("${app.admin-bootstrap.email:}") String email) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.enabled = enabled;
        this.name = name;
        this.email = email;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        if (userRepository.countByRole("ADMIN") > 0) {
            return;
        }

        // Read the secret only when bootstrap is actually required. Do not keep
        // an additional plaintext password reference in this singleton bean.
        String password = environment.getProperty("app.admin-bootstrap.password");
        if (!StringUtils.hasText(name)
                || !StringUtils.hasText(email)
                || !StringUtils.hasText(password)) {
            throw new IllegalStateException(
                    "Administrator bootstrap is enabled but required ADMIN_BOOTSTRAP_* values are missing.");
        }

        if (password.length() < 16) {
            throw new IllegalStateException(
                    "ADMIN_BOOTSTRAP_PASSWORD must contain at least 16 characters.");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException(
                    "ADMIN_BOOTSTRAP_EMAIL already belongs to an existing account. Choose an unused email address.");
        }

        User admin = new User();
        admin.setName(name.trim());
        admin.setEmail(normalizedEmail);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole("ADMIN");

        userRepository.save(admin);
    }
}
