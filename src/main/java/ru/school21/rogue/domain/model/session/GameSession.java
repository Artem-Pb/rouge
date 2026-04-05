package ru.school21.rogue.domain.model.session;

import ru.school21.rogue.domain.model.character.Player;
import ru.school21.rogue.domain.model.world.Level;

public class GameSession {
    private final Player player;
    private Level currentLevel;
    private final GameStats gameStats;
    private GameStatus gameStatus;

    public GameSession(Player player, Level level, GameStats gameStats, GameStatus gameStatus) {
        this.player = player;
        this.currentLevel = level;
        this.gameStats = gameStats;
        this.gameStatus = gameStatus;
    }

    public Player getPlayer() {
        return player;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public GameStats getGameStats() {
        return gameStats;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public void setCurrentLevel(Level currentLevel) {
        this.currentLevel = currentLevel;
    }
}
