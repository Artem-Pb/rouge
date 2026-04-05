# Presentation Layer Plan — Roguelike Game

**Роль:** presentation
**Дата:** 2026-03-26
**Версия:** 1.0
**Статус:** готов к исполнению

---

## 1. Зона ответственности

Presentation — это **глаза и руки игрока**. Здесь только:
- Рендеринг игрового мира в терминале (Lanterna 3.1.1)
- Обработка ввода с клавиатуры
- Управление экранами (меню, игра, game over, leaderboard, инвентарь)
- Туман войны — визуальное представление трёх состояний тайла
- HUD — HP, статы, уровень, инвентарь

Presentation **не меняет** состояние domain напрямую.
Всё изменение состояния — через `GameEngine` из gameplay.
Presentation **не знает** ни о Jackson, ни о сохранениях.

---

## 2. Технология: Lanterna 3.1.1

Ключевые классы библиотеки, используемые в слое:

| Класс Lanterna | Назначение |
|----------------|------------|
| `DefaultTerminalFactory` | создание терминала |
| `Screen` (интерфейс Lanterna) | буферизованный экран |
| `TerminalScreen` | основная реализация Screen |
| `TextGraphics` | рисование символов и строк |
| `KeyStroke` | одно нажатие клавиши |
| `KeyType` | тип клавиши (Character, Escape, ArrowUp...) |
| `TextColor.ANSI` | 8 стандартных цветов терминала |
| `TerminalPosition` | позиция символа на экране (col, row) |
| `TerminalSize` | размер терминала |

**Цикл рендера Lanterna:**
```
screen.startScreen()
loop:
    screen.clear()
    draw everything via textGraphics
    screen.refresh()
    keyStroke = screen.readInput()  // блокирующий вызов
```

---

## 3. Структура пакетов (создана)

```
ru.school21.rogue.presentation
│
├── screen/
│   ├── Screen.java                  # <<interface>> жизненный цикл экрана
│   ├── ScreenManager.java           # переключение между экранами
│   ├── GameScreen.java              # основной игровой экран
│   ├── MainMenuScreen.java          # главное меню
│   ├── GameOverScreen.java          # экран конца игры + итог
│   ├── LeaderboardScreen.java       # таблица рекордов
│   └── InventoryScreen.java         # выбор предмета из рюкзака (1–9)
│
├── renderer/
│   ├── Renderer.java                # <<interface>> отрисовка GameSession
│   ├── LanternaRenderer.java        # точка входа рендера, координирует суб-рендеры
│   ├── MapRenderer.java             # тайлы, туман войны
│   ├── ActorRenderer.java           # игрок и враги
│   └── HudRenderer.java             # панель статуса
│
├── input/
│   ├── InputHandler.java            # <<interface>> считывание Action
│   ├── LanternaInputHandler.java    # читает KeyStroke → Action
│   ├── Action.java                  # enum всех игровых действий
│   └── KeyBinding.java              # маппинг KeyStroke → Action
│
├── symbol/
│   ├── TileSymbol.java              # TileType → char + TextColor
│   ├── ActorSymbol.java             # EnemyType / Player → char + TextColor
│   └── ItemSymbol.java              # Item subtype → char + TextColor
│
└── hud/
    └── HudPanel.java                # структура панели: HP-бар, статы, инвентарь, сообщения
```

---

## 4. Таблица символов и цветов

### Тайлы карты

| TileType | Символ | Цвет (foreground) |
|----------|--------|-------------------|
| FLOOR | `.` | WHITE |
| WALL | `#` | WHITE |
| CORRIDOR | `·` (U+00B7) | YELLOW |
| EXIT | `>` | GREEN |
| EMPTY | ` ` | — |

### Акторы

| Актор | Символ | Цвет |
|-------|--------|------|
| Player | `@` | WHITE (bold) |
| Zombie | `z` | GREEN |
| Vampire | `v` | RED |
| Ghost | `g` | WHITE |
| Ogr | `O` | YELLOW |
| SnakeMage | `s` | CYAN |

