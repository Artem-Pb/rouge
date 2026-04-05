# Gameplay Layer Plan — Roguelike Game

**Роль:** gameplay
**Дата:** 2026-03-26
**Версия:** 1.0
**Статус:** готов к исполнению

---

## 1. Зона ответственности

Gameplay — это **логика игры**. Здесь только:
- Игровой цикл и пошаговость
- Движение игрока и врагов
- Боевая система (формулы попадания и урона)
- AI каждого типа врага
- Процедурная генерация уровней
- Туман войны
- Логика подбора и применения предметов
- Прогрессия сложности по уровням

Gameplay **не знает** ни о Lanterna, ни о Jackson, ни о сохранении файлов.
Gameplay **зависит только от domain**.

---

## 2. Структура пакетов (создана)

```
ru.school21.rogue.gameplay
│
├── engine/
│   └── GameEngine.java             # главный цикл, координирует все системы
│
├── combat/
│   └── CombatSystem.java           # проверка попадания → расчёт урона → применение
│
├── movement/
│   ├── MovementSystem.java         # движение игрока, проверка коллизий, переход на уровень
│   └── PathFinder.java             # BFS/A* — путь врага к игроку
│
├── ai/
│   ├── EnemyAI.java                # <<interface>> стратегия поведения врага
│   └── behavior/
│       ├── ZombieAI.java           # случайное движение + преследование
│       ├── VampireAI.java          # преследование + первый удар промах
│       ├── GhostAI.java            # телепорт + невидимость
│       ├── OgreAI.java             # ход на 2 клетки + отдых после атаки
│       └── SnakeMageAI.java        # диагональное движение + усыпление
│
├── generation/
│   ├── LevelGenerator.java         # генерация уровня: 9 комнат в сетке 3×3
│   └── RoomConnector.java          # соединение комнат коридорами + BFS-проверка связности
│
├── fog/
│   └── FogOfWarSystem.java         # три состояния тайла: скрыт / исследован / виден
│
├── item/
│   └── ItemSystem.java             # подбор, применение, дроп оружия
│
└── progression/
    └── ProgressionSystem.java      # параметры врагов и предметов по номеру уровня
```

---

## 3. Блокеры

Gameplay зависит от domain. Перед реализацией должны существовать:

| Класс | Где | Нужен для |
|-------|-----|-----------|
| `GameSession` | `domain.model.session` | GameEngine |
| `Level` | `domain.model.world` | все системы |
| `Player` | `domain.model.character` | MovementSystem, CombatSystem |
| `Enemy` + подтипы | `domain.model.character` | AI |
| `CombatResult` | `domain.model.combat` | CombatSystem |
| `Item` + подтипы | `domain.model.item` | ItemSystem |
| `Inventory` | `domain.model.inventory` | ItemSystem |
| `TileType`, `Tile` | `domain.model.world` | Generation, FogOfWar |

---

## 4. Формулы и алгоритмы

### Формула попадания (hit check)
```
hitChance = attacker.agility / (attacker.agility + defender.agility)
hit = Random.nextDouble() < hitChance
```
Чем выше ловкость атакующего относительно защищающегося — тем выше шанс попасть.

### Формула урона (damage)
```
// Без оружия:
damage = attacker.strength

// С оружием:
damage = attacker.strength + weapon.damageBonus
```

### Дроп сокровищ с врага
```
treasure = (enemy.hostility + enemy.strength + enemy.agility + enemy.health) / 4
         + Random.nextInt(levelNumber)
```
Чем глубже уровень и сложнее враг — тем больше дроп.

---

## 5. Поведение врагов

| Враг | Патрулирование | Преследование | Спецмеханика |
|------|----------------|---------------|--------------|
| Zombie | случайное движение по комнате | BFS к игроку | — |
| Vampire | случайное движение | BFS к игроку | первый удар по нему — всегда промах; при попадании снижает maxHP игрока |
| Ghost | телепорт в случайную позицию комнаты | BFS к игроку | периодически isInvisible=true; виден только в бою |
| Ogr | движение на 2 клетки за ход | BFS к игроку | после атаки isResting=true на 1 ход; следующая атака — гарантированное попадание |
| SnakeMage | диагональное движение, смена стороны | BFS к игроку | при успешной атаке: вероятность setSleeping(true) игроку на 1 ход |

