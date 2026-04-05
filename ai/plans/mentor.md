# Mentor Journal — Roguelike Game

**Роль:** mentor
**Дата старта:** 2026-03-26
**Версия:** 1.0

---

## ПРАВИЛО РАБОТЫ

Когда ты приходишь без `role:` — ты идёшь к ментору.
Ментор смотрит журнал, называет **текущую активную миссию**, объясняет что делать и почему.
После выполнения — ты говоришь "готово" или показываешь код. Ментор проверяет, ставит ✅ и называет следующую.

### ⚠️ ФОРМАТ ВЗАИМОДЕЙСТВИЯ (обязателен)

**Ментор:**
- Объясняет теорию и архитектуру словами — без кода из проекта
- Даёт задачу → ученик идёт в нужный агент (`role: domain`, `role: gameplay`, etc.) и пишет сам
- Проверяет результат и даёт обратную связь
- Ведёт журнал — **после каждой закрытой миссии**

**Ученик:**
- Общается с ментором (обсуждение, вопросы, "что делать")
- Инструменты разработки добывает сам (идёт к агентам: `role: domain`, `role: gameplay`, etc.)
- НЕ ждёт что ментор напишет код за него

**Нарушения формата которые надо пресекать:**
- Ментор сам пишет код из проекта → стоп
- Ментор забывает обновить журнал после ✅ → стоп, обновить немедленно
- Ученик пишет код не обратившись к агенту → напомнить про роли

---

## КАРТА ПРОЕКТА — РЕАЛЬНОЕ СОСТОЯНИЕ

```
Слой          Пакеты       Файлов    Статус
──────────────────────────────────────────────
domain        ✅ созданы    26/32    🔴 в работе
gameplay      ✅ созданы    0/20     ⬜ ждёт domain
data          ✅ созданы    0/15     ⬜ ждёт domain ports
presentation  ✅ созданы    0/23     ⬜ ждёт gameplay
──────────────────────────────────────────────
ИТОГО                       26/70   ~37% готово
```

---

## ЗАКОН ЗАВИСИМОСТЕЙ

```
domain  ←──  gameplay  ←──  presentation
  ↑
data
```

**Без завершения domain — ничего остального писать нельзя.**
Это не рекомендация. Это физическое ограничение: классы не скомпилируются.

---

## ГЛОБАЛЬНАЯ ОЧЕРЁДНОСТЬ ФАЗ

```
ФАЗА 1 ── DOMAIN         ← ТЫ ЗДЕСЬ
ФАЗА 2 ── GAMEPLAY + DATA (параллельно, оба ждут domain)
ФАЗА 3 ── PRESENTATION
ФАЗА 4 ── ИНТЕГРАЦИЯ (Main.java wiring + smoke-тест)
```

---

## ФАЗА 1 — DOMAIN

> Цель: 32 файла, чистая модель, полный ./gradlew build зелёный.
> План: `ai/plans/domain_plan.md`

### ДЕНЬ D1 — Починить существующее

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| D1.1 | Удалить `Movable.java` + убрать `implements Movable` у Ghost, Ogr, SnakeMage | `common/Movable.java` | ✅ |
| D1.2 | Создать `Damageable.java` + Actor implements Damageable | `common/Damageable.java` | ✅ |
| D1.3 | Починить `Actor`: HP-инвариант, `isAlive()`, `takeDamage()` | `Actor.java` | ✅ |
| D1.4 | Сделать всех врагов `public`: Ghost, Ogr, Zombie, Vampire, SnakeMage | 5 файлов | ✅ |

### ДЕНЬ D2 — Предметы и инвентарь

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| D2.1 | `Item.java` — абстрактный класс (name, symbol) | `item/Item.java` | ✅ |
| D2.2 | 5 подтипов: Food, Weapon, Scroll, Elixir, Treasure | `item/` | ✅ |
| D2.3 | `Inventory.java` — canAdd, add, remove, getByType | `inventory/Inventory.java` | ✅ |

### ДЕНЬ D3 — Мир и бой

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| D3.1 | `TileType.java` (enum) + `Tile.java` | `world/` | ✅ |
| D3.2 | `Room.java` + `Corridor.java` | `world/` | ✅ |
| D3.3 | `Level.java` | `world/Level.java` | ✅ |
| D3.4 | `CombatResult.java` (record) | `combat/CombatResult.java` | ✅ |

