package com.counterfeit.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class DatabaseConfigurationTest {

    @Test
    void databaseCredentialsRemainRequiredWhenEnvironmentValuesAreMissing() throws IOException {
        Properties properties = loadApplicationProperties();
        MockEnvironment environment = new MockEnvironment();

        assertThrows(
                IllegalArgumentException.class,
                () -> environment.resolveRequiredPlaceholders(
                        properties.getProperty("spring.datasource.username")));
        assertThrows(
                IllegalArgumentException.class,
                () -> environment.resolveRequiredPlaceholders(
                        properties.getProperty("spring.datasource.password")));
    }

    @Test
    void databaseCredentialsResolveFromExplicitEnvironmentValues() throws IOException {
        Properties properties = loadApplicationProperties();
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DB_USERNAME", "countercheck-test")
                .withProperty("DB_PASSWORD", "test-only-password");

        assertEquals(
                "countercheck-test",
                environment.resolveRequiredPlaceholders(
                        properties.getProperty("spring.datasource.username")));
        assertEquals(
                "test-only-password",
                environment.resolveRequiredPlaceholders(
                        properties.getProperty("spring.datasource.password")));
    }

    private Properties loadApplicationProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            assertNotNull(input, "application.properties must be available on the test classpath");
            properties.load(input);
        }
        return properties;
    }
}
