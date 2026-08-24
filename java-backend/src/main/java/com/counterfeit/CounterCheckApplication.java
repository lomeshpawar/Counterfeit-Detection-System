package com.counterfeit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main entry point for the CounterCheck Spring Boot Application.
 * 
 * @SpringBootApplication is a convenience annotation that adds:
 * - @Configuration: Tags the class as a source of bean definitions.
 * - @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings.
 * - @ComponentScan: Tells Spring to look for other components, configurations, and services in the 'com.counterfeit' package.
 */
@SpringBootApplication
public class CounterCheckApplication {

    public static void main(String[] args) {
        // Launches the Spring Boot application
        SpringApplication.run(CounterCheckApplication.class, args);
    }
}