**Общее правило преследования:** если путь к игроку существует → BFS. Если пути нет → патрулирование по паттерну.

---

## 6. Дневные миссии

> Миссия закрыта, когда класс написан, компилируется (`./gradlew build`) и логика проверена вручную или тестом.

---

### ДЕНЬ 1 — Предусловие: domain готов

#### Миссия 1.1 — Проверить готовность domain
Убедиться, что существуют и компилируются:
- `GameSession`, `Level`, `Player`, `Enemy` (и 5 подтипов — `public`)
- `CombatResult` (record)
- `Item` + `Food`, `Weapon`, `Scroll`, `Elixir`, `Treasure`
- `Inventory`, `TileType`, `Tile`, `Room`, `Corridor`

**Критерий закрытия:** `./gradlew build` — без ошибок. Domain готов.

---

### ДЕНЬ 2 — Интерфейс AI и PathFinder

#### Миссия 2.1 — `EnemyAI.java`

Файл: `gameplay/ai/EnemyAI.java`

```
public interface EnemyAI {
    void takeTurn(Enemy enemy, Level level, Player player);
}
```

Каждая реализация получает врага, уровень и игрока. Изменяет состояние врага (позиция, флаги).
Не возвращает ничего — эффекты применяются напрямую к domain-объектам.

**Критерий закрытия:** интерфейс создан, компилируется.

---

#### Миссия 2.2 — `PathFinder.java`

Файл: `gameplay/movement/PathFinder.java`

Алгоритм: BFS по тайлам `Level.tiles`.
Проходимые тайлы: `FLOOR`, `CORRIDOR`, `EXIT`.

Методы:
- `List<Position> findPath(Level level, Position from, Position to)`
  — возвращает список позиций от `from` до `to`, исключая `from`.
  — если пути нет → возвращает пустой список.
- `boolean hasPath(Level level, Position from, Position to)`
  — обёртка над `findPath`, для быстрой проверки.

**Критерий закрытия:** BFS находит путь в тестовой карте. Возвращает пустой список, если путь невозможен.

---

### ДЕНЬ 3 — Генерация уровней

#### Миссия 3.1 — `LevelGenerator.java`

Файл: `gameplay/generation/LevelGenerator.java`

Алгоритм генерации:
1. Разбить карту на сетку 3×3 секций равного размера.
2. В каждой секции случайно разместить комнату (случайный размер и позиция внутри секции).
3. Отметить одну комнату как `startRoom`, одну — как `exitRoom` (не одна и та же).
4. Заполнить `Tile[][] tiles`: WALL по умолчанию, FLOOR внутри комнат.
5. Передать управление `RoomConnector`.
6. Расставить на уровне врагов и предметы через `ProgressionSystem`.

Метод: `Level generate(int levelNumber)`

**Критерий закрытия:** метод возвращает `Level` с 9 комнатами, startRoom и exitRoom установлены, тайлы заполнены.

---

#### Миссия 3.2 — `RoomConnector.java`

Файл: `gameplay/generation/RoomConnector.java`

Алгоритм:
1. Взять список из 9 комнат.
2. Построить минимальное остовное дерево (Prim или случайный spanning tree) — гарантирует связность.
3. Для каждого ребра дерева: провести коридор L-образной формой (горизонт → вертикаль или наоборот).
4. Записать пути коридоров в `List<Position>` и в `Tile[][] tiles` (тип `CORRIDOR`).
5. BFS-проверка: из startRoom должны быть достижимы все остальные комнаты. Если нет — повторить генерацию.

Метод: `void connect(Level level)`

**Критерий закрытия:** после вызова все 9 комнат достижимы из startRoom через BFS. Тайлы коридоров прописаны в `tiles`.

---

### ДЕНЬ 4 — Боевая система

#### Миссия 4.1 — `CombatSystem.java`

Файл: `gameplay/combat/CombatSystem.java`

Метод: `CombatResult calculate(Actor attacker, Actor defender)`

Этапы:
1. **Проверка попадания:**
   `hitChance = attacker.agility / (double)(attacker.agility + defender.agility)`
   `hit = Math.random() < hitChance`
2. **Расчёт урона (если попал):**
   - Если `attacker` — Player и у него есть Weapon в инвентаре:
     `damage = player.strength + weapon.damageBonus`
   - Иначе: `damage = attacker.strength`
3. **Применение урона:**
   `defender.takeDamage(damage)`