### ДЕНЬ D4 — Сессия и контракты

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| D4.1 | `GameStats.java` | `session/GameStats.java` | ✅ |
| D4.2 | `LeaderboardEntry.java` | `session/LeaderboardEntry.java` | ✅ |
| D4.3 | `GameSession.java` | `session/GameSession.java` | ✅ |
| D4.4 | `SaveRepository.java` (interface) | `ports/SaveRepository.java` | ✅ |
| D4.5 | `LeaderboardRepository.java` (interface) | `ports/LeaderboardRepository.java` | ✅ |

### ДЕНЬ D5 — Финальная проверка domain

| # | Миссия | Критерий | Статус |
|---|--------|----------|--------|
| D5.1 | `./gradlew build` зелёный | BUILD SUCCESSFUL | ✅ |
| D5.2 | Grep: 0 запрещённых импортов в domain | нет Lanterna/Jackson | ✅ |
| D5.3 | **ФАЗА 1 ЗАКРЫТА** → разблокирует Gameplay + Data | — | ✅ |

---

## ФАЗА 2A — GAMEPLAY

> Цель: вся игровая логика, 20 файлов.
> Стартует после: D5.3 ✅
> План: `ai/plans/gameplay_plan.md`

### ДЕНЬ G2 — AI-интерфейс и PathFinder

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| G2.1 | `EnemyAI.java` (interface) | `ai/EnemyAI.java` | ✅ |
| G2.2 | `PathFinder.java` (BFS) | `movement/PathFinder.java` | ✅ |

### ДЕНЬ G3 — Генерация уровней

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| G3.1 | `LevelGenerator.java` | `generation/LevelGenerator.java` | ⬜ |
| G3.2 | `RoomConnector.java` + BFS-проверка связности | `generation/RoomConnector.java` | ⬜ |

### ДЕНЬ G4 — Боевая система

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| G4.1 | `CombatSystem.java` — формула попадания + урон | `combat/CombatSystem.java` | ⬜ |
| G4.2 | Спецмеханики: Vampire/SnakeMage/Ogr | в CombatSystem | ⬜ |

### ДЕНЬ G5 — Движение и AI врагов

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| G5.1 | `MovementSystem.java` + Direction/MoveResult | `movement/MovementSystem.java` | ⬜ |
| G5.2 | `ZombieAI.java` | `ai/behavior/ZombieAI.java` | ⬜ |
| G5.3 | `VampireAI.java` (первый удар промах) | `ai/behavior/VampireAI.java` | ⬜ |
| G5.4 | `GhostAI.java` (телепорт + невидимость) | `ai/behavior/GhostAI.java` | ⬜ |
| G5.5 | `OgreAI.java` (2 шага + отдых) | `ai/behavior/OgreAI.java` | ⬜ |
| G5.6 | `SnakeMageAI.java` (диагональ + сон) | `ai/behavior/SnakeMageAI.java` | ⬜ |

### ДЕНЬ G6 — Предметы и туман

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| G6.1 | `ItemSystem.java` | `item/ItemSystem.java` | ⬜ |
| G6.2 | `FogOfWarSystem.java` | `fog/FogOfWarSystem.java` | ⬜ |

### ДЕНЬ G7 — Прогрессия и движок

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| G7.1 | `ProgressionSystem.java` | `progression/ProgressionSystem.java` | ⬜ |
| G7.2 | `GameEngine.java` — главный цикл хода | `engine/GameEngine.java` | ⬜ |

### ДЕНЬ G8 — Тесты gameplay

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| G8.1 | `PathFinderTest.java` | test/ | ⬜ |
| G8.2 | `LevelGeneratorTest.java` | test/ | ⬜ |
| G8.3 | `CombatSystemTest.java` | test/ | ⬜ |

---

## ФАЗА 2B — DATA

> Цель: сохранение/загрузка + leaderboard, 15 файлов.
> Стартует после: D4.4 + D4.5 ✅ (domain ports готовы)
> Можно делать параллельно с Gameplay начиная с Дня D4.
> План: `ai/plans/data_plan.md`

### ДЕНЬ DA1 — Фундамент

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| DA1.1 | Проверить domain ports (блокер снят?) | — | ⬜ |
| DA1.2 | Структура папок data | `data/save/dto/`, `data/leaderboard/` | ⬜ |
| DA1.3 | `JacksonConfig.java` | `data/config/JacksonConfig.java` | ⬜ |

