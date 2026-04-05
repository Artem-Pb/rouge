# Architecture Plan — Roguelike Game (Java 21)

**Роль:** architect
**Дата:** 2026-03-25
**Версия:** 1.0

---

## 1. Принципы архитектуры

- **Многослойность** — каждый слой имеет одну зону ответственности
- **Изоляция Domain** — ядро не зависит ни от чего внешнего
- **Dependency Rule** — зависимости направлены строго внутрь (к domain)
- **Разделение логики и отображения** — UI не знает о механиках, механики не знают о UI

---

## 2. Схема зависимостей между слоями

```
┌─────────────────────────────────────────┐
│             presentation                │
│         (Lanterna, Input, UI)           │
└──────────┬──────────────────────────────┘
           │ использует
           ▼
┌─────────────────────────────────────────┐
│              gameplay                   │
│      (логика, AI, генерация, бой)       │
└──────────┬──────────────────────────────┘
           │ использует
           ▼
┌─────────────────────────────────────────┐
│               domain                   │
│    (сущности, предметы, мир, модели)    │
└─────────────────────────────────────────┘
           ▲
           │ использует (только модели)
┌──────────┴──────────────────────────────┐
│                data                     │
│       (Jackson, JSON, сохранения)       │
└─────────────────────────────────────────┘
```

**Правила зависимостей:**

| Слой         | Может зависеть от | НЕ может зависеть от          |
|--------------|-------------------|-------------------------------|
| domain       | —                 | gameplay, presentation, data  |
| gameplay     | domain            | presentation, data            |
| presentation | gameplay, domain  | data (напрямую)               |
| data         | domain            | gameplay, presentation        |

> Presentation взаимодействует с data только через интерфейс, инжектируемый снаружи (точка входа Application).

---

## 3. Package Structure

```
com.roguelike
│
├── domain/
│   ├── entity/
│   │   ├── GameEntity              # базовый абстрактный класс (position, char symbol)
│   │   ├── Player                  # игрок (health, maxHealth, strength, agility)
│   │   └── enemy/
│   │       ├── Enemy               # абстрактный враг (health, strength, agility, aggroRadius)
│   │       ├── Zombie
│   │       ├── Vampire
│   │       ├── Ghost
│   │       ├── Ogre
│   │       └── SnakeMage
│   │
│   ├── item/
│   │   ├── Item                    # абстрактный предмет (name, symbol)
│   │   ├── Treasure                # очки
│   │   ├── Food                    # восстановление здоровья
│   │   ├── Elixir                  # временный бонус
│   │   ├── Scroll                  # постоянный бонус
│   │   └── Weapon                  # увеличение урона
│   │
│   ├── inventory/
│   │   └── Inventory               # хранит до 9 предметов каждого типа
│   │
│   ├── world/
│   │   ├── Level                   # уровень (номер, комнаты, тайлы, враги, предметы)
│   │   ├── Room                    # комната (координаты, размер, тип)
│   │   ├── Corridor                # коридор (соединяет две комнаты)
│   │   ├── Tile                    # тайл карты (тип, координаты)
│   │   └── TileType                # enum: FLOOR, WALL, CORRIDOR, EXIT, EMPTY
│   │
│   ├── combat/
│   │   └── CombatResult            # value object: hit/miss, damage dealt, killed
│   │
│   ├── stats/
│   │   └── GameStats               # статистика: уровень, золото, убийства, шаги
│   │
│   └── leaderboard/
│       └── LeaderboardEntry        # запись: имя, золото, уровень, дата
│
│
├── gameplay/
│   ├── engine/
│   │   └── GameEngine              # главный цикл игры, координирует системы
│   │
│   ├── combat/
│   │   └── CombatSystem            # пошаговый бой: попадание → урон → применение
│   │
│   ├── movement/
│   │   └── MovementSystem          # перемещение игрока, проверка коллизий
│   │
│   ├── ai/
│   │   ├── EnemyAI                 # <<interface>> — стратегия поведения врага
│   │   └── behavior/
│   │       ├── ZombieAI
│   │       ├── VampireAI
│   │       ├── GhostAI
│   │       ├── OgreAI
│   │       └── SnakeMageAI
│   │
│   ├── generation/
│   │   ├── LevelGenerator          # процедурная генерация уровня 3x3 комнаты
│   │   └── RoomConnector           # алгоритм соединения комнат коридорами
│   │
│   ├── fog/
│   │   └── FogOfWarSystem          # три состояния: невидимо / исследовано / видимо
│   │
│   └── item/
│       └── ItemSystem              # логика подбора и применения предметов
│
│
├── presentation/
│   ├── screen/
│   │   ├── Screen                  # <<interface>> — жизненный цикл экрана
│   │   ├── GameScreen              # игровой экран (рендер + ввод)
│   │   ├── MainMenuScreen          # главное меню
│   │   ├── GameOverScreen          # экран конца игры
│   │   └── LeaderboardScreen       # таблица рекордов
│   │
│   ├── renderer/
│   │   ├── Renderer                # <<interface>> — отрисовка состояния
│   │   └── LanternaRenderer        # реализация через Lanterna
│   │
│   ├── input/
│   │   ├── InputHandler            # <<interface>> — считывание ввода
│   │   └── LanternaInputHandler    # реализация через Lanterna
│   │
│   ├── hud/
│   │   └── HUD                     # здоровье, инвентарь, уровень, статистика
│   │
│   └── ScreenManager               # управляет переключением между экранами
│
│
├── data/
│   ├── save/
│   │   ├── SaveRepository          # <<interface>> — сохранение/загрузка игры
│   │   ├── JsonSaveRepository      # реализация через Jackson
│   │   └── GameStateDto            # DTO: полное состояние игры для сериализации
│   │
│   └── leaderboard/
│       ├── LeaderboardRepository   # <<interface>> — CRUD таблицы рекордов
│       └── JsonLeaderboardRepository # реализация через Jackson
│
│
└── Application                     # точка входа: собирает зависимости, запускает игру
```

