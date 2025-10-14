package org.orbitalLogistic;

import org.junit.jupiter.api.Test;
import org.orbitalLogistic.entities.*;
import org.orbitalLogistic.entities.enums.*;
import org.orbitalLogistic.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IntegrationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_db");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private UserRepository userRepository;
    @Autowired private SpacecraftRepository spacecraftRepository;
    @Autowired private StorageUnitRepository storageUnitRepository;

    @Test
    void basicRepositoryOperations_ShouldWork() {
        List<User> users = (List<User>) userRepository.findAll();
        List<Spacecraft> spacecrafts = (List<Spacecraft>) spacecraftRepository.findAll();
        List<StorageUnit> storageUnits = (List<StorageUnit>) storageUnitRepository.findAll();

        assertFalse(users.isEmpty());
        assertFalse(spacecrafts.isEmpty());
        assertFalse(storageUnits.isEmpty());
    }

    @Test
    void spacecraftRepository_FindAvailable_ShouldWork() {
        List<Spacecraft> available = spacecraftRepository.findAvailableForMission();

        assertNotNull(available);
    }
}