### Предметы на полу

| Тип | Символ | Цвет |
|-----|--------|------|
| Food | `%` | GREEN |
| Weapon | `/` | CYAN |
| Scroll | `?` | YELLOW |
| Elixir | `!` | MAGENTA |
| Treasure | `*` | YELLOW |

### Туман войны (модификаторы)

| Состояние тайла | Что показывать | Цвет |
|-----------------|----------------|------|
| visible=false, explored=false | ничего | — |
| visible=false, explored=true | только `#` стены | DARK GRAY (BLACK bright) |
| visible=true, explored=true | всё: тайл + акторы + предметы | нормальный цвет |

---

## 5. Схема Action и управление

```
Action (enum)
├── MOVE_UP        → клавиша W
├── MOVE_DOWN      → клавиша S
├── MOVE_LEFT      → клавиша A
├── MOVE_RIGHT     → клавиша D
├── USE_WEAPON     → клавиша H   (открывает InventoryScreen для оружия)
├── USE_FOOD       → клавиша J   (открывает InventoryScreen для еды)
├── USE_ELIXIR     → клавиша K   (открывает InventoryScreen для эликсира)
├── USE_SCROLL     → клавиша E   (открывает InventoryScreen для свитка)
├── SAVE_QUIT      → клавиша Q   (сохранить и выйти)
├── SELECT_1..9    → цифры 1–9   (выбор предмета в InventoryScreen)
├── SELECT_0       → цифра 0     (убрать оружие без выброса)
└── ESCAPE         → ESC         (назад / отмена)
```

**Правило:** `LanternaInputHandler` только конвертирует нажатие в `Action`.
Логику того, *что делать* с `Action`, решает `GameScreen` → передаёт в `GameEngine`.

---

## 6. Схема ScreenManager

```
MainMenuScreen
    │ NEW GAME       → создать GameSession → GameScreen
    │ CONTINUE       → загрузить GameSession → GameScreen
    │ LEADERBOARD    → LeaderboardScreen
    └ QUIT           → завершение

GameScreen (игровой цикл)
    │ LEVEL_EXIT     → GameEngine.nextLevel() → обновить рендер
    │ GAME_OVER      → GameOverScreen
    └ SAVE_QUIT      → сохранить → MainMenuScreen

GameOverScreen
    └ ENTER/ESC      → MainMenuScreen (запись в leaderboard уже сделана)

LeaderboardScreen
    └ ESC            → MainMenuScreen

InventoryScreen (оверлей поверх GameScreen)
    │ 1–9            → выбрать предмет → вернуться в GameScreen
    └ ESC / 0        → отмена или unequip weapon
```

---

## 7. Структура HUD-панели

```
┌─────────────────────────────────────────────────────────┐
│  Level: 3         HP: ████████░░  80/100    Gold: 120   │
│  STR: 6  AGI: 4                                         │
├─────────────────────────────────────────────────────────┤
│  Inventory:  Food:3  Weapon:1(+5)  Scroll:0  Elixir:2   │
├─────────────────────────────────────────────────────────┤
│  > You hit the Zombie for 6 damage.                     │
│  > Zombie missed.                                       │
└─────────────────────────────────────────────────────────┘
```

- HP-бар: заполненные и пустые блоки (`█` / `░`)
- Лог событий: последние 2 строки (сообщения от GameEngine)
- Панель закреплена снизу экрана
- Карта занимает верхнюю часть экрана

---

## 8. Блокеры

| Класс | Откуда | Нужен для |
|-------|--------|-----------|
| `GameSession` | domain | рендер и HUD |
| `Level`, `Tile`, `TileType` | domain | MapRenderer |
| `Player` | domain | ActorRenderer, HudRenderer |
| `Enemy`, `EnemyType` | domain | ActorRenderer |
| `Item` + подтипы | domain | ActorRenderer (предметы на полу) |
| `Inventory` | domain | HudRenderer |
| `GameStats` | domain | HudRenderer |
| `GameEngine` | gameplay | GameScreen (обработка Action) |
| `SaveRepository` | domain.ports | MainMenuScreen (CONTINUE) |
| `LeaderboardRepository` | domain.ports | LeaderboardScreen |