### ДЕНЬ DA2 — DTO объекты

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| DA2.1 | `ItemDto.java` + `PositionDto.java` | `data/save/dto/` | ⬜ |
| DA2.2 | `PlayerDto.java` + `InventoryDto.java` | `data/save/dto/` | ⬜ |
| DA2.3 | `EnemyDto.java` | `data/save/dto/` | ⬜ |
| DA2.4 | `TileDto`, `RoomDto`, `CorridorDto` | `data/save/dto/` | ⬜ |
| DA2.5 | `LevelDto.java` | `data/save/dto/` | ⬜ |
| DA2.6 | `GameStatsDto`, `GameSessionDto`, `LeaderboardEntryDto` | `data/save/dto/` + `leaderboard/dto/` | ⬜ |

### ДЕНЬ DA3 — Репозитории

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| DA3.1 | `SessionMapper.java` | `data/save/SessionMapper.java` | ⬜ |
| DA3.2 | `JsonSaveRepository.java` | `data/save/JsonSaveRepository.java` | ⬜ |
| DA3.3 | `JsonLeaderboardRepository.java` | `data/leaderboard/JsonLeaderboardRepository.java` | ⬜ |

### ДЕНЬ DA4 — Тесты data

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| DA4.1 | `SaveRepositoryTest.java` | test/ | ⬜ |
| DA4.2 | `LeaderboardRepositoryTest.java` | test/ | ⬜ |
| DA4.3 | Wiring в Main | `Main.java` | ⬜ |

---

## ФАЗА 3 — PRESENTATION

> Цель: терминальный UI на Lanterna, 23 файла.
> Стартует после: G7.2 ✅ (GameEngine готов)
> План: `ai/plans/presentation_plan.md`

### ДЕНЬ P1 — Bootstrap

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| P1.1 | Проверить блокеры | — | ⬜ |
| P1.2 | Lanterna bootstrap в Main | `Main.java` | ⬜ |

### ДЕНЬ P2 — Ввод

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| P2.1 | `Action.java` (enum) | `input/Action.java` | ⬜ |
| P2.2 | `KeyBinding.java` | `input/KeyBinding.java` | ⬜ |
| P2.3 | `InputHandler.java` + `LanternaInputHandler.java` | `input/` | ⬜ |

### ДЕНЬ P3 — Символы и цвета

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| P3.1 | `TileSymbol.java` (туман войны) | `symbol/TileSymbol.java` | ⬜ |
| P3.2 | `ActorSymbol.java` | `symbol/ActorSymbol.java` | ⬜ |
| P3.3 | `ItemSymbol.java` | `symbol/ItemSymbol.java` | ⬜ |

### ДЕНЬ P4 — Рендер карты

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| P4.1 | `Renderer.java` (interface) | `renderer/Renderer.java` | ⬜ |
| P4.2 | `MapRenderer.java` | `renderer/MapRenderer.java` | ⬜ |
| P4.3 | `ActorRenderer.java` | `renderer/ActorRenderer.java` | ⬜ |

### ДЕНЬ P5 — HUD и полный рендер

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| P5.1 | `HudPanel.java` | `hud/HudPanel.java` | ⬜ |
| P5.2 | `HudRenderer.java` | `renderer/HudRenderer.java` | ⬜ |
| P5.3 | `LanternaRenderer.java` | `renderer/LanternaRenderer.java` | ⬜ |

### ДЕНЬ P6 — Экраны

| # | Миссия | Файл | Статус |
|---|--------|------|--------|
| P6.1 | `Screen.java` (interface) | `screen/Screen.java` | ⬜ |
| P6.2 | `MainMenuScreen.java` | `screen/MainMenuScreen.java` | ⬜ |
| P6.3 | `GameOverScreen.java` | `screen/GameOverScreen.java` | ⬜ |
| P6.4 | `LeaderboardScreen.java` | `screen/LeaderboardScreen.java` | ⬜ |
| P6.5 | `InventoryScreen.java` | `screen/InventoryScreen.java` | ⬜ |
| P6.6 | `GameScreen.java` | `screen/GameScreen.java` | ⬜ |
| P6.7 | `ScreenManager.java` | `screen/ScreenManager.java` | ⬜ |

### ДЕНЬ P7 — Финал presentation

| # | Миссия | Критерий | Статус |
|---|--------|----------|--------|
| P7.1 | Полный wiring в `Main.java` | `./gradlew run` запускает игру | ⬜ |
| P7.2 | Grep: 0 запрещённых импортов в presentation | нет Jackson/data | ⬜ |
| P7.3 | Smoke-тест: 10 шагов вручную | без крашей | ⬜ |

---

## ФАЗА 4 — ИНТЕГРАЦИЯ

| # | Задача | Критерий | Статус |
|---|--------|----------|--------|
| I1 | Все тесты зелёные | `./gradlew test` | ⬜ |
| I2 | Полный bilд | `./gradlew build` | ⬜ |
| I3 | Smoke-тест полного прохождения | уровень 1 → 2, game over, leaderboard | ⬜ |