4. **Вернуть:** `new CombatResult(hit, damage, !defender.isAlive())`

**Критерий закрытия:** `calculate()` возвращает корректный `CombatResult`. `defender.health` уменьшается при попадании.

---

#### Миссия 4.2 — Спецмеханики врагов в бою

Добавить обработку в `CombatSystem` или в отдельный метод `applyPostCombatEffects`:

| Враг | Эффект после успешной атаки |
|------|-----------------------------|
| Vampire | `player.setMaxHealth(player.getMaxHealth() - vampire.strength)` |
| SnakeMage | с вероятностью 30%: `player.setSleeping(true)` |
| Ogr | после атаки: `ogr.setResting(true)` (пропускает следующий ход) |

**Критерий закрытия:** Vampire уменьшает maxHP, SnakeMage усыпляет с нужной вероятностью, Ogr отдыхает.

---

### ДЕНЬ 5 — Движение и AI

#### Миссия 5.1 — `MovementSystem.java`

Файл: `gameplay/movement/MovementSystem.java`

Метод: `MoveResult movePlayer(Player player, Level level, Direction direction)`

Где `Direction` — enum: `UP`, `DOWN`, `LEFT`, `RIGHT` (WASD).

Логика:
1. Вычислить целевую позицию: `target = player.position.translate(dx, dy)`
2. Если `target` — стена → `MoveResult.BLOCKED`
3. Если `target` — позиция врага → инициировать бой (вернуть `MoveResult.COMBAT`)
4. Если `target` — предмет → подобрать через `ItemSystem` → `MoveResult.ITEM_PICKED`
5. Если `target` — EXIT → `MoveResult.LEVEL_EXIT`
6. Иначе → `player.setPosition(target)` → `MoveResult.MOVED`

Вспомогательные enum и record:
- `Direction` (UP, DOWN, LEFT, RIGHT) с методом `toDelta(): Position`
- `MoveResult` (MOVED, BLOCKED, COMBAT, ITEM_PICKED, LEVEL_EXIT)

**Критерий закрытия:** игрок не проходит через стены. При столкновении с врагом возвращает COMBAT.

---

#### Миссия 5.2 — `ZombieAI.java`

Файл: `gameplay/ai/behavior/ZombieAI.java`

Логика `takeTurn`:
1. Если расстояние до игрока ≤ `zombie.hostility` → использовать `PathFinder.findPath()` → сделать шаг
2. Иначе → случайный шаг на соседнюю проходимую клетку
3. Если следующая позиция — игрок → инициировать бой через `CombatSystem`

**Критерий закрытия:** зомби преследует игрока в радиусе hostility, бродит случайно за ним.

---

#### Миссия 5.3 — `VampireAI.java`

Файл: `gameplay/ai/behavior/VampireAI.java`

Дополнительное состояние (хранится в `Vampire`): `boolean firstStrike = true`

Логика `takeTurn`:
1. Преследование аналогично Zombie
2. При атаке: если `firstStrike == true` → бой пропускается (промах гарантирован), `firstStrike = false`
3. При успешном попадании → `vampire.applyEffect(player)` (снижает maxHP)

**Критерий закрытия:** первый удар по вампиру всегда промах. Второй и далее — по формуле.

---

#### Миссия 5.4 — `GhostAI.java`

Файл: `gameplay/ai/behavior/GhostAI.java`

Логика `takeTurn`:
1. Если не в бою (игрок не рядом):
   - Каждые 2 хода → `ghost.setInvisible(!ghost.isInvisible())`
   - Телепорт: `ghost.setPosition(randomFloorTileInRoom())`
2. Если игрок в радиусе hostility → `ghost.setInvisible(false)` → преследование по BFS

**Критерий закрытия:** Ghost телепортируется и переключает невидимость вне боя.

---

#### Миссия 5.5 — `OgreAI.java`

Файл: `gameplay/ai/behavior/OgreAI.java`

Логика `takeTurn`:
1. Если `ogr.isResting() == true` → пропустить ход, `ogr.setResting(false)`
2. Иначе:
   - Сделать **2 шага** за один ход (два вызова PathFinder шаг за шагом)
   - Если после любого шага рядом игрок → атаковать → `ogr.setResting(true)`

**Критерий закрытия:** Ogr делает 2 шага, после атаки пропускает ход.

---

#### Миссия 5.6 — `SnakeMageAI.java`