---

## 9. Дневные миссии

> Миссия закрыта: код написан, компилируется (`./gradlew build`), результат виден в терминале.

---

### ДЕНЬ 1 — Проверка блокеров и Lanterna bootstrap

#### Миссия 1.1 — Проверить готовность слоёв

Убедиться, что существуют и компилируются:
- `domain`: `GameSession`, `Level`, `Player`, `Enemy`, `Item`, `TileType`, `Inventory`, `GameStats`
- `gameplay`: `GameEngine` с методом `processTurn(Direction)`

**Критерий закрытия:** `./gradlew build` — без ошибок.

---

#### Миссия 1.2 — Lanterna bootstrap в `Main.java`

Убедиться, что Lanterna запускается и не крашит терминал.

В `Main.java`:
```
DefaultTerminalFactory factory = new DefaultTerminalFactory();
Screen lanternaScreen = factory.createScreen();
lanternaScreen.startScreen();
// нарисовать одну строку: "Roguelike started"
lanternaScreen.refresh();
Thread.sleep(1000);
lanternaScreen.stopScreen();
```

**Критерий закрытия:** при `./gradlew run` открывается терминал, видна строка, через 1 секунду закрывается без ошибок.

---

### ДЕНЬ 2 — Action, KeyBinding, InputHandler

#### Миссия 2.1 — `Action.java`

Файл: `input/Action.java`

```java
public enum Action {
    MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT,
    USE_WEAPON, USE_FOOD, USE_ELIXIR, USE_SCROLL,
    SELECT_0, SELECT_1, SELECT_2, SELECT_3,
    SELECT_4, SELECT_5, SELECT_6, SELECT_7,
    SELECT_8, SELECT_9,
    SAVE_QUIT, ESCAPE, UNKNOWN
}
```

**Критерий закрытия:** enum создан, компилируется.

---

#### Миссия 2.2 — `KeyBinding.java`

Файл: `input/KeyBinding.java`

Статический маппинг `KeyStroke → Action`:

| KeyStroke | Action |
|-----------|--------|
| `w` / `W` | MOVE_UP |
| `s` / `S` | MOVE_DOWN |
| `a` / `A` | MOVE_LEFT |
| `d` / `D` | MOVE_RIGHT |
| `h` / `H` | USE_WEAPON |
| `j` / `J` | USE_FOOD |
| `k` / `K` | USE_ELIXIR |
| `e` / `E` | USE_SCROLL |
| `q` / `Q` | SAVE_QUIT |
| `0`–`9` | SELECT_0–SELECT_9 |
| ESC | ESCAPE |
| всё остальное | UNKNOWN |

Метод: `static Action resolve(KeyStroke keyStroke)`

**Критерий закрытия:** `resolve` корректно маппит все клавиши. Проверить вручную логом.

---

#### Миссия 2.3 — `InputHandler.java` и `LanternaInputHandler.java`

`input/InputHandler.java`:
```java
public interface InputHandler {
    Action readAction();
}
```

`input/LanternaInputHandler.java`:
- Зависит от `com.googlecode.lanterna.screen.Screen`
- `readAction()` → `screen.readInput()` → `KeyBinding.resolve(keyStroke)`

**Критерий закрытия:** нажатие `W` в терминале возвращает `Action.MOVE_UP`. Проверить простым тестовым циклом.

---

### ДЕНЬ 3 — Таблица символов

#### Миссия 3.1 — `TileSymbol.java`

Файл: `symbol/TileSymbol.java`

Метод: `static SymbolInfo get(TileType type, boolean visible, boolean explored)`

Возвращает record/класс `SymbolInfo`:
- `char symbol`
- `TextColor foreground`
- `boolean render` — false если тайл не нужно рисовать

