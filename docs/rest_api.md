# REST API Endpoints для Orbital Logistics

## 🚀 Spacecraft (Космические корабли) ✅

| Метод | Endpoint | Описание | Пагинация |
|-------|----------|-----------|------------|
| GET | /api/spacecrafts | Получить все корабли с пагинацией
| GET | /api/spacecrafts/{id} | Получить корабль по ID
| POST | /api/spacecrafts | Создать новый корабль 
| PUT | /api/spacecrafts/{id} | Обновить корабль 
| DELETE | /api/spacecrafts/{id} | Удалить корабль
| GET | /api/spacecrafts/available | Доступные корабли для миссии
| PUT | /api/spacecrafts/{id}/status | Изменить статус корабля 

## 📦 Cargo (Грузы) ✅

| Метод | Endpoint | Описание | Пагинация |
|-------|----------|-----------|------------|
| GET | /api/cargos | Получить все грузы (бесконечная прокрутка)
| GET | /api/cargos/paged | Получить грузы с общим количеством
| GET | /api/cargos/{id} | Получить груз по ID
| POST | /api/cargos | Создать новый груз
| PUT | /api/cargos/{id} | Обновить груз
| DELETE | /api/cargos/{id} | Удалить груз
| GET | /api/cargos/search | Поиск грузов по критериям

## 🏪 StorageUnit (Складские модули) ✅

| Метод | Endpoint | Описание | Пагинация |
|-------|----------|-----------|------------|
| GET | /api/storage-units | Получить все склады
| GET | /api/storage-units/{id} | Получить склад по ID 
| POST | /api/storage-units | Создать новый склад 
| PUT | /api/storage-units/{id} | Обновить склад
| GET | /api/storage-units/{id}/inventory | Инвентарь склада 

## 👤 User (Пользователи) ✅

| Метод | Endpoint | Описание | Пагинация |
|-------|----------|-----------|------------|
| GET | /api/users | Получить всех пользователей 
| GET | /api/users/{id} | Получить пользователя по ID 
| POST | /api/users | Создать нового пользователя 
| PUT | /api/users/{id} | Обновить пользователя 
| DELETE | /api/users/{id} | Деактивировать пользователя 

## 🛡️ Role (Роли) ✅

| Метод | Endpoint | Описание | Пагинация |
|-------|----------|-----------|------------|
| GET | /api/roles | Получить все роли
| GET | /api/roles/{id} | Получить роль по ID 
| POST | /api/roles | Создать новую роль 
| PUT | /api/roles/{id} | Обновить роль 

## 🎯 Mission (Миссии)

| Метод | Endpoint | Описание | Пагинация |
|-------|----------|-----------|------------|
| GET | /api/missions | Получить все миссии 
| GET | /api/missions/{id} | Получить миссию по ID 
| POST | /api/missions | Создать новую миссию 
| PUT | /api/missions/{id} | Обновить миссию
| POST | /api/missions/{id}/complete | Завершить миссию
| GET | /api/missions/active | Активные миссии 

## 📊 SpacecraftType (Типы кораблей)

| Метод | Endpoint | Описание | Пагинация |
|-------|----------|-----------|------------|
| GET | /api/spacecraft-types | Получить все типы кораблей | ❌ |
| GET | /api/spacecraft-types/{id} | Получить тип по ID | ❌ |
| POST | /api/spacecraft-types | Создать новый тип | ❌ |

## 🗂️ CargoCategory (Категории грузов)

| Метод | Endpoint | Описание | Пагинация |
|-------|----------|-----------|------------|
| GET | /api/cargo-categories | Получить все категории | ❌ |
| GET | /api/cargo-categories/{id} | Получить категорию по ID | ❌ |
| POST | /api/cargo-categories | Создать новую категорию | ❌ |
| GET | /api/cargo-categories/tree | Дерево категорий | ❌ |

## 🔄 InventoryTransaction (Учет операций)

| Метод | Endpoint | Описание | Пагинация |
|-------|----------|-----------|------------|
| GET | /api/inventory-transactions | Все транзакции | ✅ Page |
| GET | /api/inventory-transactions/{id} | Транзакция по ID | ❌ |
| POST | /api/inventory-transactions/transfer | Переместить груз | ❌ |
| GET | /api/inventory-transactions/cargo/{cargoId} | История груза | ✅ Page |