Файл: `gameplay/ai/behavior/SnakeMageAI.java`

Внутреннее состояние: `int diagonalDirection` (1 или -1 — смена стороны)

Логика `takeTurn`:
1. Если не преследует: диагональный шаг `(+1, +diagonalDirection)`.
   Если позиция непроходима → `diagonalDirection *= -1` → попробовать снова
2. Если игрок в радиусе hostility → BFS-преследование
3. При атаке: `snakeMage.applyEffect(player)` (с вероятностью 30% — сон на 1 ход)

**Критерий закрытия:** SnakeMage движется по диагонали, меняет направление у стен.

---

### ДЕНЬ 6 — Предметы и туман войны

#### Миссия 6.1 — `ItemSystem.java`

Файл: `gameplay/item/ItemSystem.java`

Методы:

`boolean pickup(Player player, Item item, Level level)`
- Если `inventory.canAdd(item)` → `inventory.add(item)` → убрать с уровня → `true`
- Иначе → `false` (рюкзак полон)

`void useFood(Player player, Food food)`
- `player.setHealth(min(player.health + food.healAmount, player.maxHealth))`
- `inventory.remove(food)`

`void useScroll(Player player, Scroll scroll)`
- `player.setStrength(player.strength + scroll.bonusStrength)`
- `player.setAgility(player.agility + scroll.bonusAgility)`
- `player.setMaxHealth(player.maxHealth + scroll.bonusMaxHealth)` + увеличить health на ту же величину
- `inventory.remove(scroll)`

`void useElixir(Player player, Elixir elixir, GameEngine engine)`
- Применить временный бонус
- Запланировать откат через `elixir.duration` ходов
- `inventory.remove(elixir)`
- Если после отката HP ≤ 0 → установить HP = 1

`void equipWeapon(Player player, Weapon weapon, Level level)`
- Если уже есть оружие → дроп текущего на соседнюю клетку через `Level`
- Установить новое оружие

`void unequipWeapon(Player player, Level level)`
- Дроп текущего оружия на соседнюю клетку, не удаляя из инвентаря

**Критерий закрытия:** подбор, применение еды, свитка и оружия работают корректно.

---

#### Миссия 6.2 — `FogOfWarSystem.java`

Файл: `gameplay/fog/FogOfWarSystem.java`

Три состояния тайла (хранятся в `Tile`):
- `visible = false, explored = false` → **скрыт** (не отображается)
- `visible = false, explored = true` → **исследован** (отображаются только стены)
- `visible = true, explored = true` → **виден** (отображается всё)

Метод: `void updateVisibility(Level level, Player player)`

Логика:
1. Все тайлы текущей комнаты игрока → `visible = true`, `explored = true`
2. Тайлы вне текущей комнаты → `visible = false` (explored не трогать)
3. Коридор, по которому идёт игрок → `visible = true`
4. Соседние комнаты вдоль прямой видимости из коридора → применить Ray Casting (упрощённо: если есть прямая линия тайлов без стен → виден вход в следующую комнату)

**Критерий закрытия:** тайлы комнаты игрока visible=true. После выхода — explored=true, visible=false.

---

### ДЕНЬ 7 — Прогрессия и главный цикл

#### Миссия 7.1 — `ProgressionSystem.java`

Файл: `gameplay/progression/ProgressionSystem.java`

Метод: `void populate(Level level, int levelNumber)`

Логика масштабирования по `levelNumber` (1–21):
- Количество врагов: `2 + levelNumber / 3` (растёт с глубиной)
- Сложность врагов: базовые характеристики умножаются на `1 + levelNumber * 0.1`
- Количество предметов (полезных): `5 - levelNumber / 5` (уменьшается)
- Количество сокровищ: `levelNumber / 2` (растёт)

Типы врагов по уровням:
- Уровни 1–5: Zombie, Ghost
- Уровни 6–10: + Vampire
- Уровни 11–15: + SnakeMage
- Уровни 16–21: + Ogr (все типы)

**Критерий закрытия:** уровень 1 содержит 2–3 врага слабых типов. Уровень 21 — 9+ врагов всех типов с повышенными характеристиками.

---

#### Миссия 7.2 — `GameEngine.java`

Файл: `gameplay/engine/GameEngine.java`