Логика туман войны:
```
if (!explored && !visible) → render=false
if (explored && !visible)  → символ '#', цвет DARK (TextColor.ANSI.BLACK + bold)
if (visible)               → нормальный символ + цвет из таблицы раздела 4
```

**Критерий закрытия:** `get(FLOOR, true, true)` → `'.'`, WHITE. `get(FLOOR, false, false)` → `render=false`.

---

#### Миссия 3.2 — `ActorSymbol.java`

Файл: `symbol/ActorSymbol.java`

Методы:
- `static SymbolInfo getPlayer()` → `'@'`, WHITE bold
- `static SymbolInfo getEnemy(EnemyType type)` → по таблице раздела 4

Особый случай Ghost:
- Если `ghost.isInvisible() == true` → не рендерить (`render=false`)
- Если в бою / видим → `render=true`

Принимает `Enemy enemy` для обработки спецслучаев: `getEnemy(Enemy enemy)`

**Критерий закрытия:** каждый тип врага возвращает правильный символ и цвет.

---

#### Миссия 3.3 — `ItemSymbol.java`

Файл: `symbol/ItemSymbol.java`

Метод: `static SymbolInfo get(Item item)`

По таблице раздела 4. Используется только для предметов, лежащих на полу уровня.

**Критерий закрытия:** все 5 типов предметов возвращают корректный символ.

---

### ДЕНЬ 4 — Рендеринг: Map и Actors

#### Миссия 4.1 — `Renderer.java` интерфейс

Файл: `renderer/Renderer.java`

```java
public interface Renderer {
    void render(GameSession session);
}
```

`GameSession` содержит всё необходимое: Player, Level, Stats.

**Критерий закрытия:** интерфейс создан.

---

#### Миссия 4.2 — `MapRenderer.java`

Файл: `renderer/MapRenderer.java`

Зависит от: `TextGraphics`, `TileSymbol`

Метод: `void render(TextGraphics g, Level level)`

Алгоритм:
```
for each row y in level.tiles:
    for each col x in level.tiles[y]:
        tile = level.tiles[y][x]
        info = TileSymbol.get(tile.type, tile.visible, tile.explored)
        if info.render:
            g.setForegroundColor(info.foreground)
            g.putString(x, y, String.valueOf(info.symbol))
```

**Критерий закрытия:** карта рендерится в терминале. Комнаты видны, коридоры видны, пустое пространство — пустое.

---

#### Миссия 4.3 — `ActorRenderer.java`

Файл: `renderer/ActorRenderer.java`

Зависит от: `TextGraphics`, `ActorSymbol`, `ItemSymbol`

Методы:
- `void renderPlayer(TextGraphics g, Player player)`
- `void renderEnemies(TextGraphics g, List<Enemy> enemies, Level level)` — рендерить только если тайл под врагом `visible=true`
- `void renderItems(TextGraphics g, List<Item> items, Level level)` — рендерить только если тайл `visible=true`

**Критерий закрытия:** `@` игрок виден на экране. Враги в видимых комнатах отображаются правильным символом и цветом.

---

### ДЕНЬ 5 — HUD и LanternaRenderer

#### Миссия 5.1 — `HudPanel.java`

Файл: `hud/HudPanel.java`

Поля:
- `Queue<String> messageLog` — максимум 2 последних сообщения
- Методы: `void addMessage(String msg)`, `List<String> getMessages()`

Это **только модель данных** для HUD. Рендерит `HudRenderer`.

**Критерий закрытия:** сообщения добавляются, хранятся последние 2, старые вытесняются.

---

#### Миссия 5.2 — `HudRenderer.java`

Файл: `renderer/HudRenderer.java`

Зависит от: `TextGraphics`, `HudPanel`

Метод: `void render(TextGraphics g, Player player, GameStats stats, HudPanel hud, int startRow)`

Рисует с позиции `startRow` (нижняя часть экрана):