---

## 4. Основные интерфейсы

### Domain — маркерные интерфейсы (поведение сущностей)

| Интерфейс    | Методы                          | Кто реализует        |
|--------------|---------------------------------|----------------------|
| `Damageable` | `takeDamage(int)`, `isAlive()`  | Player, Enemy        |
| `Movable`    | `getPosition()`, `setPosition()`| Player, Enemy        |

---

### Gameplay — стратегии и системы

| Интерфейс    | Методы                          | Назначение                        |
|--------------|---------------------------------|-----------------------------------|
| `EnemyAI`    | `takeTurn(Enemy, Level, Player)`| Стратегия поведения врага         |

---

### Presentation — абстракции UI

| Интерфейс      | Методы                          | Назначение                     |
|----------------|---------------------------------|--------------------------------|
| `Screen`       | `show()`, `hide()`, `render()`  | Жизненный цикл экрана          |
| `Renderer`     | `render(GameState)`             | Отрисовка состояния игры       |
| `InputHandler` | `readInput(): Action`           | Считывание и маппинг ввода     |

---

### Data — репозитории

| Интерфейс                | Методы                                       | Назначение            |
|--------------------------|----------------------------------------------|-----------------------|
| `SaveRepository`         | `save(GameStateDto)`, `load(): GameStateDto` | Сохранение/загрузка   |
| `LeaderboardRepository`  | `save(LeaderboardEntry)`, `getTop(int)`      | Таблица рекордов      |

---

## 5. Точка входа и сборка зависимостей

`Application` — единственное место, где все слои собираются вместе:

```
Application
  создаёт: JsonSaveRepository, JsonLeaderboardRepository
  создаёт: LanternaRenderer, LanternaInputHandler
  создаёт: GameEngine (передаёт domain-объекты)
  создаёт: ScreenManager (передаёт renderer, input, engine)
  запускает: ScreenManager.start()
```

Ни один слой не создаёт зависимости самостоятельно — они получают их через конструктор.

---

## 6. Поток данных (игровой цикл)

```
InputHandler → Action → GameEngine → CombatSystem / MovementSystem / ItemSystem
                                   → обновляет Player, Level, Enemies
                                   → возвращает обновлённый GameState
GameState → Renderer → отображает через Lanterna
```

---

## 7. Структура сохранения (GameStateDto)

Поля, которые сериализуются в JSON:

- номер уровня
- характеристики игрока (health, maxHealth, strength, agility)
- инвентарь (список предметов с типами)
- состояние текущего уровня (тайлы, враги, предметы, позиции)
- статистика (GameStats)

> `GameStateDto` — это отдельный объект в пакете `data.save`, НЕ доменная сущность. Маппинг между domain-объектами и DTO происходит в `JsonSaveRepository`.

---

## 8. Итоговые границы слоёв

| Слой         | Зависит от             | Внешние библиотеки | Содержит                          |
|--------------|------------------------|--------------------|------------------------------------|
| domain       | —                      | —                  | Сущности, предметы, мир, value objects |
| gameplay     | domain                 | —                  | Логика, AI, генерация, системы    |
| presentation | gameplay, domain       | Lanterna           | Экраны, рендер, ввод, HUD         |
| data         | domain                 | Jackson            | Репозитории, DTO, JSON I/O        |