Главный класс, связывающий все системы. Получает через конструктор:
- `GameSession session`
- `CombatSystem combatSystem`
- `MovementSystem movementSystem`
- `ItemSystem itemSystem`
- `FogOfWarSystem fogSystem`
- `LevelGenerator levelGenerator`
- `ProgressionSystem progressionSystem`

Метод: `void processTurn(Direction playerDirection)`

Порядок обработки хода:
1. Если игрок спит (`isSleeping`) → пропустить его ход, разбудить
2. Обработать движение игрока → `MovementSystem.movePlayer()`
3. Если `LEVEL_EXIT` → загрузить следующий уровень
4. Для каждого живого врага → вызвать соответствующий AI
5. Обновить туман войны → `FogOfWarSystem.updateVisibility()`
6. Обновить активные эффекты (эликсиры)
7. Проверить условие смерти игрока → если `!player.isAlive()` → `GameOver`

**Критерий закрытия:** один вызов `processTurn()` корректно меняет состояние `GameSession`. Враги ходят после игрока.

---

### ДЕНЬ 8 — Тесты

#### Миссия 8.1 — Тест `PathFinder`

Файл: `test/.../gameplay/PathFinderTest.java`

Сценарии:
1. Путь существует → возвращает непустой список позиций
2. Путь заблокирован → возвращает пустой список
3. `from == to` → возвращает пустой список

**Критерий закрытия:** `./gradlew test` — зелёный.

---

#### Миссия 8.2 — Тест `LevelGenerator`

Файл: `test/.../gameplay/LevelGeneratorTest.java`

Сценарии:
1. Уровень содержит ровно 9 комнат
2. Ровно 1 startRoom и 1 exitRoom
3. Все комнаты достижимы из startRoom (BFS-проверка)
4. `tiles` не содержит null

**Критерий закрытия:** `./gradlew test` — зелёный.

---

#### Миссия 8.3 — Тест `CombatSystem`

Файл: `test/.../gameplay/CombatSystemTest.java`

Сценарии:
1. После 1000 атак с равной ловкостью → ~50% попаданий (±10%)
2. При попадании `defender.health < initialHealth`
3. При `damage >= defender.health` → `result.killed == true`

**Критерий закрытия:** `./gradlew test` — зелёный.

---

## 7. Сводная таблица миссий

| День | Миссия | Файл | Критерий |
|------|--------|------|----------|
| 1 | 1.1 Проверить domain | — | build OK |
| 2 | 2.1 EnemyAI | `ai/EnemyAI.java` | компилируется |
| 2 | 2.2 PathFinder | `movement/PathFinder.java` | BFS находит путь |
| 3 | 3.1 LevelGenerator | `generation/LevelGenerator.java` | 9 комнат, startRoom, exitRoom |
| 3 | 3.2 RoomConnector | `generation/RoomConnector.java` | все комнаты связаны |
| 4 | 4.1 CombatSystem | `combat/CombatSystem.java` | формула + damage |
| 4 | 4.2 Спецмеханики | в CombatSystem | Vampire, SnakeMage, Ogr |
| 5 | 5.1 MovementSystem | `movement/MovementSystem.java` | WASD + коллизии |
| 5 | 5.2 ZombieAI | `ai/behavior/ZombieAI.java` | патруль + преследование |
| 5 | 5.3 VampireAI | `ai/behavior/VampireAI.java` | первый удар промах |
| 5 | 5.4 GhostAI | `ai/behavior/GhostAI.java` | телепорт + невидимость |
| 5 | 5.5 OgreAI | `ai/behavior/OgreAI.java` | 2 шага + отдых |
| 5 | 5.6 SnakeMageAI | `ai/behavior/SnakeMageAI.java` | диагональ + сон |
| 6 | 6.1 ItemSystem | `item/ItemSystem.java` | подбор + применение |
| 6 | 6.2 FogOfWarSystem | `fog/FogOfWarSystem.java` | 3 состояния тайла |
| 7 | 7.1 ProgressionSystem | `progression/ProgressionSystem.java` | масштабирование 1–21 |
| 7 | 7.2 GameEngine | `engine/GameEngine.java` | полный ход обработан |
| 8 | 8.1 Тест PathFinder | `PathFinderTest.java` | ✅ green |
| 8 | 8.2 Тест LevelGenerator | `LevelGeneratorTest.java` | ✅ green |
| 8 | 8.3 Тест CombatSystem | `CombatSystemTest.java` | ✅ green |