## 📋 CargoManifest (Грузовые манифесты)

| Метод | Endpoint | Описание | Пагинация |
|-------|----------|-----------|------------|
| GET | /api/cargo-manifests | Все манифесты | ✅ Page |
| GET | /api/cargo-manifests/{id} | Манифест по ID | ❌ |
| POST | /api/spacecrafts/{id}/load-cargo | Загрузить груз на корабль | ❌ |
| POST | /api/spacecrafts/{id}/unload-cargo | Выгрузить груз с корабля | ❌ |
| GET | /api/spacecrafts/{id}/manifest | Манифест корабля | ✅ Page |

## 👥 MissionAssignment (Назначения на миссии)

| Метод | Endpoint | Описание | Пагинация |
|-------|----------|-----------|------------|
| GET | /api/mission-assignments | Все назначения | ✅ Page |
| POST | /api/missions/{id}/assign-crew | Назначить экипаж | ❌ |
| DELETE | /api/mission-assignments/{id} | Отменить назначение | ❌ |
| GET | /api/missions/{id}/assignments | Назначения миссии | ✅ Page |

## 🏗️ CargoStorage (Складской учет)

| Метод | Endpoint | Описание | Пагинация |
|-------|----------|-----------|------------|
| GET | /api/cargo-storage | Все записи хранения | ✅ Page |
| POST | /api/cargo-storage | Добавить груз на склад | ❌ |
| PUT | /api/cargo-storage/{id}/quantity | Обновить количество | ❌ |
| GET | /api/storage-units/{id}/storage | Грузы на складе | ✅ Page |

## 🔧 MaintenanceLog (Техническое обслуживание)

| Метод | Endpoint | Описание | Пагинация |
|-------|----------|-----------|------------|
| GET | /api/maintenance-logs | Все записи ТО 
| POST | /api/maintenance-logs | Создать запись ТО 
| PUT | /api/maintenance-logs/{id}/status | Обновить статус ТО
| GET | /api/spacecrafts/{id}/maintenance | История ТО корабля

## 🚀 SpacecraftMission (Резервные корабли миссий)

| Метод | Endpoint | Описание | Пагинация |
|-------|----------|-----------|------------|
| POST | /api/missions/{id}/backup-spacecraft | Добавить резервный корабль
| DELETE | /api/missions/{missionId}/backup-spacecraft/{spacecraftId} | Удалить резервный корабль
| GET | /api/missions/{id}/backup-spacecrafts | Резервные корабли миссии 

## 📈 Report Endpoints (Отчеты)

| Метод | Endpoint | Описание | Пагинация |
|-------|----------|-----------|------------|
| GET | /api/reports/spacecraft-utilization | Загрузка кораблей 
| GET | /api/reports/cargo-movements | Перемещения грузов 
| GET | /api/reports/maintenance-stats | Статистика ТО 
| GET | /api/reports/mission-success | Статистика миссий 

## 🎯 Сложные операции с транзакциями

| Метод | Endpoint | Описание | Транзакция |
|-------|----------|-----------|------------|
| POST | /api/operations/load-and-launch | Загрузка и запуск миссии
| POST | /api/operations/transfer-between-storages | Перемещение между складами
| POST | /api/operations/emergency-unload | Аварийная выгрузка 
| POST | /api/operations/maintenance-complete | Завершение ТО с обновлением статуса 

## 🔍 Поиск и фильтрация

| Метод | Endpoint | Описание |
|-------|----------|-----------|
| GET | /api/search/cargos | Расширенный поиск грузов |
| GET | /api/search/spacecrafts | Поиск кораблей по параметрам |
| GET | /api/search/missions | Фильтрация миссий |

## 📋 Статусы ответов

- 200 OK - Успешный запрос
- 201 Created - Успешное создание
- 400 Bad Request - Ошибка валидации
- 404 Not Found - Ресурс не найден
- 409 Conflict - Конфликт состояний
- 500 Internal Server Error - Ошибка сервера
