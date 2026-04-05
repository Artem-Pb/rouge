# Data Layer Plan — Roguelike Game

**Роль:** data
**Дата:** 2026-03-26
**Версия:** 1.0
**Статус:** готов к исполнению

---

## 1. Контекст и зависимости

### Что уже есть
| Ресурс | Статус |
|--------|--------|
| `jackson-databind:2.21.1` | ✅ в `build.gradle.kts` |
| `lanterna:3.1.1` | ✅ (не используем в data) |
| `domain/ports/` | ⏳ пустой пакет, нужно наполнить (задача domain) |
| `data/` | ❌ пустой пакет, вся работа впереди |

### Блокеры
Data слой **зависит от domain**. Прежде чем начинать реализацию:
- `domain.ports.SaveRepository` — должен существовать
- `domain.ports.LeaderboardRepository` — должен существовать
- `domain.model.session.GameSession` — должен существовать
- `domain.model.session.LeaderboardEntry` — должен существовать

> Если domain ещё не готов → первые два дня можно потратить на проектирование DTO и JSON-схемы. Код пишем только после готовности domain.

---

## 2. Целевая структура пакетов

```
ru.school21.rogue.data
│
├── save/
│   ├── JsonSaveRepository.java          # реализует domain.ports.SaveRepository
│   ├── SessionMapper.java               # маппинг: GameSession ↔ GameSessionDto
│   └── dto/
│       ├── GameSessionDto.java          # корневой DTO сессии
│       ├── PlayerDto.java               # состояние игрока
│       ├── InventoryDto.java            # инвентарь (списки предметов)
│       ├── ItemDto.java                 # предмет с type-discriminator
│       ├── LevelDto.java                # уровень целиком
│       ├── RoomDto.java                 # комната
│       ├── CorridorDto.java             # коридор
│       ├── TileDto.java                 # клетка карты
│       ├── EnemyDto.java                # враг с EnemyType
│       └── GameStatsDto.java            # статистика
│
├── leaderboard/
│   ├── JsonLeaderboardRepository.java   # реализует domain.ports.LeaderboardRepository
│   └── dto/
│       └── LeaderboardEntryDto.java     # запись таблицы рекордов
│
└── config/
    └── JacksonConfig.java               # ObjectMapper с нужными настройками
```

**Файлы на диске:**
- `save.json` — текущая сохранённая сессия
- `leaderboard.json` — массив записей таблицы рекордов

---

## 3. JSON-схема

### 3.1 `save.json` — структура

```json
{
  "player": {
    "health": 80,
    "maxHealth": 100,
    "strength": 6,
    "agility": 4,
    "position": { "x": 5, "y": 3 },
    "sleeping": false,
    "inventory": {
      "items": [
        { "type": "FOOD",    "healAmount": 30 },
        { "type": "WEAPON",  "damageBonus": 5 },
        { "type": "SCROLL",  "bonusStrength": 2, "bonusAgility": 0 },
        { "type": "ELIXIR",  "bonusStrength": 1, "bonusAgility": 2, "duration": 10 },
        { "type": "TREASURE","value": 50 }
      ]
    }
  },
  "level": {
    "number": 3,
    "tiles": [
      [ { "type": "WALL",  "visible": false, "explored": false }, "..." ],
      [ { "type": "FLOOR", "visible": true,  "explored": true  }, "..." ]
    ],
    "rooms": [
      { "topLeft": { "x": 1, "y": 1 }, "width": 8, "height": 5,
        "startRoom": true, "exitRoom": false }
    ],
    "corridors": [
      { "fromIndex": 0, "toIndex": 1,
        "path": [ { "x": 9, "y": 3 }, { "x": 10, "y": 3 } ] }
    ],
    "enemies": [
      { "type": "ZOMBIE",  "health": 30, "maxHealth": 30,
        "strength": 4, "agility": 2, "hostility": 3,
        "position": { "x": 12, "y": 4 }, "alive": true }
    ],
    "items": [
      { "type": "FOOD", "healAmount": 20, "position": { "x": 7, "y": 2 } }
    ],
    "playerStart": { "x": 2, "y": 2 },
    "exit":        { "x": 18, "y": 9 }
  },
  "stats": {
    "currentLevelNumber": 3,
    "gold": 120,
    "kills": 7,
    "itemsUsed": 3,
    "stepsTaken": 214
  }
}
```

