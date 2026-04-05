# Domain Layer Plan — Roguelike Game

**Роль:** domain
**Дата:** 2026-03-26
**Версия:** 2.0
**Статус:** готов к исполнению

---

## 1. Зона ответственности

Domain — это **ядро игры**. Здесь только:
- Сущности и их состояние
- Инварианты (правила, которые нельзя нарушить: HP ≥ 0 и т.д.)
- Интерфейсы-контракты, которые другие слои обязаны реализовать

Domain **не знает** ни о Lanterna, ни о Jackson, ни об AI, ни о генерации уровней.

---

## 2. Текущее состояние пакетов (после нормализации)

```
ru.school21.rogue.domain
│
├── model/
│   ├── character/       ✅ Actor, Player, Enemy, EnemyType, Zombie, Vampire, Ghost, Ogr, SnakeMage
│   ├── common/          ✅ Position, AttackEffect, Damageable | Movable удалён
│   ├── item/            ✅ Item, Food, Weapon, Scroll, Elixir, Treasure
│   ├── inventory/       ✅ Inventory
│   ├── world/           ✅ TileType, Tile, Room, Corridor, Level
│   ├── combat/          ✅ CombatResult
│   └── session/         ⚠️ GameStats (на ревью) | LeaderboardEntry, GameSession — ⬜
│
└── ports/               📂 пустой — заполняется в День 4
```

**Удалено (было неверно):**
- ~~`domain/generation/`~~ → принадлежит `gameplay/generation/`
- ~~`domain/logic/`~~ → принадлежит `gameplay/`

---

## 3. Целевая структура (финал)

```
ru.school21.rogue.domain
│
├── model/
│   ├── character/
│   │   ├── Actor.java              # абстрактный базовый класс персонажа
│   │   ├── Player.java             # игрок
│   │   ├── Enemy.java              # sealed-класс врага
│   │   ├── EnemyType.java          # enum: ZOMBIE, VAMPIRE, GHOST, OGR, SNAKE_MAGE
│   │   ├── Zombie.java
│   │   ├── Vampire.java
│   │   ├── Ghost.java
│   │   ├── Ogr.java
│   │   └── SnakeMage.java
│   │
│   ├── common/
│   │   ├── Position.java           # record (x, y) — уже есть
│   │   ├── Damageable.java         # interface: takeDamage(int), isAlive()  ← создать
│   │   └── AttackEffect.java       # interface: applyEffect(Actor)          ← уже есть
│   │   # Movable.java              # ← УДАЛИТЬ (алгоритм движения = gameplay)
│   │
│   ├── item/
│   │   ├── Item.java               # абстрактный класс предмета
│   │   ├── Food.java               # healAmount
│   │   ├── Weapon.java             # damageBonus
│   │   ├── Scroll.java             # bonusStrength, bonusAgility (постоянно)
│   │   ├── Elixir.java             # bonusStrength, bonusAgility, duration (временно)
│   │   └── Treasure.java           # value (очки)
│   │
│   ├── inventory/
│   │   └── Inventory.java          # хранит до 9 предметов каждого типа
│   │
│   ├── world/
│   │   ├── TileType.java           # enum: FLOOR, WALL, CORRIDOR, EXIT, EMPTY
│   │   ├── Tile.java               # type, visible, explored
│   │   ├── Room.java               # topLeft, width, height, startRoom, exitRoom
│   │   ├── Corridor.java           # fromIndex, toIndex, path
│   │   └── Level.java              # number, tiles[][], rooms, corridors, enemies, items, exit
│   │
│   ├── combat/
│   │   └── CombatResult.java       # record: hit, damage, killed
│   │
│   └── session/
│       ├── GameStats.java          # gold, kills, itemsUsed, stepsTaken
│       ├── LeaderboardEntry.java   # playerName, gold, level, date
│       └── GameSession.java        # player + currentLevel + stats
│
└── ports/
    ├── SaveRepository.java         # interface: save, load, hasSave, deleteSave
    └── LeaderboardRepository.java  # interface: save, getTop(int)
```

---

## 4. Дневные миссии

> Миссия закрыта, когда код написан, компилируется (`./gradlew build`) и логика проверена вручную.

---

### ДЕНЬ 1 — Починить существующее

#### Миссия 1.1 — Удалить `Movable.java`

**Проблема:** `Movable.move(Position)` — это алгоритм перемещения (куда идти).
Алгоритмы принадлежат `gameplay`, а не `domain`.
`Actor` уже имеет `getPosition()` / `setPosition()` — этого достаточно для domain.
`Ghost` и `Ogr` реализуют `Movable` сейчас — после удаления это тоже нужно убрать из их объявлений.

Что сделать:
1. Удалить файл `domain/model/common/Movable.java`
2. В `Ghost.java` — убрать `implements Movable` и метод `move()`
3. В `Ogr.java` — убрать `implements Movable` и метод `move()`
4. В `SnakeMage.java` — убрать `implements Movable` и метод `move()`

