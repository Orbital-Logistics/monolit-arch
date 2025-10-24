-- 1. Роли
INSERT INTO role (name, description, permissions) VALUES
('admin', 'System Administrator', '{"cargo": "full", "spacecraft": "full", "users": "full"}'),
('logistics_officer', 'Manages cargo and storage', '{"cargo": "edit", "storage": "full"}'),
('mission_commander', 'Leads missions', '{"missions": "lead", "crew": "view"}'),
('technician', 'Performs maintenance', '{"maintenance": "edit"}');

-- 2. Пользователи
INSERT INTO users (username, email, password_hash, role_id, is_active) VALUES
('admin_user', 'admin@orbital.log', 'hash123' , 1, true),
('logi_officer', 'logi@orbital.log', 'hash456', 2, true),
('cmdr_reyes', 'reyes@orbital.log', 'hash789', 3, true),
('tech_jones', 'jones@orbital.log', 'hash000', 4, true);

-- 3. Типы кораблей
INSERT INTO spacecraft_type (type_name, classification, max_crew_capacity) VALUES
('Freighter-X9', 'CARGO_HAULER', 6),
('Orion-Class', 'PERSONNEL_TRANSPORT', 24),
('Voyager-7', 'SCIENCE_VESSEL', 12);

-- 4. Корабли
INSERT INTO spacecraft (registry_code, name, spacecraft_type_id, mass_capacity, volume_capacity, status, current_location) VALUES
('SC-001', 'Star Mule', 1, 50000.00, 1200.00, 'DOCKED', 'Orbital Station Alpha'),
('SC-002', 'Aurora', 2, 10000.00, 800.00, 'IN_TRANSIT', 'En route to Mars'),
('SC-003', 'Nebula Explorer', 3, 8000.00, 600.00, 'MAINTENANCE', 'Dry Dock 4');

-- 5. Категории грузов (с рекурсией)
INSERT INTO cargo_category (name, parent_category_id, description) VALUES
('Supplies', NULL, 'General supplies'),
('Food', 1, 'Edible items'),
('Equipment', NULL, 'Tools and machinery'),
('Scientific Instruments', 3, 'Lab and research gear');

-- 6. Грузы
INSERT INTO cargo (name, cargo_category_id, mass_per_unit, volume_per_unit, cargo_type, hazard_level) VALUES
('Dehydrated Rations', 2, 0.50, 0.01, 'FOOD', 'NONE'),
('Oxygen Tanks', 1, 10.00, 0.10, 'EQUIPMENT', 'LOW'),
('Plasma Spectrometer', 4, 25.00, 0.30, 'SCIENTIFIC', 'NONE'),
('Reinforced Steel Beams', 3, 500.00, 2.00, 'CONSTRUCTION_MATERIALS', 'NONE');

-- 7. Склады
INSERT INTO storage_unit (unit_code, location, storage_type, total_mass_capacity, total_volume_capacity) VALUES
('SU-ALPHA-01', 'Orbital Station Alpha', 'AMBIENT', 100000.00, 2000.00),
('SU-MARS-05', 'Mars Colony Base', 'PRESSURIZED', 50000.00, 1000.00),
('SU-HAZ-01', 'Lunar Quarantine Zone', 'HAZMAT', 10000.00, 200.00);

-- 8. Миссии
INSERT INTO mission (mission_code, mission_name, mission_type, status, priority, commanding_officer_id, spacecraft_id, scheduled_departure, scheduled_arrival) VALUES
('MIS-2025-01', 'Mars Resupply Run', 'CARGO_TRANSPORT', 'SCHEDULED', 'HIGH', 3, 1, '2025-06-15 08:00:00', '2025-07-01 14:00:00'),
('MIS-2025-02', 'Jupiter Science Survey', 'SCIENCE_EXPEDITION', 'PLANNING', 'CRITICAL', 3, 3, '2025-09-01 00:00:00', '2026-03-01 00:00:00');

-- 9. Назначения на миссии
INSERT INTO mission_assignment (mission_id, user_id, assignment_role, responsibility_zone) VALUES
(1, 3, 'COMMANDER', 'Bridge'),
(1, 2, 'CARGO_OFFICER', 'Cargo Bay'),
(2, 3, 'COMMANDER', 'Science Deck'),
(2, 4, 'ENGINEER', 'Engineering');

-- 10. Складские остатки
INSERT INTO cargo_storage (storage_unit_id, cargo_id, quantity, stored_at, last_checked_by_user_id) VALUES
(1, 1, 10000, NOW() - INTERVAL '2 days', 2),
(1, 2, 500, NOW() - INTERVAL '1 day', 2),
(1, 3, 20, NOW(), 2),
(2, 1, 2000, NOW() - INTERVAL '5 days', NULL);

-- 11. Манифесты (груз на борту или в процессе загрузки)
INSERT INTO cargo_manifest (
    spacecraft_id, cargo_id, storage_unit_id, quantity,
    loaded_at, loaded_by_user_id, manifest_status, priority
) VALUES
(1, 1, 1, 5000, NOW() - INTERVAL '1 hour', 2, 'LOADED', 'HIGH'),
(1, 2, 1, 200, NOW() - INTERVAL '30 minutes', 2, 'LOADED', 'NORMAL');

-- 12. Транзакции (загрузка на корабль = транзакция LOAD)
INSERT INTO inventory_transaction (
    transaction_type, cargo_id, quantity,
    from_storage_unit_id, to_spacecraft_id,
    performed_by_user_id, reason_code, reference_number
) VALUES
('LOAD', 1, 5000, 1, 1, 2, 'MIS-2025-01_PREP', 'TXN-001'),
('LOAD', 2, 200, 1, 1, 2, 'MIS-2025-01_PREP', 'TXN-002');

-- 13. Резервные корабли для миссий (простая M2M)
INSERT INTO spacecraft_mission (spacecraft_id, mission_id) VALUES
(2, 1); -- Aurora как резерв для Mars Resupply

-- 14. Журнал техобслуживания
INSERT INTO maintenance_log (
    spacecraft_id, maintenance_type, performed_by_user_id, supervised_by_user_id,
    start_time, end_time, status, description, cost
) VALUES
(3, 'ROUTINE', 4, 3, NOW() - INTERVAL '3 days', NOW() - INTERVAL '1 day', 'COMPLETED', 'Annual systems calibration', 12500.00);