### 3.2 `leaderboard.json` — структура

```json
[
  { "playerName": "Hero", "gold": 450, "level": 12, "date": "2026-03-26" },
  { "playerName": "Rogue", "gold": 310, "level": 8,  "date": "2026-03-25" }
]
```

### 3.3 Полиморфизм предметов (ItemDto)

`Item` — абстрактный класс с подтипами. При сериализации нужно сохранять тип.

Подход: поле `"type"` как строковый дискриминатор + Jackson `@JsonTypeInfo` / `@JsonSubTypes`.

| type       | Дополнительные поля                          |
|------------|----------------------------------------------|
| FOOD       | `healAmount: int`                            |
| WEAPON     | `damageBonus: int`                           |
| SCROLL     | `bonusStrength: int`, `bonusAgility: int`    |
| ELIXIR     | `bonusStrength: int`, `bonusAgility: int`, `duration: int` |
| TREASURE   | `value: int`                                 |

Предмет на уровне содержит дополнительно поле `position`.

---

## 4. Контракты интерфейсов (реализует data, определяет domain)

### `SaveRepository` (определён в `domain.ports`)
```
void save(GameSession session)    — сериализует в save.json
GameSession load()                — десериализует из save.json
boolean hasSave()                 — проверяет существование файла
void deleteSave()                 — удаляет save.json (при game over)
```

### `LeaderboardRepository` (определён в `domain.ports`)
```
void save(LeaderboardEntry entry)             — добавляет запись, сортирует по gold DESC
List<LeaderboardEntry> getTop(int limit)      — возвращает топ-N
```

---

## 5. Дневные миссии

> Каждая миссия — это конкретный результат, который ты должен закрыть сам.
> Миссия считается выполненной, когда код написан, компилируется и проверен вручную.

---

### ДЕНЬ 1 — Фундамент и конфигурация

**Предусловие:** domain ports (`SaveRepository`, `LeaderboardRepository`) и session-модели должны быть реализованы в domain слое.

#### Миссия 1.1 — Проверить готовность domain
- Убедиться, что следующие классы существуют и компилируются:
  - `domain.ports.SaveRepository`
  - `domain.ports.LeaderboardRepository`
  - `domain.model.session.GameSession`
  - `domain.model.session.LeaderboardEntry`
- Если хотя бы одного нет → остановиться, закрыть миссию как BLOCKED, уведомить.

**Критерий закрытия:** все 4 класса найдены, импорт не даёт ошибок.

---

#### Миссия 1.2 — Создать пакетную структуру data слоя
Создать директории:
```
src/main/java/ru/school21/rogue/data/save/dto/
src/main/java/ru/school21/rogue/data/leaderboard/dto/
src/main/java/ru/school21/rogue/data/config/
```

**Критерий закрытия:** директории созданы, видны в файловой системе.

---

#### Миссия 1.3 — JacksonConfig
Создать `data/config/JacksonConfig.java`:
- Единственный метод: `static ObjectMapper create()`
- Настройки:
  - `FAIL_ON_UNKNOWN_PROPERTIES = false` (устойчивость к будущим изменениям схемы)
  - `INDENT_OUTPUT = true` (читаемый JSON)
  - `JavaTimeModule` если нужны даты (опционально)

**Критерий закрытия:** класс создан, `ObjectMapper` создаётся без ошибок.

---

### ДЕНЬ 2 — DTO-объекты (слой данных без логики)

> DTO — это простые классы с полями. Никакой логики. Только хранение для JSON.

#### Миссия 2.1 — ItemDto
Создать `data/save/dto/ItemDto.java`:
- Поле `type` (String) — дискриминатор типа предмета
- Все возможные поля всех подтипов (nullable): `healAmount`, `damageBonus`, `bonusStrength`, `bonusAgility`, `duration`, `value`
- Поле `position` (PositionDto) — для предметов на уровне (null для инвентаря)

Создать `data/save/dto/PositionDto.java`:
- `int x`, `int y`

**Критерий закрытия:** классы компилируются, ObjectMapper сериализует экземпляр без ошибок.