**Критерий закрытия:** `./gradlew build` — без ошибок. `Movable.java` не существует.

---

#### Миссия 1.2 — Создать `Damageable.java`

Файл: `domain/model/common/Damageable.java`

```
interface Damageable {
    void takeDamage(int damage);
    boolean isAlive();
}
```

Реализует: `Actor` (и все наследники через него).

**Критерий закрытия:** интерфейс создан, `Actor` объявляет `implements Damageable`.

---

#### Миссия 1.3 — Починить `Actor.java`

Текущие проблемы:
- `attack()` возвращает `0` вместо базового урона
- `takeDamage()` пустой — HP не уменьшается
- `setHealth()` принимает отрицательные значения — нарушение инварианта
- Нет метода `isAlive()`

Что исправить (направление):
- `setHealth(int h)` → `this.health = Math.max(0, h)`
- `takeDamage(int d)` → `setHealth(health - d)` — конкретная реализация в `Actor`, не абстрактная
- `isAlive()` → `return health > 0`
- `attack()` → убрать `abstract`, дать реализацию `return strength` (базовый урон)

**Критерий закрытия:** `Actor` реализует `Damageable`. `./gradlew build` — без ошибок.

---

#### Миссия 1.4 — Починить видимость классов врагов

**Проблема:** `Ghost`, `Ogr`, `Zombie`, `Vampire`, `SnakeMage` объявлены без `public`.
Из пакета `gameplay` они недоступны.

Что исправить: добавить `public` к объявлению каждого класса.

**Критерий закрытия:** все 5 классов — `public final class`. `./gradlew build` — без ошибок.

---

### ДЕНЬ 2 — Предметы и инвентарь

#### Миссия 2.1 — `Item.java` (базовый класс)

Файл: `domain/model/item/Item.java`

Поля:
- `String name`
- `char symbol`

Абстрактный класс. Без абстрактных методов — конкретные действия (применить, использовать) принадлежат `gameplay/item/ItemSystem`.

**Критерий закрытия:** класс создан, компилируется.

---

#### Миссия 2.2 — Пять подтипов предметов

Создать в `domain/model/item/`:

| Файл | Поля |
|------|------|
| `Food.java` | `int healAmount` |
| `Weapon.java` | `int damageBonus` |
| `Treasure.java` | `int value` |
| `Scroll.java` | `int bonusStrength`, `int bonusAgility` |
| `Elixir.java` | `int bonusStrength`, `int bonusAgility`, `int duration` |

Каждый — `public final class`, extends `Item`.

**Критерий закрытия:** все 5 файлов созданы. `./gradlew build` — без ошибок.

---

#### Миссия 2.3 — `Inventory.java`

Файл: `domain/model/inventory/Inventory.java`

Поля:
- `Map<Class<? extends Item>, List<Item>> items`

Методы (только управление состоянием, никакой логики применения):
- `boolean canAdd(Item item)` — проверяет лимит (9 каждого типа)
- `void add(Item item)` — добавляет, если `canAdd`
- `void remove(Item item)` — удаляет
- `List<Item> getByType(Class<? extends Item> type)` — возвращает список

**Критерий закрытия:** создан, компилируется, `canAdd` возвращает `false` при 9 предметах одного типа.

---

### ДЕНЬ 3 — Мир и бой

#### Миссия 3.1 — `TileType.java` и `Tile.java`

`TileType.java` — enum: `FLOOR`, `WALL`, `CORRIDOR`, `EXIT`, `EMPTY`

`Tile.java` — поля:
- `TileType type`
- `boolean visible`
- `boolean explored`

**Критерий закрытия:** оба файла созданы, компилируются.

---

#### Миссия 3.2 — `Room.java` и `Corridor.java`

`Room.java` — поля:
- `Position topLeft`
- `int width`, `int height`
- `boolean startRoom`, `boolean exitRoom`

`Corridor.java` — поля:
- `int fromIndex` — индекс первой комнаты в списке
- `int toIndex` — индекс второй комнаты в списке
- `List<Position> path` — клетки коридора

**Критерий закрытия:** оба файла созданы, компилируются.

---

#### Миссия 3.3 — `Level.java`

Файл: `domain/model/world/Level.java`

Поля:
- `int number` — номер уровня (1–21)
- `Tile[][] tiles` — двумерная карта
- `List<Room> rooms` — 9 комнат (3×3)
- `List<Corridor> corridors`
- `List<Enemy> enemies`
- `List<Item> items` — предметы на полу
- `Position playerStart`
- `Position exit`

**Критерий закрытия:** класс создан. Можно создать экземпляр `new Level(...)` без ошибок компиляции.

---

#### Миссия 3.4 — `CombatResult.java`

Файл: `domain/model/combat/CombatResult.java`

Реализация: `record` (Java 21):
- `boolean hit`
- `int damage`
- `boolean killed`

Используется в `gameplay/combat/CombatSystem` как возвращаемый результат боя. Domain только определяет структуру результата.

