package org.orbitalLogistic.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@Configuration
@EnableJdbcRepositories(basePackages = "org.orbitalLogistic.repositories")
public class JdbcConfig {
}