**Строка 1 — статус:**
```
Level: 3   HP: ████████░░  80/100   Gold: 120
```
- HP-бар: 10 символов, `█` = заполненных, `░` = пустых
- `filled = (int)(10.0 * player.health / player.maxHealth)`

**Строка 2 — характеристики:**
```
STR: 6   AGI: 4   Kills: 7   Steps: 214
```

**Строка 3 — инвентарь:**
```
Inventory:  Food:3  Weapon:/+5  Scroll:0  Elixir:2  Treasure:120
```

**Строки 4–5 — лог:**
```
> You hit Zombie for 6 damage.
> Zombie missed you.
```

**Критерий закрытия:** HUD отображается в нижней части терминала с актуальными данными.

---

#### Миссия 5.3 — `LanternaRenderer.java`

Файл: `renderer/LanternaRenderer.java`

Реализует `Renderer`. Координирует все суб-рендеры.

Зависит от (через конструктор):
- `Screen lanternaScreen`
- `MapRenderer mapRenderer`
- `ActorRenderer actorRenderer`
- `HudRenderer hudRenderer`
- `HudPanel hudPanel`

Метод `render(GameSession session)`:
```
1. screen.clear()
2. TextGraphics g = screen.newTextGraphics()
3. mapRenderer.render(g, session.currentLevel)
4. actorRenderer.renderItems(g, session.currentLevel.items, session.currentLevel)
5. actorRenderer.renderEnemies(g, session.currentLevel.enemies, session.currentLevel)
6. actorRenderer.renderPlayer(g, session.player)
7. int hudRow = screen.getTerminalSize().getRows() - 5
8. hudRenderer.render(g, session.player, session.stats, hudPanel, hudRow)
9. screen.refresh()
```

**Критерий закрытия:** полный кадр рисуется за один вызов `render()`. Карта + акторы + HUD видны одновременно.

---

### ДЕНЬ 6 — Экраны

#### Миссия 6.1 — `Screen.java` интерфейс

Файл: `screen/Screen.java`

```java
public interface Screen {
    void show();    // инициализация, первый рендер
    void hide();    // очистка ресурсов
    Screen handleInput(Action action);  // возвращает следующий экран (или this)
}
```

`handleInput` возвращает `Screen`:
- `return this` — остаться на текущем экране
- `return new GameScreen(...)` — перейти на другой экран
- `return null` — завершение приложения

**Критерий закрытия:** интерфейс создан, компилируется.

---

#### Миссия 6.2 — `MainMenuScreen.java`

Файл: `screen/MainMenuScreen.java`

Пункты меню:
```
  ROGUELIKE

  [1] New Game
  [2] Continue
  [3] Leaderboard
  [4] Quit
```

`handleInput`:
- `SELECT_1` → `new GameScreen(GameEngine с новой GameSession)`
- `SELECT_2` → если `saveRepository.hasSave()` → `new GameScreen(загруженная сессия)` иначе — показать сообщение "No save found"
- `SELECT_3` → `new LeaderboardScreen(...)`
- `SELECT_4` / `ESCAPE` → `return null`

**Критерий закрытия:** меню рендерится, навигация работает, переходы между экранами корректны.

---

#### Миссия 6.3 — `GameOverScreen.java`

Файл: `screen/GameOverScreen.java`

Отображает:
```
  GAME OVER

  Level reached:  7
  Gold collected: 310
  Enemies killed: 23

  Press ENTER to continue
```

`handleInput`:
- `ESCAPE` или любой ввод → `new MainMenuScreen(...)`

**Критерий закрытия:** экран отображается с реальными данными из `GameStats`.

---

#### Миссия 6.4 — `LeaderboardScreen.java`

Файл: `screen/LeaderboardScreen.java`

Отображает топ-10:
```
  LEADERBOARD

  #   Name       Gold    Level
  1.  Hero       450     12
  2.  Rogue      310     8
  3.  ...

  Press ESC to go back
```

`handleInput`:
- `ESCAPE` → `new MainMenuScreen(...)`

