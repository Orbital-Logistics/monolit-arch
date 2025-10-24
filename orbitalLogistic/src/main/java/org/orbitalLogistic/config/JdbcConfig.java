package org.orbitalLogistic.config;

import org.orbitalLogistic.config.converters.EnumToStringConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;

import java.util.List;
import java.util.*;
import javax.sql.DataSource;

@Configuration
@EnableJdbcRepositories(
    basePackages = "org.orbitalLogistic.repositories",
    jdbcOperationsRef = "namedParameterJdbcOperations"
)
public class JdbcConfig extends AbstractJdbcConfiguration {

    @Bean
    public NamedParameterJdbcOperations namedParameterJdbcOperations(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public TransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Override
    protected List<?> userConverters() {
        return Arrays.asList(
            // Spacecraft
            new EnumToStringConverter.SpacecraftStatusEnumToString(),
            new EnumToStringConverter.StringToSpacecraftStatusEnum(),
            // new EnumToStringConverter.SpacecraftClassificationEnumToString(),
            // new EnumToStringConverter.StringToSpacecraftClassificationEnum(),

            // Storage
            new EnumToStringConverter.StorageTypeEnumToString(),
            new EnumToStringConverter.StringToStorageTypeEnum(),

            // Cargo
            new EnumToStringConverter.CargoTypeEnumToString(),
            new EnumToStringConverter.StringToCargoTypeEnum(),
            new EnumToStringConverter.HazardLevelEnumToString(),
            new EnumToStringConverter.StringToHazardLevelEnum(),

            // Mission
            new EnumToStringConverter.MissionTypeEnumToString(),
            new EnumToStringConverter.StringToMissionTypeEnum(),
            new EnumToStringConverter.MissionStatusEnumToString(),
            new EnumToStringConverter.StringToMissionStatusEnum()
            // new EnumToStringConverter.MissionPriorityEnumToString(),
            // new EnumToStringConverter.StringToMissionPriorityEnum(),

            // // Assignment
            // new EnumToStringConverter.AssignmentRoleEnumToString(),
            // new EnumToStringConverter.StringToAssignmentRoleEnum(),

            // // Maintenance
            // new EnumToStringConverter.MaintenanceTypeEnumToString(),
            // new EnumToStringConverter.StringToMaintenanceTypeEnum(),
            // new EnumToStringConverter.MaintenanceStatusEnumToString(),
            // new EnumToStringConverter.StringToMaintenanceStatusEnum(),

            // // Transaction
            // new EnumToStringConverter.TransactionTypeEnumToString(),
            // new EnumToStringConverter.StringToTransactionTypeEnum(),

            // // Manifest
            // new EnumToStringConverter.ManifestStatusEnumToString(),
            // new EnumToStringConverter.StringToManifestStatusEnum(),
            // new EnumToStringConverter.ManifestPriorityEnumToString(),
            // new EnumToStringConverter.StringToManifestPriorityEnum()
        );
    }
}