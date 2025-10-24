package org.orbitalLogistic.config.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.orbitalLogistic.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.entities.enums.*;

public class EnumToStringConverter {

    // Spacecraft Status
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

    // Storage Type
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

    // Cargo Type
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

    // Hazard Level
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

    // Mission Type
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

    // Mission Status
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

}