---

#### Миссия 2.2 — PlayerDto + InventoryDto
Создать `data/save/dto/InventoryDto.java`:
- `List<ItemDto> items`

Создать `data/save/dto/PlayerDto.java`:
- `int health`, `int maxHealth`, `int strength`, `int agility`
- `PositionDto position`
- `boolean sleeping`
- `InventoryDto inventory`

**Критерий закрытия:** DTO сериализуются и десериализуются в JSON корректно.

---

#### Миссия 2.3 — EnemyDto
Создать `data/save/dto/EnemyDto.java`:
- `String type` — значение из `EnemyType` enum (ZOMBIE, VAMPIRE, GHOST, OGR, SNAKE_MAGE)
- `int health`, `int maxHealth`, `int strength`, `int agility`, `int hostility`
- `PositionDto position`
- `boolean alive`
- Специфичные поля: `boolean resting` (для Ogr), `boolean invisible` (для Ghost)

**Критерий закрытия:** DTO сериализуется, тип врага читаем в JSON.

---

#### Миссия 2.4 — TileDto, RoomDto, CorridorDto
Создать `data/save/dto/TileDto.java`:
- `String type` (FLOOR, WALL, CORRIDOR, EXIT, EMPTY)
- `boolean visible`, `boolean explored`

Создать `data/save/dto/RoomDto.java`:
- `PositionDto topLeft`
- `int width`, `int height`
- `boolean startRoom`, `boolean exitRoom`

Создать `data/save/dto/CorridorDto.java`:
- `int fromIndex`, `int toIndex` (индексы в списке rooms)
- `List<PositionDto> path`

**Критерий закрытия:** все три класса компилируются.

---

#### Миссия 2.5 — LevelDto
Создать `data/save/dto/LevelDto.java`:
- `int number`
- `TileDto[][] tiles`
- `List<RoomDto> rooms`
- `List<CorridorDto> corridors`
- `List<EnemyDto> enemies`
- `List<ItemDto> items`
- `PositionDto playerStart`
- `PositionDto exit`

**Критерий закрытия:** DTO компилируется, вложенные объекты доступны.

---

#### Миссия 2.6 — GameStatsDto + GameSessionDto
Создать `data/save/dto/GameStatsDto.java`:
- `int currentLevelNumber`, `int gold`, `int kills`, `int itemsUsed`, `int stepsTaken`

Создать `data/save/dto/GameSessionDto.java`:
- `PlayerDto player`
- `LevelDto level`
- `GameStatsDto stats`

Создать `data/leaderboard/dto/LeaderboardEntryDto.java`:
- `String playerName`, `int gold`, `int level`, `String date`

**Критерий закрытия:** полная цепочка сериализации без ошибок: `GameSessionDto → JSON → GameSessionDto`.

---

### ДЕНЬ 3 — Маппинг и репозитории

> Здесь появляется реальная логика: конвертация domain-объектов в DTO и обратно.

#### Миссия 3.1 — SessionMapper
Создать `data/save/SessionMapper.java`:
- `GameSessionDto toDto(GameSession session)` — конвертирует все domain-объекты в DTO
- `GameSession fromDto(GameSessionDto dto)` — восстанавливает domain-объекты из DTO

Это самый сложный класс слоя. Маппинг должен быть **полным**: каждое поле domain-объекта → поле DTO и обратно.

**Критерий закрытия:** написан маппинг для Player + Inventory + Items. Level, Enemies — можно оставить на следующий проход, но класс должен компилироваться.

---

#### Миссия 3.2 — JsonSaveRepository
Создать `data/save/JsonSaveRepository.java`:
- Реализует `domain.ports.SaveRepository`
- Зависит от: `ObjectMapper` (через конструктор), `SessionMapper`, путь к файлу (`save.json`)
- `save(GameSession)`: `session → SessionMapper.toDto() → ObjectMapper.writeValue(file)`
- `load()`: `ObjectMapper.readValue(file) → SessionMapper.fromDto()`
- `hasSave()`: `file.exists()`
- `deleteSave()`: `file.delete()`

**Критерий закрытия:** `save()` создаёт файл, `load()` читает тот же файл и возвращает объект без NPE.

---