---

## ПРОГРЕСС-БАР

```
DOMAIN       [████████░░░░░░░░░░░░]  26/32 файлов
GAMEPLAY     [░░░░░░░░░░░░░░░░░░░░]   0/20 файлов
DATA         [░░░░░░░░░░░░░░░░░░░░]   0/15 файлов
PRESENTATION [░░░░░░░░░░░░░░░░░░░░]   0/23 файлов
─────────────────────────────────────────────────
ИТОГО        [████░░░░░░░░░░░░░░░░]  26/70 файлов  (~37%)
```

---

## ЧТО СЕЙЧАС МЕШАЕТ ВСЕМУ

Это три конкретных проблемы в существующем коде. Их надо закрыть **первыми**:

### ❌ Проблема 1 — `Movable.java` живёт в domain
Файл: `domain/model/common/Movable.java`
Почему плохо: алгоритм перемещения (куда идти) — это gameplay, не domain.
Ghost, Ogr, SnakeMage реализуют его — это надо убрать.

### ❌ Проблема 2 — Враги package-private
Ghost, Ogr, Zombie, Vampire, SnakeMage — без `public`.
Gameplay не сможет их использовать из другого пакета.

### ❌ Проблема 3 — Actor не работает
`takeDamage()` — пустой. `attack()` — возвращает 0. HP может уйти в минус.
Вся боевая система сломана на уровне domain.

---

## ПЕРВЫЕ ДЕЙСТВИЯ — ПРЯМО СЕЙЧАС

### → Миссия D1.1

**Что делать:**
1. Удалить файл `src/main/java/ru/school21/rogue/domain/model/common/Movable.java`
2. Открыть `Ghost.java` → убрать `implements Movable` из объявления класса → убрать метод `move()`
3. То же самое для `Ogr.java`
4. То же самое для `SnakeMage.java`
5. Запустить `./gradlew build`

**Почему начинаем именно с этого:**
Movable — это архитектурная ошибка, которая тянется через несколько файлов.
Чем дольше она живёт — тем больше привыкаешь к неправильной модели.
Убираем мусор первым, чтобы работать на чистом фундаменте.

**Когда сделаешь — напиши "D1.1 готово" и я обновлю журнал + дам D1.2.**

---

## ЖУРНАЛ СЕССИЙ

| Дата | Миссия | Результат |
|------|--------|-----------|
| 2026-03-26 | Структура планов создана | Все 5 планов готовы, пакеты исправлены |
| 2026-03-26 | D1.1 ✅ | Movable.java удалён, implements убран у Ghost/Ogr/SnakeMage, build зелёный |
| 2026-03-26 | D1.2–D1.4 ✅ | Damageable создан, Actor починен (HP-инвариант, isAlive, takeDamage), враги public |
| 2026-03-27 | D2.1–D2.3 ✅ | Item абстрактный, Food/Weapon/Scroll/Elixir/Treasure, Inventory с лимитом 9 |
| 2026-03-28 | D3.1–D3.4 ✅ | TileType+Tile, Room+Corridor records, Level, CombatResult record |
| 2026-03-30 | D4.1 ⚠️ | GameStats написан как record — на архитектурном ревью (проблема изменяемости + состав полей) |
| 2026-04-04 | D4.1–D4.5 ✅ | GameStats переписан (class, 8 полей), LeaderboardEntry дополнена, GameSession+GameStatus, SaveRepository+LeaderboardRepository в ports/ |
| 2026-04-04 | D5.1–D5.3 ✅ | Build зелёный, запрещённых импортов нет, ФАЗА 1 ЗАКРЫТА. Player получил Inventory+Weapon+геттеры, пустые takeDamage() убраны из Player и Enemy |

---

## АРХИТЕКТУРНЫЕ КРАСНЫЕ ЛИНИИ

Это нельзя нарушать никогда. Если видишь — говори мне:

| Нарушение | Пример | Почему плохо |
|-----------|--------|--------------|
| domain импортирует Lanterna | `import com.googlecode.lanterna` в Actor | domain становится зависимым от UI |
| domain импортирует Jackson | `import com.fasterxml.jackson` в Player | domain должен быть чистой моделью |
| gameplay меняет UI | рисование символов в CombatSystem | смешивание логики и отображения |
| presentation содержит логику боя | расчёт урона в GameScreen | UI слой становится God Object |
| data содержит игровую логику | AI-поведение в JsonSaveRepository | data только хранит, не думает |
