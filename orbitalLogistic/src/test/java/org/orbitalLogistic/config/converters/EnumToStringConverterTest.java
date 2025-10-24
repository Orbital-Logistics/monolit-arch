package org.orbitalLogistic.config.converters;

import org.junit.jupiter.api.Test;
import org.orbitalLogistic.entities.enums.*;
import org.springframework.core.convert.converter.Converter;

import static org.junit.jupiter.api.Assertions.*;

class EnumToStringConverterTest {

    @Test
    void spacecraftStatusEnumToString_ConvertValidEnum_ShouldReturnString() {
        Converter<SpacecraftStatus, String> converter = new EnumToStringConverter.SpacecraftStatusEnumToString();
        String result = converter.convert(SpacecraftStatus.DOCKED);
        assertEquals("DOCKED", result);
    }

    @Test
    void spacecraftStatusEnumToString_ConvertNull_ShouldReturnNull() {
        Converter<SpacecraftStatus, String> converter = new EnumToStringConverter.SpacecraftStatusEnumToString();
        String result = converter.convert(null);
        assertNull(result);
    }

    @Test
    void stringToSpacecraftStatusEnum_ConvertValidString_ShouldReturnEnum() {
        Converter<String, SpacecraftStatus> converter = new EnumToStringConverter.StringToSpacecraftStatusEnum();
        SpacecraftStatus result = converter.convert("DOCKED");
        assertEquals(SpacecraftStatus.DOCKED, result);
    }

    @Test
    void stringToSpacecraftStatusEnum_ConvertNull_ShouldReturnNull() {
        Converter<String, SpacecraftStatus> converter = new EnumToStringConverter.StringToSpacecraftStatusEnum();
        SpacecraftStatus result = converter.convert(null);
        assertNull(result);
    }

    @Test
    void storageTypeEnumToString_ConvertValidEnum_ShouldReturnString() {
        Converter<StorageTypeEnum, String> converter = new EnumToStringConverter.StorageTypeEnumToString();
        String result = converter.convert(StorageTypeEnum.AMBIENT);
        assertEquals("AMBIENT", result);
    }

    @Test
    void stringToStorageTypeEnum_ConvertValidString_ShouldReturnEnum() {
        Converter<String, StorageTypeEnum> converter = new EnumToStringConverter.StringToStorageTypeEnum();
        StorageTypeEnum result = converter.convert("AMBIENT");
        assertEquals(StorageTypeEnum.AMBIENT, result);
    }

    @Test
    void cargoTypeEnumToString_ConvertValidEnum_ShouldReturnString() {
        Converter<CargoType, String> converter = new EnumToStringConverter.CargoTypeEnumToString();
        String result = converter.convert(CargoType.FOOD);
        assertEquals("FOOD", result);
    }

    @Test
    void stringToCargoTypeEnum_ConvertValidString_ShouldReturnEnum() {
        Converter<String, CargoType> converter = new EnumToStringConverter.StringToCargoTypeEnum();
        CargoType result = converter.convert("FOOD");
        assertEquals(CargoType.FOOD, result);
    }


    @Test
    void missionTypeEnumToString_ConvertValidEnum_ShouldReturnString() {
        Converter<MissionType, String> converter = new EnumToStringConverter.MissionTypeEnumToString();
        String result = converter.convert(MissionType.CARGO_TRANSPORT);
        assertEquals("CARGO_TRANSPORT", result);
    }

    @Test
    void stringToMissionTypeEnum_ConvertValidString_ShouldReturnEnum() {
        Converter<String, MissionType> converter = new EnumToStringConverter.StringToMissionTypeEnum();
        MissionType result = converter.convert("CARGO_TRANSPORT");
        assertEquals(MissionType.CARGO_TRANSPORT, result);
    }


    @Test
    void transactionTypeEnumToString_ConvertValidEnum_ShouldReturnString() {
        Converter<TransactionType, String> converter = new EnumToStringConverter.TransactionTypeEnumToString();
        String result = converter.convert(TransactionType.LOAD);
        assertEquals("LOAD", result);
    }

    @Test
    void stringToTransactionTypeEnum_ConvertValidString_ShouldReturnEnum() {
        Converter<String, TransactionType> converter = new EnumToStringConverter.StringToTransactionTypeEnum();
        TransactionType result = converter.convert("LOAD");
        assertEquals(TransactionType.LOAD, result);
    }






}