#### Миссия 3.3 — JsonLeaderboardRepository
Создать `data/leaderboard/JsonLeaderboardRepository.java`:
- Реализует `domain.ports.LeaderboardRepository`
- Зависит от: `ObjectMapper`, путь к файлу (`leaderboard.json`)
- `save(LeaderboardEntry)`: читает файл → добавляет запись → сортирует по `gold DESC` → записывает обратно
- `getTop(int limit)`: читает файл → возвращает первые N записей

**Критерий закрытия:** после двух вызовов `save()` файл содержит 2 записи, отсортированные по gold.

---

### ДЕНЬ 4 — Тесты и интеграция

#### Миссия 4.1 — Тест сохранения/загрузки
Создать `src/test/java/ru/school21/rogue/data/SaveRepositoryTest.java`:

Сценарий:
1. Создать `GameSession` с минимальным набором данных
2. `save(session)`
3. `load()` → получить новый объект
4. Проверить: `player.health == original.health`
5. Проверить: `stats.gold == original.stats.gold`

**Критерий закрытия:** тест проходит (`./gradlew test`).

---

#### Миссия 4.2 — Тест leaderboard
Создать `src/test/java/ru/school21/rogue/data/LeaderboardRepositoryTest.java`:

Сценарий:
1. Сохранить 3 записи с разным `gold`
2. `getTop(2)` → получить 2 записи
3. Проверить: первая запись содержит наибольший `gold`

**Критерий закрытия:** тест проходит (`./gradlew test`).

---

#### Миссия 4.3 — Wiring в Main (подготовка к integration)
В `Main.java` убедиться, что `JsonSaveRepository` и `JsonLeaderboardRepository` можно инстанциировать:
```
ObjectMapper mapper = JacksonConfig.create();
SaveRepository saveRepo = new JsonSaveRepository(mapper, "save.json");
LeaderboardRepository leaderRepo = new JsonLeaderboardRepository(mapper, "leaderboard.json");
```

**Критерий закрытия:** код компилируется, `./gradlew build` — без ошибок.

---

## 6. Зависимости слоя (что data использует, что нет)

```
data слой
│
├── ИСПОЛЬЗУЕТ:
│   ├── ru.school21.rogue.domain.ports.*         (интерфейсы-контракты)
│   ├── ru.school21.rogue.domain.model.*         (domain-объекты для маппинга)
│   └── com.fasterxml.jackson.*                  (сериализация)
│
└── НЕ ИСПОЛЬЗУЕТ:
    ├── ru.school21.rogue.gameplay.*             (нарушение архитектуры)
    ├── ru.school21.rogue.presentation.*         (нарушение архитектуры)
    └── com.googlecode.lanterna.*               (нарушение архитектуры)
```

---

## 7. Сводная таблица миссий

| День | Миссия | Результат | Критерий |
|------|--------|-----------|----------|
| 1 | 1.1 Проверить domain | блокеры сняты | 4 класса найдены |
| 1 | 1.2 Структура папок | директории созданы | видны в FS |
| 1 | 1.3 JacksonConfig | ObjectMapper готов | создаётся без ошибок |
| 2 | 2.1 ItemDto + PositionDto | DTO предмета | сериализуется |
| 2 | 2.2 PlayerDto + InventoryDto | DTO игрока | сериализуется |
| 2 | 2.3 EnemyDto | DTO врага | type в JSON |
| 2 | 2.4 TileDto, RoomDto, CorridorDto | DTO мира | компилируются |
| 2 | 2.5 LevelDto | DTO уровня | компилируется |
| 2 | 2.6 GameStatsDto + GameSessionDto | DTO сессии | полный цикл JSON |
| 3 | 3.1 SessionMapper | маппинг domain↔DTO | Player замаплен |
| 3 | 3.2 JsonSaveRepository | save/load файла | round-trip работает |
| 3 | 3.3 JsonLeaderboardRepository | leaderboard файла | сортировка работает |
| 4 | 4.1 SaveRepositoryTest | тест сохранения | ✅ green |
| 4 | 4.2 LeaderboardRepositoryTest | тест таблицы | ✅ green |
| 4 | 4.3 Wiring в Main | интеграция | `./gradlew build` OK |
