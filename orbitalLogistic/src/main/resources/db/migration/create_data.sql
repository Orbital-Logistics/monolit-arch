-- 1. ENUM-типы (PostgreSQL позволяет создавать собственные типы)
CREATE TYPE spacecraft_status_enum AS ENUM ('DOCKED', 'IN_TRANSIT', 'MAINTENANCE', 'DECOMMISSIONED');
CREATE TYPE spacecraft_classification_enum AS ENUM ('CARGO_HAULER', 'PERSONNEL_TRANSPORT', 'SCIENCE_VESSEL');
CREATE TYPE cargo_type_enum AS ENUM ('FOOD', 'EQUIPMENT', 'SCIENTIFIC', 'CONSTRUCTION_MATERIALS');
CREATE TYPE hazard_level_enum AS ENUM ('NONE', 'LOW', 'MEDIUM', 'HIGH');
CREATE TYPE storage_type_enum AS ENUM ('AMBIENT', 'REFRIGERATED', 'PRESSURIZED', 'HAZMAT');
CREATE TYPE mission_type_enum AS ENUM ('CARGO_TRANSPORT', 'PERSONNEL_TRANSPORT', 'SCIENCE_EXPEDITION');
CREATE TYPE mission_status_enum AS ENUM ('PLANNING', 'SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
CREATE TYPE mission_priority_enum AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');
CREATE TYPE assignment_role_enum AS ENUM ('COMMANDER', 'PILOT', 'ENGINEER', 'SCIENTIST', 'CARGO_OFFICER');
CREATE TYPE maintenance_type_enum AS ENUM ('ROUTINE', 'REPAIR', 'UPGRADE', 'INSPECTION');
CREATE TYPE maintenance_status_enum AS ENUM ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED');
CREATE TYPE transaction_type_enum AS ENUM ('LOAD', 'UNLOAD', 'TRANSFER', 'ADJUSTMENT', 'CONSUMPTION');

-- 2. Таблицы

CREATE TABLE role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    permissions JSON
);

CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role_id BIGINT NOT NULL REFERENCES role(id) ON DELETE RESTRICT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE SpacecraftType (
    id BIGSERIAL PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL UNIQUE,
    classification spacecraft_classification_enum NOT NULL,
    max_crew_capacity INTEGER
);

CREATE TABLE Spacecraft (
    id BIGSERIAL PRIMARY KEY,
    registry_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    spacecraft_type_id BIGINT NOT NULL REFERENCES SpacecraftType(id) ON DELETE RESTRICT,
    mass_capacity DECIMAL(15,2) NOT NULL,
    volume_capacity DECIMAL(15,2) NOT NULL,
    status spacecraft_status_enum NOT NULL DEFAULT 'DOCKED',
    current_location VARCHAR(100)
);

CREATE TABLE CargoCategory (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    parent_category_id BIGINT REFERENCES CargoCategory(id) ON DELETE SET NULL,
    description TEXT
);

CREATE TABLE Cargo (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    cargo_category_id BIGINT NOT NULL REFERENCES CargoCategory(id) ON DELETE RESTRICT,
    mass_per_unit DECIMAL(10,2) NOT NULL,
    volume_per_unit DECIMAL(10,2) NOT NULL,
    cargo_type cargo_type_enum NOT NULL,
    hazard_level hazard_level_enum NOT NULL DEFAULT 'NONE'
);

CREATE TABLE StorageUnit (
    id BIGSERIAL PRIMARY KEY,
    unit_code VARCHAR(20) NOT NULL UNIQUE,
    location VARCHAR(100) NOT NULL,
    storage_type storage_type_enum NOT NULL,
    total_mass_capacity DECIMAL(15,2) NOT NULL,
    total_volume_capacity DECIMAL(15,2) NOT NULL,
    current_mass DECIMAL(15,2) NOT NULL DEFAULT 0,
    current_volume DECIMAL(15,2) NOT NULL DEFAULT 0
);