Зависит от: `LeaderboardRepository`

**Критерий закрытия:** загружает реальные данные, отображает отсортированный список.

---

#### Миссия 6.5 — `InventoryScreen.java`

Файл: `screen/InventoryScreen.java`

Оверлей поверх игрового экрана. Отображается при `USE_WEAPON / USE_FOOD / USE_ELIXIR / USE_SCROLL`.

Пример для еды:
```
  SELECT FOOD  (ESC to cancel)

  1. Bread       (+30 HP)
  2. Meat        (+50 HP)
  ...
```

`handleInput`:
- `SELECT_1..9` → вернуть выбранный предмет в `GameScreen` → применить через `ItemSystem`
- `SELECT_0` → только для оружия (unequip)
- `ESCAPE` → вернуться в `GameScreen` без действия

**Критерий закрытия:** список предметов нужного типа отображается, выбор корректно передаётся обратно.

---

#### Миссия 6.6 — `GameScreen.java`

Файл: `screen/GameScreen.java`

Центральный экран. Самый сложный.

Зависит от (через конструктор):
- `LanternaRenderer renderer`
- `InputHandler inputHandler`
- `GameEngine gameEngine`
- `HudPanel hudPanel`
- `SaveRepository saveRepository`

Метод `show()`: первый рендер сессии.

Метод `handleInput(Action action)`:
```
switch action:
  MOVE_* → gameEngine.processTurn(direction) → обновить hudPanel лог → renderer.render()
  USE_FOOD → return new InventoryScreen(FOOD, ...)
  USE_WEAPON → return new InventoryScreen(WEAPON, ...)
  USE_ELIXIR → return new InventoryScreen(ELIXIR, ...)
  USE_SCROLL → return new InventoryScreen(SCROLL, ...)
  SAVE_QUIT → saveRepository.save(session) → return new MainMenuScreen(...)
  ESCAPE → return this
```

Обработка результатов от `GameEngine`:
- `LEVEL_EXIT` → `gameEngine.loadNextLevel()` → `renderer.render()`
- `GAME_OVER` → `leaderboardRepository.save(entry)` → `return new GameOverScreen(...)`

**Критерий закрытия:** полный игровой цикл: ввод → `processTurn` → рендер. Переход между уровнями работает. Game over → GameOverScreen.

---

#### Миссия 6.7 — `ScreenManager.java`

Файл: `screen/ScreenManager.java`

Главный цикл приложения:

```java
public void start(Screen initialScreen) {
    Screen current = initialScreen;
    current.show();
    while (current != null) {
        Action action = inputHandler.readAction();
        Screen next = current.handleInput(action);
        if (next != current) {
            current.hide();
            if (next != null) next.show();
        }
        current = next;
    }
}
```

Зависит от: `InputHandler`

**Критерий закрытия:** `ScreenManager.start(new MainMenuScreen(...))` запускает приложение. Переходы между экранами через `handleInput` работают корректно.

---

### ДЕНЬ 7 — Финальная сборка и проверка

#### Миссия 7.1 — Wiring в `Application` / `Main`

В `Main.java` собрать весь граф зависимостей:

```
ObjectMapper mapper = JacksonConfig.create()
SaveRepository saveRepo = new JsonSaveRepository(mapper, "save.json")
LeaderboardRepository lbRepo = new JsonLeaderboardRepository(mapper, "leaderboard.json")

Screen lanternaScreen = new DefaultTerminalFactory().createScreen()
lanternaScreen.startScreen()

HudPanel hudPanel = new HudPanel()
MapRenderer mapRenderer = new MapRenderer()
ActorRenderer actorRenderer = new ActorRenderer()
HudRenderer hudRenderer = new HudRenderer()
LanternaRenderer renderer = new LanternaRenderer(lanternaScreen, mapRenderer, actorRenderer, hudRenderer, hudPanel)

InputHandler inputHandler = new LanternaInputHandler(lanternaScreen)
ScreenManager manager = new ScreenManager(inputHandler)

MainMenuScreen menu = new MainMenuScreen(renderer, saveRepo, lbRepo, ...)
manager.start(menu)
```