**Критерий закрытия:** record создан. `./gradlew build` — без ошибок.

---

### ДЕНЬ 4 — Сессия и контракты

#### Миссия 4.1 — `GameStats.java`

Файл: `domain/model/session/GameStats.java`

Поля:
- `int currentLevelNumber`
- `int gold`
- `int kills`
- `int itemsUsed`
- `int stepsTaken`

**Критерий закрытия:** класс создан, компилируется.

---

#### Миссия 4.2 — `LeaderboardEntry.java`

Файл: `domain/model/session/LeaderboardEntry.java`

Поля:
- `String playerName`
- `int gold`
- `int level`
- `String date`

**Критерий закрытия:** класс создан, компилируется.

---

#### Миссия 4.3 — `GameSession.java`

Файл: `domain/model/session/GameSession.java`

Поля:
- `Player player`
- `Level currentLevel`
- `GameStats stats`

Это главный объект состояния всей игры. `data` слой сохраняет именно его.

**Критерий закрытия:** класс создан. Можно собрать `new GameSession(player, level, stats)` без ошибок.

---

#### Миссия 4.4 — `SaveRepository.java`

Файл: `domain/ports/SaveRepository.java`

```
public interface SaveRepository {
    void save(GameSession session);
    GameSession load();
    boolean hasSave();
    void deleteSave();
}
```

**Критерий закрытия:** интерфейс создан. `data` слой сможет его реализовать.

---

#### Миссия 4.5 — `LeaderboardRepository.java`

Файл: `domain/ports/LeaderboardRepository.java`

```
public interface LeaderboardRepository {
    void save(LeaderboardEntry entry);
    List<LeaderboardEntry> getTop(int limit);
}
```

**Критерий закрытия:** интерфейс создан. `data` слой сможет его реализовать.

---

### ДЕНЬ 5 — Финальная проверка

#### Миссия 5.1 — Полный билд

Запустить: `./gradlew build`

Ожидаемый результат: **BUILD SUCCESSFUL**, 0 ошибок компиляции.

**Критерий закрытия:** билд зелёный.

---

#### Миссия 5.2 — Проверка изоляции domain

Убедиться, что ни один файл в `domain/` не содержит:
- `import com.googlecode.lanterna`
- `import com.fasterxml.jackson`
- `import ru.school21.rogue.gameplay`
- `import ru.school21.rogue.presentation`
- `import ru.school21.rogue.data`

Способ: поиск по `src/main/java/ru/school21/rogue/domain/` на наличие этих строк.

**Критерий закрытия:** ни одного запрещённого импорта не найдено.

---

#### Миссия 5.3 — Уведомить data слой о готовности портов

После закрытия миссий 4.4 и 4.5 — domain ports готовы.
`data` слой снимает свой блокер (Миссия 1.1 из `data_plan.md`).

**Критерий закрытия:** `domain.ports.SaveRepository` и `domain.ports.LeaderboardRepository` существуют и компилируются.

---

## 5. Сводная таблица миссий

| День | Миссия | Файл/Действие | Критерий |
|------|--------|---------------|----------|
| 1 | 1.1 Удалить Movable | удалить файл + убрать implements | build OK |
| 1 | 1.2 Damageable | `common/Damageable.java` | Actor implements Damageable |
| 1 | 1.3 Починить Actor | инвариант HP, isAlive, attack | build OK |
| 1 | 1.4 Видимость врагов | public на 5 классах | build OK |
| 2 | 2.1 Item | `item/Item.java` | компилируется |
| 2 | 2.2 Подтипы предметов | Food, Weapon, Treasure, Scroll, Elixir | все компилируются |
| 2 | 2.3 Inventory | `inventory/Inventory.java` | canAdd работает |
| 3 | 3.1 TileType + Tile | `world/TileType.java`, `world/Tile.java` | компилируются |
| 3 | 3.2 Room + Corridor | `world/Room.java`, `world/Corridor.java` | компилируются |
| 3 | 3.3 Level | `world/Level.java` | экземпляр создаётся |
| 3 | 3.4 CombatResult | `combat/CombatResult.java` (record) | build OK |
| 4 | 4.1 GameStats | `session/GameStats.java` | компилируется |
| 4 | 4.2 LeaderboardEntry | `session/LeaderboardEntry.java` | компилируется |
| 4 | 4.3 GameSession | `session/GameSession.java` | экземпляр создаётся |
| 4 | 4.4 SaveRepository | `ports/SaveRepository.java` | интерфейс готов |
| 4 | 4.5 LeaderboardRepository | `ports/LeaderboardRepository.java` | интерфейс готов |
| 5 | 5.1 Полный билд | `./gradlew build` | BUILD SUCCESSFUL |
| 5 | 5.2 Изоляция domain | grep запрещённых импортов | 0 нарушений |
| 5 | 5.3 Уведомить data | domain ports готовы | data снимает блокер |