CREATE TABLE Mission (
    id BIGSERIAL PRIMARY KEY,
    mission_code VARCHAR(20) NOT NULL UNIQUE,
    mission_name VARCHAR(200) NOT NULL,
    mission_type mission_type_enum NOT NULL,
    status mission_status_enum NOT NULL DEFAULT 'PLANNING',
    priority mission_priority_enum NOT NULL DEFAULT 'MEDIUM',
    commanding_officer_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    spacecraft_id BIGINT NOT NULL REFERENCES Spacecraft(id) ON DELETE RESTRICT,
    scheduled_departure TIMESTAMP WITHOUT TIME ZONE,
    scheduled_arrival TIMESTAMP WITHOUT TIME ZONE
);

-- Связующие таблицы

CREATE TABLE MissionAssignment (
    id BIGSERIAL PRIMARY KEY,
    mission_id BIGINT NOT NULL REFERENCES Mission(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    assignment_role assignment_role_enum NOT NULL,
    responsibility_zone VARCHAR(100)
);

CREATE TABLE CargoStorage (
    id BIGSERIAL PRIMARY KEY,
    storage_unit_id BIGINT NOT NULL REFERENCES StorageUnit(id) ON DELETE CASCADE,
    cargo_id BIGINT NOT NULL REFERENCES Cargo(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
    stored_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    last_inventory_check TIMESTAMP WITHOUT TIME ZONE,
    last_checked_by_user_id BIGINT REFERENCES "user"(id) ON DELETE SET NULL
);

CREATE TABLE MaintenanceLog (
    id BIGSERIAL PRIMARY KEY,
    spacecraft_id BIGINT NOT NULL REFERENCES Spacecraft(id) ON DELETE CASCADE,
    maintenance_type maintenance_type_enum NOT NULL,
    performed_by_user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    supervised_by_user_id BIGINT REFERENCES "user"(id) ON DELETE SET NULL,
    start_time TIMESTAMP WITHOUT TIME ZONE,
    end_time TIMESTAMP WITHOUT TIME ZONE,
    status maintenance_status_enum NOT NULL DEFAULT 'SCHEDULED',
    description TEXT,
    cost DECIMAL(10,2)
);

CREATE TABLE CargoManifest (
    id BIGSERIAL PRIMARY KEY,
    spacecraft_id BIGINT NOT NULL REFERENCES Spacecraft(id) ON DELETE CASCADE,
    cargo_id BIGINT NOT NULL REFERENCES Cargo(id) ON DELETE RESTRICT,
    storage_unit_id BIGINT NOT NULL REFERENCES StorageUnit(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    loaded_at TIMESTAMP WITHOUT TIME ZONE,
    unloaded_at TIMESTAMP WITHOUT TIME ZONE,
    loaded_by_user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    unloaded_by_user_id BIGINT REFERENCES "user"(id) ON DELETE SET NULL,
    manifest_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (manifest_status IN ('PENDING', 'LOADED', 'IN_TRANSIT', 'UNLOADED')),
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'CRITICAL'))
);

CREATE TABLE InventoryTransaction (
    id BIGSERIAL PRIMARY KEY,
    transaction_type transaction_type_enum NOT NULL,
    cargo_id BIGINT NOT NULL REFERENCES Cargo(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL,
    from_storage_unit_id BIGINT REFERENCES StorageUnit(id) ON DELETE SET NULL,
    to_storage_unit_id BIGINT REFERENCES StorageUnit(id) ON DELETE SET NULL,
    from_spacecraft_id BIGINT REFERENCES Spacecraft(id) ON DELETE SET NULL,
    to_spacecraft_id BIGINT REFERENCES Spacecraft(id) ON DELETE SET NULL,
    performed_by_user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    transaction_date TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    reason_code VARCHAR(50),
    reference_number VARCHAR(100),
    notes TEXT,
    -- Добавим проверку: хотя бы один источник и один получатель должны быть указаны
    CONSTRAINT chk_transaction_source CHECK (
        from_storage_unit_id IS NOT NULL OR from_spacecraft_id IS NOT NULL
    ),
    CONSTRAINT chk_transaction_target CHECK (
        to_storage_unit_id IS NOT NULL OR to_spacecraft_id IS NOT NULL
    )
);

-- Простая M2M: Spacecraft <-> Mission (резервные корабли)
CREATE TABLE SpacecraftMission (
    spacecraft_id BIGINT REFERENCES Spacecraft(id) ON DELETE CASCADE,
    mission_id BIGINT REFERENCES Mission(id) ON DELETE CASCADE,
    PRIMARY KEY (spacecraft_id, mission_id)
);