**Критерий закрытия:** `./gradlew run` запускает игру. Главное меню отображается в терминале.

---

#### Миссия 7.2 — Проверка изоляции слоя

Убедиться, что ни один файл в `presentation/` не содержит:
- `import com.fasterxml.jackson`
- `import ru.school21.rogue.data`
- Прямого изменения domain-объектов (кроме чтения)

**Критерий закрытия:** grep по `presentation/` на запрещённые импорты → 0 результатов.

---

#### Миссия 7.3 — Smoke-тест полного прохождения

Ручная проверка:
1. Запустить игру → увидеть главное меню
2. New Game → появилась карта с `@`, врагами, HUD
3. Нажать WASD → персонаж движется, туман войны обновляется
4. Подойти к врагу → инициируется бой, лог в HUD обновляется
5. Нажать J → открывается InventoryScreen для еды
6. Выбрать 1 → HP игрока восстанавливается
7. Найти `>` выход → переход на следующий уровень
8. Нажать Q → сохранение, возврат в меню
9. Continue → загрузка сохранённой сессии
10. Умереть → GameOverScreen с итогами → Leaderboard обновился

**Критерий закрытия:** все 10 шагов пройдены без крашей и артефактов отображения.

---

## 10. Сводная таблица миссий

| День | Миссия | Файл | Критерий |
|------|--------|------|----------|
| 1 | 1.1 Блокеры | — | build OK |
| 1 | 1.2 Lanterna bootstrap | `Main.java` | терминал открывается |
| 2 | 2.1 Action | `input/Action.java` | enum компилируется |
| 2 | 2.2 KeyBinding | `input/KeyBinding.java` | W → MOVE_UP |
| 2 | 2.3 InputHandler | `input/InputHandler.java` + Lanterna impl | W читается в терминале |
| 3 | 3.1 TileSymbol | `symbol/TileSymbol.java` | туман войны корректен |
| 3 | 3.2 ActorSymbol | `symbol/ActorSymbol.java` | все враги → символ+цвет |
| 3 | 3.3 ItemSymbol | `symbol/ItemSymbol.java` | все предметы → символ+цвет |
| 4 | 4.1 Renderer интерфейс | `renderer/Renderer.java` | компилируется |
| 4 | 4.2 MapRenderer | `renderer/MapRenderer.java` | карта видна в терминале |
| 4 | 4.3 ActorRenderer | `renderer/ActorRenderer.java` | @ и враги видны |
| 5 | 5.1 HudPanel | `hud/HudPanel.java` | лог хранит 2 сообщения |
| 5 | 5.2 HudRenderer | `renderer/HudRenderer.java` | HP-бар и статы видны |
| 5 | 5.3 LanternaRenderer | `renderer/LanternaRenderer.java` | полный кадр за один render() |
| 6 | 6.1 Screen интерфейс | `screen/Screen.java` | компилируется |
| 6 | 6.2 MainMenuScreen | `screen/MainMenuScreen.java` | навигация работает |
| 6 | 6.3 GameOverScreen | `screen/GameOverScreen.java` | статы отображаются |
| 6 | 6.4 LeaderboardScreen | `screen/LeaderboardScreen.java` | топ-10 из файла |
| 6 | 6.5 InventoryScreen | `screen/InventoryScreen.java` | выбор предмета 1–9 |
| 6 | 6.6 GameScreen | `screen/GameScreen.java` | полный игровой цикл |
| 6 | 6.7 ScreenManager | `screen/ScreenManager.java` | переходы между экранами |
| 7 | 7.1 Wiring в Main | `Main.java` | `./gradlew run` запускает игру |
| 7 | 7.2 Изоляция | grep | 0 запрещённых импортов |
| 7 | 7.3 Smoke-тест | ручная проверка | 10 шагов без крашей |
