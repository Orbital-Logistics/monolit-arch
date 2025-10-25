package org.orbitalLogistic.config.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.orbitalLogistic.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.entities.enums.*;

public class EnumToStringConverter {

    @WritingConverter
    public static class SpacecraftStatusEnumToString implements Converter<SpacecraftStatus, String> {
        @Override
        public String convert(SpacecraftStatus source) {
            return source != null ? source.name() : null;
        }
    }

    @ReadingConverter
    public static class StringToSpacecraftStatusEnum implements Converter<String, SpacecraftStatus> {
        @Override
        public SpacecraftStatus convert(String source) {
            return source != null ? SpacecraftStatus.valueOf(source) : null;
        }
    }

    @WritingConverter
    public static class StorageTypeEnumToString implements Converter<StorageTypeEnum, String> {
        @Override
        public String convert(StorageTypeEnum source) {
            return source != null ? source.name() : null;
        }
    }

    @ReadingConverter
    public static class StringToStorageTypeEnum implements Converter<String, StorageTypeEnum> {
        @Override
        public StorageTypeEnum convert(String source) {
            return source != null ? StorageTypeEnum.valueOf(source) : null;
        }
    }

    @WritingConverter
    public static class CargoTypeEnumToString implements Converter<CargoType, String> {
        @Override
        public String convert(CargoType source) {
            return source != null ? source.name() : null;
        }
    }

    @ReadingConverter
    public static class StringToCargoTypeEnum implements Converter<String, CargoType> {
        @Override
        public CargoType convert(String source) {
            return source != null ? CargoType.valueOf(source) : null;
        }
    }

    @WritingConverter
    public static class HazardLevelEnumToString implements Converter<HazardLevel, String> {
        @Override
        public String convert(HazardLevel source) {
            return source != null ? source.name() : null;
        }
    }

    @ReadingConverter
    public static class StringToHazardLevelEnum implements Converter<String, HazardLevel> {
        @Override
        public HazardLevel convert(String source) {
            return source != null ? HazardLevel.valueOf(source) : null;
        }
    }

    @WritingConverter
    public static class MissionTypeEnumToString implements Converter<MissionType, String> {
        @Override
        public String convert(MissionType source) {
            return source != null ? source.name() : null;
        }
    }

    @ReadingConverter
    public static class StringToMissionTypeEnum implements Converter<String, MissionType> {
        @Override
        public MissionType convert(String source) {
            return source != null ? MissionType.valueOf(source) : null;
        }
    }

    @WritingConverter
    public static class MissionStatusEnumToString implements Converter<MissionStatus, String> {
        @Override
        public String convert(MissionStatus source) {
            return source != null ? source.name() : null;
        }
    }

    @ReadingConverter
    public static class StringToMissionStatusEnum implements Converter<String, MissionStatus> {
        @Override
        public MissionStatus convert(String source) {
            return source != null ? MissionStatus.valueOf(source) : null;
        }
    }

    @WritingConverter
    public static class TransactionTypeEnumToString implements Converter<TransactionType, String> {
        @Override
        public String convert(TransactionType source) {
            return source != null ? source.name() : null;
        }
    }

    @ReadingConverter
    public static class StringToTransactionTypeEnum implements Converter<String, TransactionType> {
        @Override
        public TransactionType convert(String source) {
            return source != null ? TransactionType.valueOf(source) : null;
        }
    }

    @WritingConverter
    public static class ManifestStatusEnumToString implements Converter<ManifestStatus, String> {
        @Override
        public String convert(ManifestStatus source) {
            return source != null ? source.name() : null;
        }
    }

    @ReadingConverter
    public static class StringToManifestStatusEnum implements Converter<String, ManifestStatus> {
        @Override
        public ManifestStatus convert(String source) {
            return source != null ? ManifestStatus.valueOf(source) : null;
        }
    }

    @WritingConverter
    public static class ManifestPriorityEnumToString implements Converter<ManifestPriority, String> {
        @Override
        public String convert(ManifestPriority source) {
            return source != null ? source.name() : null;
        }
    }

    @ReadingConverter
    public static class StringToManifestPriorityEnum implements Converter<String, ManifestPriority> {
        @Override
        public ManifestPriority convert(String source) {
            return source != null ? ManifestPriority.valueOf(source) : null;
        }
    }

    @WritingConverter
    public static class MaintenanceTypeEnumToString implements Converter<MaintenanceType, String> {
        @Override
        public String convert(MaintenanceType source) {
            return source != null ? source.name() : null;
        }
    }

    @ReadingConverter
    public static class StringToMaintenanceTypeEnum implements Converter<String, MaintenanceType> {
        @Override
        public MaintenanceType convert(String source) {
            return source != null ? MaintenanceType.valueOf(source) : null;
        }
    }

    @WritingConverter
    public static class MaintenanceStatusEnumToString implements Converter<MaintenanceStatus, String> {
        @Override
        public String convert(MaintenanceStatus source) {
            return source != null ? source.name() : null;
        }
    }

    @ReadingConverter
    public static class StringToMaintenanceStatusEnum implements Converter<String, MaintenanceStatus> {
        @Override
        public MaintenanceStatus convert(String source) {
            return source != null ? MaintenanceStatus.valueOf(source) : null;
        }
    }

    @WritingConverter
    public static class AssignmentRoleEnumToString implements Converter<AssignmentRole, String> {
        @Override
        public String convert(AssignmentRole source) {
            return source != null ? source.name() : null;
        }
    }

    @ReadingConverter
    public static class StringToAssignmentRoleEnum implements Converter<String, AssignmentRole> {
        @Override
        public AssignmentRole convert(String source) {
            return source != null ? AssignmentRole.valueOf(source) : null;
        }
    }

    @WritingConverter
    public static class MissionPriorityEnumToString implements Converter<MissionPriority, String> {
        @Override
        public String convert(MissionPriority source) {
            return source != null ? source.name() : null;
        }
    }

    @ReadingConverter
    public static class StringToMissionPriorityEnum implements Converter<String, MissionPriority> {
        @Override
        public MissionPriority convert(String source) {
            return source != null ? MissionPriority.valueOf(source) : null;
        }
    }

}